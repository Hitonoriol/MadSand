package hitonoriol.madsand.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
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

public class GameWorldRenderer {
	static final int TILESIZE = MadSand.TILESIZE;
	public static final float DEFAULT_ZOOM = 1.5F;
	static final int OBJECT_LOOT = 7;
	final int camxoffset = 17, camyoffset = 37;

	private float cameraX, cameraY;
	private boolean followPlayer;
	private float zoom = DEFAULT_ZOOM;

	private OrthographicCamera camera = new OrthographicCamera();
	private ConcurrentHashMap<PairFloat, AnimationContainer> animations = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Path, PathDescriptor> paths = new ConcurrentHashMap<>();
	private List<Pair> renderArea = new ArrayList<>();

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

		Texture texture = object.getTexture();
		float w = texture.getWidth(), h = texture.getHeight();
		if ((object.id != MapObject.NULL_OBJECT_ID) && (object.id != MapObject.COLLISION_MASK_ID))
			MadSand.batch.draw(texture, x, y, w / 2f, h / 2f, w, h, 1f, 1f,
					object.getDirection().getRotation(),
					0, 0, (int) w, (int) h, false, false);
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

	void drawPaths() {
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

	void drawAnimations() {
		if (animations.isEmpty())
			return;

		animations.forEach((coords, animation) -> {
			MadSand.batch.draw(animation.getCurrentKeyFrame(), coords.x, coords.y);

			if (animation.isAnimationFinished())
				animations.remove(coords);
		});
	}

	private static final float LOOT_SIZE = TILESIZE * 0.85f;
	private static final float LOOT_OFFSET = (TILESIZE - LOOT_SIZE) * 0.5f;

	void drawLoot(float x, float y) {
		Loot loot = MadSand.world().getCurLoc().getLoot((int) x, (int) y);
		Texture lootTx;

		if (loot.getItemCount() == 1)
			lootTx = loot.get(0).getTexture();
		else
			lootTx = Resources.objects[OBJECT_LOOT];

		MadSand.batch.draw(lootTx, x * TILESIZE + LOOT_OFFSET, y * TILESIZE + LOOT_OFFSET, LOOT_SIZE, LOOT_SIZE);
	}

	void drawEntity(Entity entity) {
		PairFloat drawPos = entity.getWorldPos();
		if (entity.isMoving())
			entity.animateMovement();

		MadSand.batch.draw(entity.getSprite(), drawPos.x, drawPos.y);

		if (followPlayer && entity instanceof Player)
			setCamPosition(drawPos.x, drawPos.y);
	}

	private static Color defColor = new Color(1, 1, 1, 1);

	public void drawPath(Path path, Color color) {
		MadSand.batch.setColor(color);
		path.forEach(cell -> MadSand.batch.draw(Resources.mapCursor, cell.x * TILESIZE, cell.y * TILESIZE));
		MadSand.batch.setColor(defColor);
	}

	public void drawPath(Path path) {
		drawPath(path, defColor);
	}

	void drawMapCursor() {
		int x = Mouse.wx, y = Mouse.wy;
		if (!Mouse.hasClickAction())
			MadSand.batch.draw(Resources.mapCursor, x * TILESIZE, y * TILESIZE);
		else
			drawPath(Mouse.getPathToCursor());
	}

	void drawGame() {
		Map loc = MadSand.world().getCurLoc();
		AbstractNpc npc;
		Tile tile;
		MapObject object;
		Player player = MadSand.player();
		boolean tileVisited;

		int x, y;
		int xsz = loc.getWidth(), ysz = loc.getHeight();

		if (player.isInBackground()) // Draw player under tiles & objects if he is currently in the background
			drawEntity(player);

		for (Pair cell : renderArea) { // Render background tiles
			x = player.x + cell.x;
			y = player.y + cell.y;

			tile = loc.getTile(x, y);

			if (!tile.visible && !tile.visited) //Don't render tiles which were never seen
				continue;

			if ((x > xsz || y > ysz || x < 0 || y < 0) && MadSand.world().isUnderGround()) // Don't render default tile while underground
				continue;

			MadSand.batch.draw(Resources.tile[MadSand.world().getTileOrDefault(x, y)], x * TILESIZE, y * TILESIZE);
		}

		for (Pair cell : renderArea) { // Render objects, loot, NPCs and player
			x = player.x + cell.x;
			y = player.y + cell.y;

			tile = loc.getTile(x, y);
			object = loc.getObject(x, y);
			npc = loc.getNpc(x, y);
			tileVisited = tile.visited && !tile.visible;

			if ((x > xsz || y > ysz || x < 0 || y < 0) && MadSand.world().isUnderGround())
				continue;

			if (!tile.visible && !tile.visited) //Don't render anything on tiles which were never seen
				continue;

			if (tileVisited && object.isWall) // If object is a wall, it'll be rendered even when not visible
				renderObject(object, x, y);

			if (tileVisited) { // Render visited & not currently visible tiles partially darkened
				MadSand.batch.draw(Resources.visitedMask, x * TILESIZE, y * TILESIZE);
				continue;
			}

			renderObject(object, x, y);

			if (player.standingOnLoot(x, y))
				drawLoot(x, y);

			if (npc != Map.nullNpc)
				drawEntity(npc);

			if (x == player.x && y == player.y)
				if (!player.isInBackground())
					drawEntity(player);
		}

		drawAnimations();

		if (!Gui.gameUnfocused)
			drawMapCursor();
		drawPaths();
	}

	public void render(float delta) {
		MadSand.batch.begin();
		drawGame();
		MadSand.batch.end();
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

	public void setCamPosition(float x, float y) {
		cameraX = x;
		cameraY = y;
	}

	public void updateCamPosition(float x, float y) {
		camera.position.set(x + camxoffset, y + camyoffset, 0.0F);
		MadSand.batch.setProjectionMatrix(camera.combined);
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
