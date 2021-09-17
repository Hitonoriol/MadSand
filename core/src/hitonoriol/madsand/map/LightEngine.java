package hitonoriol.madsand.map;

import static hitonoriol.madsand.MadSand.player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Circle;
import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.containers.Tuple;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.util.Strings;

public class LightEngine {
	private Map map;
	private Deque<MapEntity> emitters = new ArrayDeque<>();

	/*
	 * Each MapEntity's light map is generated and stored separately,
	 * where each Tile's lightLevel is identified by a Tuple<Entity, Position hashcode>.
	 * After all light emitting entities have been processed, their light maps are
	 * combined and added to the map in the end() method. 
	 */
	private HashMap<Tuple<MapEntity, Integer>, Integer> entityLightMap = new HashMap<>();
	private Tuple<MapEntity, Integer> tmpLightId = new Tuple<>();

	public LightEngine(Map map) {
		this.map = map;
	}

	private void forEachInRenderArea(BiConsumer<Integer, Integer> action) {
		Player player = player();
		Circle.forEachPoint(player.x, player.y, player.getFov(),
				(x, y) -> action.accept(x + player.x, y + player.y));
	}

	/* Prepare render area, setting light level to 0 (Tile.setVisible()) for all tiles in it */
	public void begin() {
		entityLightMap.clear();
		emitters.clear();
		skyLight = MadSand.world().getSkyLight();
		forEachInRenderArea((x, y) -> map.getTile(x, y).setVisible());
	}

	/* Combine and add changes from all light emitting MapEntities to Map */
	public void end() {
		if (emitters.isEmpty())
			return;

		Pair tilePos = new Pair();
		MutableInt lightLevel = new MutableInt(0);
		Player player = player();
		forEachInRenderArea((x, y) -> {
			tilePos.set(x, y);
			lightLevel.setValue(0);
			emitters.stream().forEach(entity -> {
				int light = getEntityLight(entity, tilePos);
				if (entity != player && entityLightExists(entity, tilePos)) {
					if (light > 0)
						light *= -1;
				}
				lightLevel.add(light);
			});
			Tile tile = map.getTile(x, y);
			tile.setLightLevel(lightLevel.getValue());
			if (tile.visible())
				tile.visited = true;
		});
	}

	private Pair obstacle = new Pair(Pair.nullPair);
	private MutableBoolean wall = new MutableBoolean();
	private int skyLight;

	/*
	 * Calculate light level of a single tile,
	 * 		centerCoords - position of the light source
	 * 		tilePos - position of the Tile which light level is calculated
	 * 		luminosity - emitter's light strength 
	 */
	private int update(Pair centerCoords, Pair tilePos, float luminosity) {
		double dstToCenter = Line.calcDistance(centerCoords.x, centerCoords.y, tilePos.x, tilePos.y);
		int lightDelta = (int) (dstToCenter - luminosity);
		MapObject object = map.getObject(tilePos);
		int lightLevel = Integer.MIN_VALUE;

		if (obstacle.equals(centerCoords))
			obstacle.clear();

		/* Walls block light fully */
		if (wall.isTrue())
			lightLevel = (int) dstToCenter - 1;

		/* Non-wall objects take luminosity into account */
		else if (dstToCenter > luminosity)
			lightLevel = lightDelta;

		/* Empty tiles are lit by the sky */
		if (obstacle.isEmpty())
			lightLevel = lightDelta + skyLight;

		/* Non-transparent objects block light, but are lit themselves */
		if (!object.isTransparent() && !object.emitsLight()) {
			obstacle.set(tilePos);
			wall.setValue(object.isWall);
		}

		return lightLevel;
	}

	/* Get a temporary identifier for a single tile lightLevel in entity's light map */
	private Tuple<MapEntity, Integer> lightId(MapEntity entity, Pair coords) {
		return tmpLightId.set(entity, coords.hashCode());
	}

	private void setEntityLight(MapEntity entity, Pair tilePos, int lightLevel) {
		entityLightMap.put(lightId(entity, tilePos), lightLevel);
	}

	private int getEntityLight(MapEntity entity, Pair tilePos) {
		return entityLightMap.getOrDefault(lightId(entity, tilePos), 0);
	}

	private boolean entityLightExists(MapEntity entity, Pair tilePos) {
		return entityLightMap.containsKey(lightId(entity, tilePos));
	}

	public void update(Player player) {
		update((MapEntity) player);
		skyLight = 0;
	}

	/* Update and store light levels for <entity> */
	public void update(MapEntity entity) {
		emitters.push(entity);
		update(entity, (coords, lightLevel) -> setEntityLight(entity, coords, lightLevel));
	}

	/* Update light in render distance, feeding calculated lightLevels and their positions to lightConsumer */
	private void update(MapEntity entity, BiConsumer<Pair, Integer> lightConsumer) {
		final Pair center = entity.getPosition(), tilePos = new Pair();
		final float luminosity = entity.getLuminosity();
		final int radius = entity == player() ? player().getFov() : (int) (luminosity + 1);

		Circle.forEachPoint(center.x, center.y, radius, (x, y) -> {
			int dx = center.x + x, dy = center.y + y;
			if (!map.validCoords(dx, dy))
				return;

			obstacle.clear();
			wall.setFalse();
			Line.forEachPoint(center.x, center.y, dx, dy, (rx, ry) -> {
				tilePos.set(rx, ry);
				int light = update(center, tilePos, luminosity);
				if (light == Integer.MIN_VALUE)
					return;

				lightConsumer.accept(tilePos, light);
			});
		});
	}

	private StringBuilder debugSb = new StringBuilder();

	public String getEntityLight(Pair tilePos) {
		Strings.clearBuilder(debugSb);
		emitters.forEach(entity -> {
			if (entityLightExists(entity, tilePos))
				Strings.newLine(debugSb.append(entity.getName() + "=" + getEntityLight(entity, tilePos)));
		});
		if (debugSb.length() > 0)
			debugSb.setLength(debugSb.length() - 1);
		return debugSb.toString();
	}
}
