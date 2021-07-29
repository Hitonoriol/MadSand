package hitonoriol.madsand.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import com.badlogic.gdx.math.Vector2;
import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.TimeDependent;
import hitonoriol.madsand.containers.Circle;
import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.TradeListContainer;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.Tool;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.entities.npc.Npc;
import hitonoriol.madsand.entities.skill.Skill;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.enums.TradeCategory;
import hitonoriol.madsand.map.object.Crop;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.map.object.ResourceObject;
import hitonoriol.madsand.pathfinding.Graph;
import hitonoriol.madsand.pathfinding.PathfinfingEngine;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.properties.WorldGenProp;
import hitonoriol.madsand.util.Functional;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.Location;
import hitonoriol.madsand.world.worldgen.OverworldPreset;
import hitonoriol.madsand.world.worldgen.RollList;
import hitonoriol.madsand.world.worldgen.WorldGenPreset;

public class Map {
	private int xsz, ysz;

	public static int MIN_MAPSIZE = 100;
	public static int MAX_MAPSIZE = 500;

	public int defTile = 0;
	public int defObject = 0;

	public boolean editable = true;

	public Pair spawnPoint = Pair.nullPair; // for dungeon levels only

	public static final int COLLISION_MASK_ID = 666;
	public static Tile nullTile = new Tile();
	public static MapObject nullObject = new MapObject();
	public static Loot nullLoot = new Loot();
	public static Crop nullCrop = new Crop();
	public static AbstractNpc nullNpc = new Npc();

	private HashMap<Pair, Tile> mapTiles;
	private HashMap<Pair, MapObject> mapObjects;
	private HashMap<Pair, Loot> mapLoot;
	private HashMap<Pair, AbstractNpc> mapNpcs;

	private final TimeScheduler timeScheduler = new TimeScheduler();
	private PathfinfingEngine pathfinding = new PathfinfingEngine(this);

	Pair coords = new Pair(0, 0);

	public Map(int xsz, int ysz) {
		this.xsz = xsz;
		this.ysz = ysz;
		purge();
	}

	public Map() {
		rollSize();
		purge();
	}

	public void postLoadInit() {}

	/* Cleanup, called on map switch/World.close() */
	public void close() {
		timeScheduler.stop();
	}

	public void rollSize(int min, int max) {
		this.setSize(Utils.rand(min, max), Utils.rand(min, max));
		Utils.out("Rolled map size: " + xsz + ", " + ysz);
	}

	public void rollSize() {
		rollSize(MIN_MAPSIZE, MAX_MAPSIZE);
	}

	@JsonIgnore
	public PathfinfingEngine getPathfindingEngine() {
		return pathfinding;
	}

	@JsonIgnore
	public Graph getPathfindingGraph() {
		return pathfinding.getGraph();
	}

	public void refreshPathfindingGraph() {
		pathfinding.refreshGraph();
	}

	@JsonIgnore
	public HashMap<Pair, MapEntity> getTimeDependentMapEntities() {
		HashMap<Pair, MapEntity> timeDependentMap = new HashMap<>();
		timeScheduler.forEach(tdEntity -> {
			MapEntity entity = (MapEntity) tdEntity;
			timeDependentMap.put(entity.getPosition(), entity);
		});
		return timeDependentMap;
	}

	public void setTimeDependentMapEntities(HashMap<Pair, MapEntity> timeDependentMap) {
		timeDependentMap.forEach((coords, entity) -> {
			entity.as(Entity.class).ifPresent(creature -> creature.postLoadInit());
			boolean restored = entity.add(this, coords);
			Utils.dbg("{%X} Restoring %s at %s: %b",
					timeScheduler.hashCode(), entity.getName(), coords, restored);
		});
	}

	@JsonIgnore
	public HashMap<Pair, AbstractNpc> getNpcs() {
		return mapNpcs;
	}

	public void setNpcs(HashMap<Pair, AbstractNpc> npcs) {
		npcs.forEach((coords, npc) -> add(coords, npc));
	}

	@JsonIgnore
	HashMap<Pair, Tile> getTiles() {
		return mapTiles;
	}

	@JsonIgnore
	HashMap<Pair, MapObject> getObjects() {
		return mapObjects;
	}

	@JsonIgnore
	public HashMap<Pair, Loot> getLoot() {
		return mapLoot;
	}

	public void setLoot(HashMap<Pair, Loot> loot) {
		mapLoot = loot;
	}

