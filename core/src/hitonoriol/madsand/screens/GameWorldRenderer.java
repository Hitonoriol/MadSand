package hitonoriol.madsand.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.AnimationContainer;
import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.containers.PairFloat;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.input.Mouse;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.pathfinding.Path;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.resources.TextureMap;
import hitonoriol.madsand.util.Utils;

public class GameWorldRenderer {
	static final int TILESIZE = MadSand.TILESIZE;
	public static final float DEFAULT_ZOOM = 1.5F;
	static final int OBJECT_LOOT = 7;
	final int camxoffset = 17, camyoffset = 37;

	private float cameraX, cameraY;
	private boolean followPlayer;
	private float zoom = DEFAULT_ZOOM;

	private SpriteBatch batch = new SpriteBatch();
	private OrthographicCamera camera = new OrthographicCamera();
	private Light light = new Light();
	private ConcurrentHashMap<PairFloat, AnimationContainer> animations = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Path, PathDescriptor> paths = new ConcurrentHashMap<>();
	private List<Pair> renderArea = new ArrayList<>();

	private static TextureRegion mapCursor = Resources.getTexture("misc/map_cursor");

	public GameWorldRenderer() {
		updateViewport();
	}

	public void setRenderRadius(int radius) {
		final int center = radius, diameter = radius * 2;
		renderArea = new ArrayList<>();
		for (int y = 0; y < diameter; ++y) {
			for (int x = 0; x < diameter; ++x) {
				if (Line.calcDistance(center, center, x, y) <= radius)
					renderArea.add(new Pair(center - x, center - y));
			}
		}
	}

	private void renderObject(MapObject object, float x, float y) {
		x *= TILESIZE;
		y *= TILESIZE;

		if (object.centered)
			x -= object.getRenderOffset();

		TextureRegion texture = object.getTexture();
		float w = texture.getRegionWidth(), h = texture.getRegionHeight();
		if ((object.id != MapObject.NULL_OBJECT_ID) && (object.id != MapObject.COLLISION_MASK_ID))
			batch.draw(texture, x, y, w / 2f, h / 2f, w, h, 1f, 1f,
					object.getDirection().getRotation());
	}

	public void queueAnimation(AnimationContainer animation, float x, float y) {
		animations.put(new PairFloat(x, y), animation);
	}

	public void queuePath(Path path, float duration, Color color) {
		paths.put(path, new PathDescriptor(duration, color));
	}

	public void queuePath(Path path, Color color) {
		queuePath(path, Float.MAX_VALUE, color);
	}

	public void queuePath(Path path) {
		paths.put(path, PathDescriptor.DEFAULT);
	}

	public void removePath(Path path) {
		if (path != null)
			paths.remove(path);
	}

	public boolean isPathQueued(Path path) {
		return paths.containsKey(path);
	}

	private void drawPaths() {
		paths.forEach((path, duration) -> {
			PathDescriptor pInfo = paths.get(path);
			drawPath(path, pInfo.color);

			if (pInfo.noTimeLimit())
				return;

			float time = pInfo.render(Gdx.graphics.getDeltaTime());

			if (time <= 0)
				paths.remove(path);
		});
	}

	private void drawAnimations() {
		if (animations.isEmpty())
			return;

		animations.forEach((coords, animation) -> {
			batch.draw(animation.getCurrentKeyFrame(), coords.x, coords.y);

			if (animation.isAnimationFinished())
				animations.remove(coords);
		});
	}

	private static final float LOOT_SIZE = TILESIZE * 0.85f;
	private static final float LOOT_OFFSET = (TILESIZE - LOOT_SIZE) * 0.5f;

	private void drawLoot(float x, float y) {
		Loot loot = MadSand.world().getCurLoc().getLoot((int) x, (int) y);

		x = x * TILESIZE + LOOT_OFFSET;
		y = y * TILESIZE + LOOT_OFFSET;

		if (loot.getItemCount() == 1)
			batch.draw(loot.get(0).getTexture(),
					x, y, LOOT_SIZE, LOOT_SIZE);
		else
			batch.draw(Resources.getObject(OBJECT_LOOT),
					x, y, LOOT_SIZE, LOOT_SIZE);

	}

	private void drawEntity(Entity entity) {
		PairFloat drawPos = entity.getWorldPos();
		if (entity.isMoving())
			entity.animateMovement();

		batch.draw(entity.getSprite(), drawPos.x, drawPos.y);

		if (followPlayer && entity instanceof Player)
			setCamPosition(drawPos.x, drawPos.y);
	}

	private static Color defColor = new Color(1, 1, 1, 1);

	public void drawPath(Path path, Color color) {
		batch.setColor(color);
		path.forEach(cell -> batch.draw(GameWorldRenderer.mapCursor, cell.x * TILESIZE, cell.y * TILESIZE));
		batch.setColor(defColor);
	}

	public void drawPath(Path path) {
		drawPath(path, defColor);
	}

	private void drawMapCursor() {
		int x = Mouse.wx, y = Mouse.wy;
		if (!Mouse.hasClickAction())
			batch.draw(GameWorldRenderer.mapCursor, x * TILESIZE, y * TILESIZE);
		else
			drawPath(Mouse.getPathToCursor());
	}

	private void darkenTile(int x, int y, int dstToPlayer) {
		batch.draw(light.get(dstToPlayer), x * TILESIZE, y * TILESIZE);
	}