	void setTiles(HashMap<Pair, Tile> tiles) {
		mapTiles = tiles;
	}

	void setObjects(HashMap<Pair, MapObject> objects) {
		mapObjects = objects;
	}

	public void setSize(int xsz, int ysz) {
		this.xsz = xsz;
		this.ysz = ysz;

	}

	@JsonIgnore
	public int getWidth() {
		return xsz;
	}

	@JsonIgnore
	public int getHeight() {
		return ysz;
	}

	@JsonIgnore
	public int getArea() {
		return xsz * ysz;
	}

	@JsonIgnore
	public float getScaleFactor() {
		return (float) getArea() / (float) Math.pow(MIN_MAPSIZE, 2.075);
	}

	@JsonIgnore
	public int getObjectCount() {
		return mapObjects.size();
	}

	@JsonIgnore
	public int getNpcCount() {
		return mapNpcs.size();
	}

	@JsonIgnore
	public int getHostileNpcCount() {
		int count = 0;

		for (Entry<Pair, AbstractNpc> entry : mapNpcs.entrySet())
			if (!entry.getValue().friendly)
				++count;

		return count;
	}

	public Pair rayCast(Pair from, Pair to) {
		Pair occupiedTile = new Pair(Pair.nullPair);
		MutableBoolean ignoreTile = new MutableBoolean(true);

		Line.rayCast(from, to, (x, y) -> {
			if (ignoreTile.booleanValue()) {
				ignoreTile.setFalse();
				return true;
			}

			if (!isFreeTile(x, y)) {
				occupiedTile.set(x, y);
				return false;
			}

			return true;
		});

		return occupiedTile;
	}

	public MapEntity getMapEntity(Pair coords) {
		MapEntity mapEntity;
		if ((mapEntity = getObject(coords)) != nullObject)
			return mapEntity;

		return getNpc(coords);
	}

	public Pair getRandomPoint(int distanceFromPlayer, int maxDistanceFromPlayer) {
		Player player = MadSand.player();
		int x, y;
		int distance;

		do {
			x = Utils.rand(xsz);
			y = Utils.rand(ysz);
			distance = (int) Line.calcDistance(x, y, player.x, player.y);

			if (isFreeTile(x, y))
				continue;

		} while (distance < distanceFromPlayer || distance > maxDistanceFromPlayer);

		return coords.set(x, y);
	}

	public Pair getRandomPoint(int distanceFromPlayer) {
		return getRandomPoint(distanceFromPlayer, getArea());
	}

	public Pair getRandomPoint() {
		return getRandomPoint(-1);
	}

	public Map purge() {
		mapTiles = new HashMap<>();
		mapObjects = new HashMap<>();
		mapLoot = new HashMap<>();
		mapNpcs = new HashMap<>();
		timeScheduler.clear();
		pathfinding.init();
		return this;
	}

	void putTileInDir(int x, int y, Direction dir, int id) {
		this.addTile(x, y, dir, id);
	}

	@FunctionalInterface
	private interface MapAction {
		public boolean changeMap(int x, int y, int id);
	}

	private MapAction tileAction = (int x, int y, int id) -> {
		return addTile(x, y, id, true);
	};

	private MapAction objectAction = (int x, int y, int id) -> {
		if (id == 0) {
			delObject(x, y);
			return true;
		}

		if (getTile(x, y).foreground)
			return false;

		if (objectExists(x, y))
			return false;

		return addObject(x, y, id, true);
	};

	private MapAction lootAction = (int x, int y, int id) -> {
		if (objectExists(x, y))
			return false;

		putLoot(x, y, id, 1);

		return true;
	};

	private MapAction cropAction = (int x, int y, int id) -> {
		if (objectExists(x, y))
			return false;

		return putCrop(x, y, id);
	};

	private static double ERODE_PROBABILITY = 30;
	private MapAction erodeTileAction = (int x, int y, int id) -> {
		if (Utils.percentRoll(ERODE_PROBABILITY))
			return addTile(x, y, id);

		return false;
	};

	private Map drawRect(MapAction action, int x, int y, int w, int h, int id, boolean fill) {
		int rectWidth = x + w, rectHeight = y + h;
		int startX = x, startY = y;

		for (; x <= rectWidth; ++x) {
			for (y = startY; y <= rectHeight; ++y)
				if (fill || (x == startX || y == startY || x == rectWidth || y == rectHeight))
					action.changeMap(x, y, id);
		}

		return this;
	}

	private Map drawLine(MapAction action, int x1, int y1, int x2, int y2, int id) {
		Line.forEachPoint(x1, y1, x2, y2, (x, y) -> action.changeMap(x, y, id));
		return this;
	}

	private Map drawCircle(MapAction action, int x0, int y0, int radius, int id, boolean fill) {
		int x, y;
		Pair coords = new Pair();
		double angle;
		for (float i = 0; i < 360; ++i) {
			angle = Math.toRadians(i);
			x = x0 + (int) (radius * Math.cos(angle));
			y = y0 + (int) (radius * Math.sin(angle));

			if (coords.equals(x, y) || !validCoords(x, y))
				continue;

			coords.set(x, y);

			if (fill)
				drawLine(action, x0, y0, x, y, id);
			else
				action.changeMap(x, y, id);
		}
		return this;
	}

	private void fillTriangle(MapAction action, Pair p1, Pair p2, Pair p3, int id) {
		int maxX = Math.max(p1.x, Math.max(p2.x, p3.x));
		int minX = Math.min(p1.x, Math.min(p2.x, p3.x));
		int maxY = Math.max(p1.y, Math.max(p2.y, p3.y));
		int minY = Math.min(p1.y, Math.min(p2.y, p3.y));

		Vector2 vs1 = new Vector2(p2.x - p1.x, p2.y - p1.y);
		Vector2 vs2 = new Vector2(p3.x - p1.x, p3.y - p1.y);
		Vector2 q = new Vector2();
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				q.set(x - p1.x, y - p1.y);

				float s = q.crs(vs2) / vs1.crs(vs2);
				float t = vs1.crs(q) / vs1.crs(vs2);

				if ((s >= 0) && (t >= 0) && (s + t <= 1))
					action.changeMap(x, y, id);
			}
		}
	}

	private Map drawTriangle(MapAction action, Pair p1, Pair p2, Pair p3, int id) {
		drawLine(action, p1.x, p1.y, p2.x, p2.y, id);
		drawLine(action, p1.x, p1.y, p3.x, p3.y, id);
		drawLine(action, p2.x, p2.y, p3.x, p3.y, id);
		return this;
	}

	public Map drawObjectLine(int x1, int y1, int x2, int y2, int id) {
		return drawLine(objectAction, x1, y1, x2, y2, id);
	}

	public Map drawObjectTriangle(Pair p1, Pair p2, Pair p3, int id) {
		return drawTriangle(objectAction, p1, p2, p3, id);
	}

	public Map drawObjectCircle(int x0, int y0, int radius, int id) {
		return drawCircle(objectAction, x0, y0, radius, id, false);
	}

	public Map drawTileCircle(int x0, int y0, int radius, int id) {
		return drawCircle(tileAction, x0, y0, radius, id, false);
	}

	public Map erodeTileCircle(int x0, int y0, int radius, int id) {
		return drawCircle(erodeTileAction, x0, y0, radius, id, false);
	}

	public Map drawTileTriangle(Pair p1, Pair p2, Pair p3, int id) {
		return drawTriangle(tileAction, p1, p2, p3, id);
	}

	public Map erodeTileRectangle(int x, int y, int w, int h, int id) {
		return drawRect(erodeTileAction, x, y, w, h, id, false);
	}

	public Map erodeTileRectangle(int x, int y, int w, int h, int depth, int id) {
		for (; depth >= 0; --depth)
			erodeTileRectangle(x + depth, y + depth, w - depth, h - depth, id);
		return this;
	}

	public Map drawTileRectangle(int x, int y, int w, int h, int id) {
		return drawRect(tileAction, x, y, w, h, id, false);
	}

	public Map drawObjectRectangle(int x, int y, int w, int h, int id) {
		return drawRect(objectAction, x, y, w, h, id, false);
	}

	public Map fillTile(int x, int y, int w, int h, int id) {
		return drawRect(tileAction, x, y, w, h, id, true);
	}

	public Map fillTile(int id) {
		return fillTile(0, 0, xsz, ysz, id);
	}

	public Map fillTile() {
		return fillTile(defTile);
	}

	public Map fillObject(int x, int y, int w, int h, int id) {
		return drawRect(objectAction, x, y, w, h, id, true);
	}

	public Map fillObjectCircle(int x0, int y0, int radius, int id) {
		return drawCircle(objectAction, x0, y0, radius, id, true);
	}

	public Map fillTileCircle(int x0, int y0, int radius, int id) {
		return drawCircle(tileAction, x0, y0, radius, id, true);
	}

	public void fillTileTriangle(Pair p1, Pair p2, Pair p3, int id) {
		fillTriangle(tileAction, p1, p2, p3, id);
	}

	public void fillObjectTriangle(Pair p1, Pair p2, Pair p3, int id) {
		fillTriangle(objectAction, p1, p2, p3, id);
	}

	public Map fillObject(int id) {
		return fillObject(0, 0, xsz, ysz, id);
	}

	public Map fillObject() {
		return fillObject(defObject);
	}

	public Map fillCrop(int x, int y, int w, int h, int id) {
		return drawRect(cropAction, x, y, w, h, id, true);
	}

	public boolean validCoords(int x, int y) {
		if (x <= xsz && y <= ysz && x >= 0 && y >= 0)
			return true;
		else
			return false;
	}

	public boolean validCoords(Pair coords) {
		return validCoords(coords.x, coords.y);
	}

	private boolean addTile(Pair coords, Tile tile) {
		return mapTiles.put(coords, tile) == null;
	}

	public boolean addTile(int x, int y, int id, boolean force) {
		if (!validCoords(coords.set(x, y)))
			return false;

		int prevTile = getTile(x, y).id;
		if (force)
			forceDelTile(coords);
		else if (prevTile != defTile && prevTile != nullTile.id)
			return false;

		return addTile(new Pair(coords), new Tile(id));
	}

	private boolean forceDelTile(Pair coords) {
		return mapTiles.remove(coords) != null;
	}

	public boolean delTile(Pair coords) {
		boolean removed = mapTiles.remove(coords) != null;
		mapTiles.put(coords, new Tile(defTile));
		return removed;
	}

	public boolean delTile(int x, int y) {
		return delTile(coords.set(x, y));
	}

	public boolean addTile(int x, int y, int id) {
		return addTile(x, y, id, true);
	}

	public boolean addTile(int x, int y, Direction dir, int id) {
		if (validCoords(coords.set(x, y))) {
			coords.addDirection(dir);
			addTile(coords.x, coords.y, id, false);
			return true;
		} else
			return false;
	}

	public Tile getTile(int x, int y) {
		if (validCoords(coords.set(x, y))) {
			Tile ret = mapTiles.get(coords);
			if (ret == null)
				ret = nullTile;
			return ret;
		} else
			return nullTile;
	}

	public Tile getTile(Pair coords) {
		return getTile(coords.x, coords.y);
	}

	Tile getTile(int x, int y, Direction dir) {
		coords.set(x, y).addDirection(dir);
		return getTile(coords.x, coords.y);
	}

	void setObjectSize(int x, int y, int id) { // If object is bigger than 1x1, fill the rest of the space with COLLISION_MASK_ID
		MapObject objectProp = ObjectProp.getObject(id);
		int i = objectProp.maskHeight;
		if (y + 1 < ysz - 1) {
			while (i > 0) {
				if (y + i < ysz)
					addObject(x, y + i, COLLISION_MASK_ID);
				i--;
			}
		}

		i = objectProp.maskWidth;
		if (x + 1 < xsz - 1) {
			while (i > 0) {
				if (x + i < xsz)
					addObject(x + i, y, COLLISION_MASK_ID);
				i--;
			}
		}
	}

	public boolean delObject(Pair coords) {
		MapObject deletedObject = mapObjects.remove(coords);
		if (deletedObject != null)
			removeTimeDependent(deletedObject);

		return deletedObject != null;
	}

	public boolean delObject(int x, int y) {
		if (!validCoords(coords.set(x, y)))
			return false;

		boolean removed = delObject(coords);

		if (removed)
			pathfinding.objectRemoved(x, y);

		return removed;
	}

	public boolean add(Pair coords, MapObject object) {
		if (!validCoords(coords))
			return false;

		registerTimeDependent(object);
		return mapObjects.put(coords, object) == null;
	}

	public boolean addObject(int x, int y, int id, boolean force) {
		if (!validCoords(coords.set(x, y)) || id == nullObject.id)
			return false;

		if (!force && mapObjects.containsKey(coords))
			return false;

		MapObject object = MapObject.create(id);
		if (add(coords.copy(), object)) {
			setObjectSize(x, y, id);
			pathfinding.objectAdded(object, x, y);
			return true;
		}
		return false;
	}

	public boolean addObject(int x, int y, int id) {
		return addObject(x, y, id, true);
	}

	public boolean addObject(Pair coord, int id) {
		return addObject(coord.x, coord.y, id);
	}

	public MapObject addObject(int x, int y, Direction dir, int id) {
		coords.set(x, y).addDirection(dir);
		if (addObject(coords.x, coords.y, id))
			return getObject(coords);
		else
			return Map.nullObject;
	}

	public MapObject getObject(int x, int y) {
		if (!validCoords(coords.set(x, y)))
			return nullObject;

		MapObject ret = mapObjects.get(coords);

		if (ret == null)
			return nullObject;

		if (ret.id == nullObject.id) {
			delObject(coords);

			if (ret.isDestroyed())
				MadSand.world().updateLight();

			return nullObject;
		}

		return ret;
	}

	public MapObject getObject(Pair coords) {
		return getObject(coords.x, coords.y);
	}

	boolean objectExists(int x, int y) {
		if (validCoords(coords.set(x, y)))
			return !getObject(x, y).equals(nullObject);
		else
			return false;
	}

	private void registerTimeDependent(MapEntity entity) {
		if (!(entity instanceof TimeDependent))
			return;
		timeScheduler.register((TimeDependent) entity);
	}

	private void removeTimeDependent(MapEntity entity) {
		if (!(entity instanceof TimeDependent))
			return;
		timeScheduler.remove((TimeDependent) entity);
	}

	private void brutePlace(Runnable coordModifier, MapAction action, int id) {
		do
			coordModifier.run();
		while (!action.changeMap(coords.x, coords.y, id));
	}

	private void randPlaceInTriangle(MapAction action, int id, Pair p1, Pair p2, Pair p3) {
		brutePlace(() -> coords.randomInTriangle(p1, p2, p3), action, id);
	}

	private void randPlaceInCircle(MapAction action, int id, int x0, int y0, int radius) {
		brutePlace(() -> coords.randomInCircle(x0, y0, radius), action, id);
	}

	private Pair randPlace(MapAction action, int id) {
		brutePlace(() -> coords.random(xsz, ysz), action, id);
		return coords;
	}

	public void randPlaceLootInTriangle(int id, Pair p1, Pair p2, Pair p3) {
		randPlaceInTriangle(lootAction, id, p1, p2, p3);
	}

	public void randPlaceLootInCircle(int id, int x0, int y0, int radius) {
		randPlaceInCircle(lootAction, id, x0, y0, radius);
	}

	public Pair randPlaceObject(int id) {
		return randPlace(objectAction, id);
	}

	public Pair randPlaceObject(ArrayList<Integer> id, int range) {
		return randPlace(objectAction, id.get(Utils.random.nextInt(range)));
	}

	public Pair randPlaceTile(int id) {
		return randPlace(tileAction, id);
	}

	public Pair randPlaceLoot(int id) {
		return randPlace(lootAction, id);
	}

	public MapObject getObject(int x, int y, Direction dir) {
		if (validCoords(coords.set(x, y))) {
			coords.addDirection(dir);
			return getObject(coords.x, coords.y);
		} else
			return nullObject;
	}

	public Loot getLoot(int x, int y) {
		if (validCoords(coords.set(x, y))) {
			Loot ret = mapLoot.get(coords.set(x, y));
			if (ret != null) {
				if (ret.isEmpty()) {
					removeLoot(x, y);
					return nullLoot;
				}
				return ret;
			} else
				return nullLoot;
		} else
			return nullLoot;
	}

	public Loot getLoot(Pair coords) {
		return getLoot(coords.x, coords.y);
	}

	void removeLoot(int x, int y) {
		if (validCoords(coords.set(x, y)))
			mapLoot.remove(coords);
	}

	public void putLoot(int x, int y, int id, int q) {
		putLoot(x, y, Item.create(id, q));
	}

	public void putLoot(int x, int y, int id) {
		putLoot(x, y, id, 1);
	}

	public void putLoot(int x, int y, Item item) {
		if (validCoords(coords.set(x, y))) {
			if (mapLoot.get(coords) != null)
				mapLoot.get(coords).add(item);
			else
				mapLoot.put(new Pair(coords), new Loot(item));
		}
	}

	public void putLoot(Pair coords, Item item) {
		putLoot(coords.x, coords.y, item);
	}

	public void putLoot(Pair coords, int id, int quantity) {
		putLoot(coords.x, coords.y, id, quantity);
	}

	public void putLoot(int x, int y, List<Item> loot) {
		for (Item item : loot)
			putLoot(x, y, item);
	}

	public void putLoot(int x, int y, TradeCategory category, int maxRolls) {
		TradeListContainer trade = NpcProp.tradeLists;
		ArrayList<Item> items = trade.roll(category, trade.rollTier());
		for (int i = 0; i < maxRolls && i < items.size(); ++i)
			putLoot(x, y, items.get(i));
	}

	public void updateLight(int wx, int wy, int r, double luminosity) {
		Pair obstacle = new Pair(Pair.nullPair);
		MutableBoolean wall = new MutableBoolean();
		MutableInt pointCounter = new MutableInt(0);
		final int skyLight = MadSand.world().getSkyLight();
		Circle.forEachPoint(wx, wy, r, (x, y) -> {
			int dx = wx + x, dy = wy + y;
			if (!validCoords(dx, dy))
				return;

			getTile(dx, dy).setVisible();
			obstacle.clear();
			pointCounter.setValue(0);
			wall.setFalse();
			Line.forEachPoint(wx, wy, dx, dy, (rx, ry) -> {
				double dstToCenter = Line.calcDistance(wx, wy, rx, ry);
				int lightDelta = (int) (dstToCenter - luminosity);
				Tile tile = getTile(rx, ry);
				MapObject object = getObject(rx, ry);

				/* Walls block light fully */
				if (wall.isTrue())
					tile.setVisible((int) dstToCenter - 1);

				/* Non-wall objects take luminosity into account */
				else if (dstToCenter > luminosity)
					tile.setVisible(lightDelta);

				/* Empty tiles are light by the sky */
				if (obstacle.isEmpty())
					tile.setVisible(lightDelta + skyLight);

				/* Non-transparent objects block light, but are lit themselves */
				if (!object.isTransparent()) {
					obstacle.set(rx, ry);
					wall.setValue(object.isWall);
				}

				/* If tile is lit, mark it as visited */
				if (tile.visible())
					tile.visited = true;
			});

		});
	}

	public void updateLight(int wx, int wy, int r) {
		updateLight(wx, wy, r, 0);
	}

	public boolean putCrop(int x, int y, int id) { // item id
		return putCrop(x, y, new Crop(id, MadSand.world().currentActionTick()));
	}

	public boolean putCrop(int x, int y, Crop crop) {
		Pair coords = new Pair(x, y);
		if (!validCoords(coords))
			return false;
		if (objectExists(x, y))
			return false;
		if (getTile(x, y).id != ItemProp.getCropSoil(crop.getSeedsId()))
			return false;

		return add(coords, crop);
	}

	public Crop getCrop(int x, int y) {
		if (!validCoords(coords.set(x, y)))
			return nullCrop;

		return getObject(coords.copy()).as(Crop.class).orElse(nullCrop);
	}

	public boolean spawnNpc(int id, int x, int y) {
		if (!validCoords(coords.set(x, y)))
			return false;

		if (!getNpc(coords.x, coords.y).equals(nullNpc))
			return false;

		AbstractNpc npc = NpcProp.spawnNpc(id, x, y);
		return add(new Pair(x, y), npc);
	}

	public boolean spawnNpc(int id, Pair coords) {
		return spawnNpc(id, coords.x, coords.y);
	}

	public AbstractNpc spawnNpc(int id) {
		Pair npcPos = getRandomPoint().copy();
		spawnNpc(id, npcPos);
		return getNpc(npcPos);
	}

	public boolean add(Pair coords, AbstractNpc npc) {
		if (!validCoords(coords))
			return false;

		if (!getNpc(coords).equals(nullNpc))
			return false;

		registerTimeDependent(npc);
		npc.setGridCoords(coords.x, coords.y);
		mapNpcs.put(coords, npc);
		return true;
	}

	public boolean putNpc(AbstractNpc npc, int x, int y) {
		return add(new Pair(x, y), npc);
	}

	public AbstractNpc getNpc(long uid) {
		for (Entry<Pair, AbstractNpc> entry : mapNpcs.entrySet())
			if (entry.getValue().uid == uid)
				return entry.getValue();
		return nullNpc;
	}

	public AbstractNpc getNpc(int x, int y) {
		if (!validCoords(coords.set(x, y)))
			return nullNpc;

		AbstractNpc npc = mapNpcs.get(coords);

		if (npc == null)
			return nullNpc;

		if (!npc.equals(nullNpc))
			return npc;

		return nullNpc;
	}

	public AbstractNpc getNpc(Pair coords) {
		return getNpc(coords.x, coords.y);
	}

	public boolean npcExists(int x, int y) {
		return !getNpc(x, y).equals(nullNpc);
	}

	public boolean npcExists(Pair coords) {
		return npcExists(coords.x, coords.y);
	}

	/*
	 * Moves NPC on the grid (not on the screen) before processing movement animation
	 * called by NPC when changing its position
	 */
	public boolean moveNpc(AbstractNpc npc, int x, int y) {
		int xold = npc.x, yold = npc.y;

		if (!validCoords(coords.set(x, y)))
			return false;

		AbstractNpc destNpc = getNpc(coords.x, coords.y);
		if (destNpc != nullNpc)
			return false;

		add(coords.copy(), npc);

		if (!(xold == x && yold == y))
			removeNpc(xold, yold);

		return true;
	}

	public boolean removeNpc(AbstractNpc npc) {
		return removeNpc(npc.x, npc.y);
	}

	public boolean removeNpc(int x, int y) {
		if (!validCoords(coords.set(x, y)))
			return false;

		if (getNpc(coords.x, coords.y).equals(nullNpc))
			return false;

		AbstractNpc npc = mapNpcs.remove(coords);
		if (npc != null && npc.isDead())
			removeTimeDependent(npc);
		return true;
	}

	public void spawnMobs(boolean friendly, boolean force) {
		if (MadSand.world().curLayer() != Location.LAYER_OVERWORLD)
			return;

		WorldGenPreset preset = WorldGenProp.getBiome(MadSand.world().getLocBiome());
		OverworldPreset overworld = preset.overworld;
		double forceVal = force ? 100 : 0;

		if (getNpcCount() >= getMaxNpcs())
			return;

		Utils.dbg("Auto-spawning mobs, friendly = " + friendly);

		if (friendly)
			spawnFromRollList(overworld.friendlyMobs, overworld.friendlySpawnChance + forceVal);
		else
			spawnFromRollList(overworld.hostileMobs, overworld.hostileSpawnChance + forceVal);
	}

	public void spawnMobs(boolean friendly) {
		spawnMobs(friendly, false);
	}

	public void spawnMobs(String list, int count, int minDst) {
		spawnMobs(list, count, minDst, xsz * ysz);
	}

	public void spawnMobs(String list, int count, int minDst, int maxDst) {
		spawnFromRollList(new RollList(count, Utils.parseList(list)), 100, minDst, maxDst);
	}

	private void spawnFromRollList(RollList list, double chance, int distanceFromPlayer, int maxDistance) {
		if (Utils.percentRoll(chance))
			for (int i = 0; i < list.getRollCount(getScaleFactor()); ++i) {
				do {
					getRandomPoint(distanceFromPlayer, maxDistance);
				} while (!spawnNpc(Utils.randElement(list.idList), coords.x, coords.y));
			}
	}

	private void spawnFromRollList(RollList list, double chance, int distanceFromPlayer) {
		spawnFromRollList(list, chance, distanceFromPlayer, xsz * ysz);
	}

	private void spawnFromRollList(RollList list, double chance) {
		spawnFromRollList(list, chance, MadSand.player().getFov());
	}

	public void naturalRegeneration() {
		if (MadSand.world().curLayer() != Location.LAYER_OVERWORLD)
			return;

		WorldGenPreset preset = WorldGenProp.getBiome(MadSand.world().getLocBiome());
		OverworldPreset overworld = preset.overworld;

		if (overworld.regenerateObjects == null)
			return;

		if (!Utils.percentRoll(overworld.chanceToRegenerate))
			return;

		int maxObjects = getMaxObjects();

		Utils.out("Performing natural object regeneration");

		if (getObjectCount() < maxObjects)
			for (RollList rollList : overworld.regenerateObjects)
				rollObjects(rollList);
	}

	public boolean addStructure(MapStructure structure) {
		return structure.build();
	}

	public boolean addStructure(String name, int x, int y) {
		return addStructure(new MapStructure(x, y).setName(name));
	}

	public MapStructure addStructure(String name) {
		Pair coords = new Pair();
		MapStructure structure = new MapStructure(coords.random(xsz, ysz)).setName(name);

		do
			coords.random(xsz, ysz);
		while (!addStructure(structure.setCoords(coords)));

		Utils.dbg("Successfully generated " + name + " at " + coords);
		return structure;
	}

	public void delAll(int x, int y) {
		removeNpc(x, y);
		delObject(x, y);
	}

	public boolean isFreeTile(int x, int y) {
		return !npcExists(x, y) && !objectExists(x, y);
	}

	public boolean isFreeTile(Pair coords) {
		return isFreeTile(coords.x, coords.y);
	}

	public Pair getFreeTileNear(Pair coords) {
		Pair direction = new Pair();
		Pair freeTile = new Pair();
		Collections.shuffle(Direction.directions);

		for (Direction dir : Direction.directions)
			if (isFreeTile(freeTile.set(coords).add(direction.fromDirection(dir))))
				return freeTile;

		return Pair.nullPair;
	}

	@SuppressWarnings("unchecked")
	private <T> Pair locate(T thing, boolean exact) {
		HashMap<Pair, T> mapThings;

		if (thing instanceof Tile)
			mapThings = (HashMap<Pair, T>) mapTiles;
		else if (thing instanceof MapObject)
			mapThings = (HashMap<Pair, T>) mapObjects;
		else
			return Pair.nullPair;

		if (!mapThings.containsValue(thing))
			return Pair.nullPair;

		for (Entry<Pair, T> entry : mapThings.entrySet())
			if (exact) {
				if (entry.getValue() == thing)
					return entry.getKey();
			} else {
				if (entry.getValue().equals(thing))
					return entry.getKey();
			}

		return Pair.nullPair;
	}

	public Pair locateTile(int id) {
		return locate(TileProp.tiles.get(id), false);
	}

	public Pair locateObject(int id) {
		return locate(ObjectProp.getObject(id), false);
	}

	public Pair locateObject(MapObject object) {
		return locate(object, true);
	}

	public Pair locateTile(Tile tile) {
		return locate(tile, true);
	}

	public Pair locateDiggableTile() {
		List<Entry<Pair, Tile>> entryList = new ArrayList<>();
		Iterator<Entry<Pair, Tile>> it = mapTiles.entrySet().iterator();
		Entry<Pair, Tile> tileEntry;
		while (it.hasNext())
			if ((tileEntry = it.next()).getValue().id != defTile)
				entryList.add(tileEntry);
		Collections.shuffle(entryList);
		Tile tile;
		for (Entry<Pair, Tile> entry : entryList) {
			tile = entry.getValue();
			if (tile.rollDrop(Tool.Type.Shovel) != -1)
				return entry.getKey();
		}
		return Pair.nullPair;
	}

	public Pair locateObject(Skill skill) {
		for (Entry<Pair, MapObject> entry : mapObjects.entrySet()) {
			if (Functional.test(entry.getValue().as(ResourceObject.class),
					resourceObj -> resourceObj.skill == skill))
				return entry.getKey();
		}
		return Pair.nullPair;
	}

	public Stream<AbstractNpc> pickNpcs(Predicate<AbstractNpc> predicate) {
		return mapNpcs.values().stream().filter(predicate);
	}

	public float getOccupiedObjectSpace() {
		return ((float) getObjectCount() / (float) getMaxObjects()) * 100f;
	}

	/*
	 * Get maximum count of objects on map by its size
	 */
	private float MAX_OBJECT_PERCENT = 0.125f; // Max percent of map allowed to be filled with objects
	private float MAX_NPC_PERCENT = 0.02f;

	private int maxObjects = -1;

	@JsonIgnore
	public int getMaxObjects() {
		if (maxObjects == -1) {
			maxObjects = (int) (getArea() * MAX_OBJECT_PERCENT);
			Utils.out("Max objects for current location: " + maxObjects);
		}

		return maxObjects;
	}

	private int maxNpcs = -1;

	@JsonIgnore
	public int getMaxNpcs() {
		if (maxNpcs == -1) {
			maxNpcs = (int) (getArea() * MAX_NPC_PERCENT);
			Utils.out("Max auto-spawned npcs: " + maxNpcs);
		}

		return maxNpcs;
	}

	public void rollObjects(RollList objectRollList) {
		ArrayList<Integer> objectIdList;
		int listSize;

		for (int i = 0; i < objectRollList.getRollCount(getScaleFactor()); ++i) {
			objectIdList = objectRollList.idList;
			listSize = objectIdList.size();
			randPlaceObject(objectIdList.get(Utils.random.nextInt(listSize)));
		}
	}

	@JsonIgnore
	public TimeScheduler getTimeScheduler() {
		return timeScheduler;
	}
}