	private void drawGame() {
		Map map = MadSand.world().getCurLoc();
		AbstractNpc npc;
		Tile tile;
		MapObject object;
		Player player = MadSand.player();
		boolean tileVisited;
		int x, y;

		if (player.isInBackground()) // Draw player under tiles & objects if he is currently in the background
			drawEntity(player);

		for (Pair cell : renderArea) { // Render background tiles
			x = player.x + cell.x;
			y = player.y + cell.y;

			tile = map.getTile(x, y);

			if (!tile.visible && !tile.visited) //Don't render tiles which were never seen
				continue;

			if (!map.validCoords(x, y) && MadSand.world().isUnderGround()) // Don't render default tile while underground
				continue;

			batch.draw(Resources.getTile(MadSand.world().getTileOrDefault(x, y)), x * TILESIZE, y * TILESIZE);
		}

		for (Pair cell : renderArea) { // Render objects, loot, NPCs and player
			x = player.x + cell.x;
			y = player.y + cell.y;

			tile = map.getTile(x, y);
			object = map.getObject(x, y);
			npc = map.getNpc(x, y);
			tileVisited = tile.visited && !tile.visible;

			if (!map.validCoords(x, y) && MadSand.world().isUnderGround())
				continue;

			if (!tile.visible && !tile.visited) //Don't render anything on tiles which were never seen
				continue;

			if (tileVisited && object.isWall) // If object is a wall, it'll be rendered even when not visible
				renderObject(object, x, y);

			if (tileVisited) { // Render visited & not currently visible tiles partially darkened
				darkenTile(x, y, player.distanceTo(x, y));
				continue;
			}

			renderObject(object, x, y);

			if (player.standingOnLoot(x, y))
				drawLoot(x, y);

			if (npc != Map.nullNpc)
				drawEntity(npc);

			if (player.at(x, y) && !player.isInBackground())
				drawEntity(player);
		}

		drawAnimations();

		if (!Gui.isGameUnfocused())
			drawMapCursor();
		drawPaths();
	}

	public void render(float delta) {
		batch.begin();
		drawGame();
		batch.end();
		updateCamPosition();
	}

	public OrthographicCamera getCamera() {
		return camera;
	}

	public float getCamX() {
		return cameraX;
	}

	public float getCamY() {
		return cameraY;
	}

	public float getCamZoom() {
		return zoom;
	}

	public void setCamFollowPlayer(boolean follow) {
		followPlayer = follow;
	}

	public void moveCamera(float byX, float byY) {
		cameraX += byX;
		cameraY += byY;
	}

	public void changeZoom(float by) {
		zoom += by;
		updateViewport();
	}
	
	public void setZoom(float val) {
		zoom = val;
		updateViewport();
	}

	public void setCamPosition(float x, float y) {
		cameraX = x;
		cameraY = y;
	}

	public void updateCamPosition(float x, float y) {
		camera.position.set(x + camxoffset, y + camyoffset, 0.0F);
		batch.setProjectionMatrix(camera.combined);
		camera.update();
	}

	public void setWorldCamPosition(int x, int y) {
		updateCamPosition(x * TILESIZE, y * TILESIZE);
	}

	public void updateCamPosition() {
		updateCamPosition(cameraX, cameraY);
	}

	public void updateViewport() {
		camera.viewportWidth = Gdx.graphics.getWidth() / zoom;
		camera.viewportHeight = Gdx.graphics.getHeight() / zoom;
		camera.update();
	}

	private static class Light extends TextureMap<Integer> {
		private final static float LIGHT_START = 0.16f, ALPHA_STEP = 0.055f;
		private final static int LIGHT_LEVELS = (int) ((1f - LIGHT_START) / ALPHA_STEP);
		private final static java.util.Map<Integer, Integer> lightMap = new HashMap<>();

		/* How often <n> repeats */
		private final static IntUnaryOperator lightLevelFunction = n -> (int) Math.log(1.2 * n);
		private final static String REGION = "light";

		private Light() {
			super(Resources.getAtlas(), REGION);
			createLightLevels();
			int lightEntries = IntStream.range(1, LIGHT_LEVELS + 1)
					.map(lightLevelFunction)
					.sum();
			for (int dst = 1, lightLvl = 1, occurences = 0; dst <= lightEntries; ++dst) {
				Utils.dbg("Light dst=%d lvl=%d", dst, lightLvl);
				lightMap.put(dst, lightLvl);
				if (lightLvl < LIGHT_LEVELS && ++occurences >= lightLevelFunction.applyAsInt(lightLvl)) {
					++lightLvl;
					occurences = 0;
				}
			}
		}

		private void createLightLevels() {
			Color color = new Color(Color.BLACK);
			for (int lvl = 1; lvl <= LIGHT_LEVELS; ++lvl) {
				Pixmap light = new Pixmap(TILESIZE, TILESIZE, Format.RGBA8888);
				color.a = Math.min(LIGHT_START + ALPHA_STEP * (lvl - 1), 1f);
				Utils.dbg("Preparing light level %d alpha=%f", lvl, color.a);
				light.setColor(color);
				light.fill();
				atlas.addRegion(REGION + "/" + lvl, new TextureRegion(new Texture(light)));
			}
		}

		@Override
		public AtlasRegion get(Integer distanceToPlayer) {
			return super.get(lightMap.getOrDefault(distanceToPlayer, LIGHT_LEVELS));
		}
	}

	@SuppressWarnings("unused")
	private static class PathDescriptor {
		float duration = Float.MAX_VALUE;
		Color color = defColor;

		static PathDescriptor DEFAULT = new PathDescriptor();

		public PathDescriptor(float duration, Color color) {
			this.duration = duration;
			this.color = color;
		}

		public PathDescriptor(Color color) {
			this.color = color;
		}

		public PathDescriptor() {}

		boolean noTimeLimit() {
			return duration == Float.MAX_VALUE;
		}

		float render(float delta) {
			duration -= delta;
			return duration;
		}
	}
}
