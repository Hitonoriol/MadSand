package hitonoriol.madsand;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.containers.AnimationContainer;
import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.containers.PairFloat;
import hitonoriol.madsand.dialog.GameTextSubstitutor;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.enums.GameState;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.world.World;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class MadSand extends Game {
	static final int OBJECT_LOOT = 7;
	public static final int TILESIZE = 33;

	static int renderradius;

	public static final String SAVE_EXT = ".msf";

	public static final String SAVEDIR = "MadSand_Saves/";
	public static final String MAPDIR = SAVEDIR + "worlds/";
	public static final String PLAYERFILE = "/Player" + SAVE_EXT;
	public static final String LOGFILE = "/log" + SAVE_EXT;
	public static final String NPCSFILE = "NPCs";
	public static final String WORLDFILE = "/World" + SAVE_EXT;
	public static String WORLDNAME = "";

	public static final float DEFAULT_ZOOM = 1.5F;
	public static float ZOOM = DEFAULT_ZOOM;
	static final int DEFAULT_FOV = 12;

	public static boolean isWorldUntouched = true; // flag for once-per-launch actions

	public static OrthographicCamera camera;
	public static int camxoffset = 17;
	public static int camyoffset = 37;
	private float elapsedTime;// For player animation

	public static GameState state = GameState.LAUNCHER;
	public static int MAXSAVESLOTS = 10;

	private static ConcurrentHashMap<PairFloat, AnimationContainer> animations = new ConcurrentHashMap<>();

	public static World world;

	private static PairFloat[] renderArea;
	private static final int TEST_POINT = 50;

	static float ymid;
	static float xmid = ymid = 0;
	static float cameraY;
	static float cameraX = cameraY = xmid;
	private static float menuXStep = 0.8f, menuYStep = 0f;
	private static float menuOffset = 250;

	public static Game game;

	public void create() {
		Utils.out("Starting initialization!");
		game = this;
		Gdx.graphics.setContinuousRendering(false);

		Timer.instance().start();

		setRenderRadius(DEFAULT_FOV);
		setRenderRadius();
		Utils.out("Render area: " + renderArea.length);
		Utils.init();
		Gui.init();
		GameTextSubstitutor.init();
		Keyboard.initKeyListener();

		initNewGame();
		world.generate();

		initMenuAnimation();
		camera = new OrthographicCamera();
		setViewport();
		updateCamPosition();

		Utils.out("End of initialization!");

	}

	public static void initNewGame() {
		world = new World();
		World.player.updCoords();
		LuaUtils.init();
		Gui.overlay.gameLog.clear();
	}

	public static void switchScreen(Screen screen) {
		game.setScreen(screen);
	}

	public static void switchStage(GameState state, Stage stage) {
		if (Gui.gameUnfocused)
			Gui.overlay.gameContextMenu.setVisible(false);

		else if (state == GameState.GAME)
			Gui.overlay.gameTooltip.setVisible(true);

		Gdx.input.setInputProcessor(stage);
		MadSand.state = state;
	}

	public static void reset() {
		switchScreen(null);
		switchStage(GameState.GAME, Gui.overlay);
	}

	static int countRcells() {
		int i = 0;
		int ii = 0, cl = 0;
		while (i < World.DEFAULT_MAPSIZE) {
			while (ii < World.DEFAULT_MAPSIZE) {
				if (Line.calcDistance(TEST_POINT * TILESIZE, TEST_POINT * TILESIZE, i * TILESIZE,
						ii * TILESIZE) <= renderradius) {
					cl++;
				}
				ii++;
			}
			ii = 0;
			i++;
		}
		return cl;
	}

	private static PairFloat[] getAllRcells(PairFloat[] cl) {
		int i = 0;
		int ii = 0, clc = 0;
		while (i < World.DEFAULT_MAPSIZE) {
			while (ii < World.DEFAULT_MAPSIZE) {
				if (Line.calcDistance(TEST_POINT * TILESIZE, TEST_POINT * TILESIZE, i * TILESIZE,
						ii * TILESIZE) <= renderradius) {
					cl[clc] = new PairFloat(TEST_POINT - ii, TEST_POINT - i);
					clc++;
				}
				ii++;
			}
			ii = 0;
			i++;
		}
		return cl;
	}

	static void setRenderRadius() {
		renderArea = new PairFloat[countRcells()];
		renderArea = getAllRcells(renderArea);
	}

	public static void setRenderRadius(int radius) {
		renderradius = radius * MadSand.TILESIZE;
		setRenderRadius();
	}

	public static void setViewport() {
		camera.viewportWidth = (Gdx.graphics.getWidth() / ZOOM);
		camera.viewportHeight = (Gdx.graphics.getHeight() / ZOOM);
		camera.update();
	}

	public static void setCamPosition(float x, float y) {
		cameraX = x;
		cameraY = y;
	}

	public static void updateCamPosition(float x, float y) {
		camera.position.set(x + camxoffset, y + camyoffset, 0.0F);
		Utils.batch.setProjectionMatrix(camera.combined);
		camera.update();
	}

	public static void updateCamPosition() {
		updateCamPosition(cameraX, cameraY);
	}

	private void renderObject(MapObject object, int x, int y) {
		x *= TILESIZE;
		y *= TILESIZE;

		if (object.centered)
			x -= object.getRenderOffset();

		if ((object.id != MapObject.NULL_OBJECT_ID) && (object.id != MapObject.COLLISION_MASK_ID))
			Utils.batch.draw(Resources.objects[object.id], x, y);
	}

	public static void queueAnimation(AnimationContainer animation, float x, float y) {
		animations.put(new PairFloat(x, y), animation);
	}

	public static void drawAnimations() {
		if (animations.isEmpty())
			return;

		PairFloat coords;
		AnimationContainer animation;

		for (Entry<PairFloat, AnimationContainer> anim : animations.entrySet()) {
			coords = anim.getKey();
			animation = anim.getValue();
			Utils.batch.draw(animation.getCurrentKeyFrame(), coords.x, coords.y);

			if (animation.isAnimationFinished())
				animations.remove(coords);
		}
	}

	void drawLoot(int x, int y) {
		Loot loot = world.getCurLoc().getLoot(x, y);
		if (loot.contents.size() == 1)
			Utils.batch.draw(Resources.item[loot.contents.get(0).id], x * TILESIZE, y * TILESIZE, TILESIZE, TILESIZE);
		else
			Utils.batch.draw(Resources.objects[OBJECT_LOOT], x * TILESIZE, y * TILESIZE);
	}

	void drawGame() {
		Map loc = world.getCurLoc();
		Npc npc;
		Tile tile;
		MapObject object;
		Player player = World.player;
		boolean tileVisited;

		int x, y;
		int xsz = loc.getWidth(), ysz = loc.getHeight();
		int i = 0;

		if (player.isInBackground()) // Draw player under tiles & objects if he is currently in the background
			drawEntity(player);

		while (i < renderArea.length) { // Render background tiles
			x = player.x + (int) renderArea[i].x;
			y = player.y + (int) renderArea[i].y;

			tile = loc.getTile(x, y);

			if (!tile.visible && !tile.visited) { //Don't render tiles which were never seen
				++i;
				continue;
			}

			if ((x > xsz || y > ysz || x < 0 || y < 0) && MadSand.world.isUnderGround()) {// Don't render default tile while underground
				++i;
				continue;
			}

			Utils.batch.draw(Resources.tile[world.getTileOrDefault(x, y)], x * TILESIZE, y * TILESIZE);
			++i;
		}

		i = 0;

		while (i < renderArea.length) { // Render objects, loot, NPCs and player
			x = player.x + (int) renderArea[i].x;
			y = player.y + (int) renderArea[i].y;

			tile = loc.getTile(x, y);
			object = loc.getObject(x, y);
			npc = loc.getNpc(x, y);
			tileVisited = tile.visited && !tile.visible;

			if ((x > xsz || y > ysz || x < 0 || y < 0) && MadSand.world.isUnderGround()) {
				++i;
				continue;
			}

			if (!tile.visible && !tile.visited) { //Don't render anything on tiles which were never seen
				++i;
				continue;
			}

			if (tileVisited && object.isWall) // If object is a wall, it'll be rendered even when not visible
				renderObject(object, x, y);

			if (tileVisited) { // Render visited & not currently visible tiles partially darkened
				Utils.batch.draw(Resources.visitedMask, x * TILESIZE, y * TILESIZE);
				++i;
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

			++i;
		}

		drawAnimations();

		if (!Gui.gameUnfocused && state.equals(GameState.GAME)) {
			Utils.batch.draw(Resources.mapcursor, Mouse.wx * TILESIZE, Mouse.wy * TILESIZE);
			Utils.batch.end();
			Utils.batch.begin();
		}

	}

	private void initMenuAnimation() {
		cameraX = xmid = World.player.x * TILESIZE;
		cameraY = ymid = World.player.y * TILESIZE;
	}

	private float cameraBounce(float n) {
		float ret = (float) -(n);
		return ret;
	}

	private void drawMenuBackground() {

		if (cameraX > (xmid + menuOffset))
			menuXStep = cameraBounce(menuXStep);

		if (cameraY > (ymid + menuOffset))
			menuYStep = cameraBounce(menuYStep);

		if (cameraX < (xmid - menuOffset))
			menuXStep = cameraBounce(menuXStep);

		if (cameraY < (ymid - menuOffset))
			menuYStep = cameraBounce(menuYStep);

		cameraY += menuYStep;
		cameraX += menuXStep;

		drawGame();
		Gdx.graphics.requestRendering();
	}

	void drawEntity(Entity entity) {
		if (entity.isStepping()) {
			Direction dir = entity.stats.look;
			float drawx, drawy;
			drawx = entity.globalPos.x;
			drawy = entity.globalPos.y;

			if (dir == Direction.RIGHT)
				drawx = entity.globalPos.x - entity.stepx;
			else if (dir == Direction.LEFT)
				drawx = entity.globalPos.x + entity.stepx;
			else if (dir == Direction.UP)
				drawy = entity.globalPos.y - entity.stepy;
			else
				drawy = entity.globalPos.y + entity.stepy;

			if (entity instanceof Player) {
				Animation<TextureRegion> anim = null;
				this.elapsedTime += Gdx.graphics.getDeltaTime();

				if (dir == Direction.RIGHT)
					anim = Resources.ranim;
				else if (dir == Direction.LEFT)
					anim = Resources.lanim;
				else if (dir == Direction.UP)
					anim = Resources.uanim;
				else
					anim = Resources.danim;

				Utils.batch.draw((TextureRegion) anim.getKeyFrame(this.elapsedTime, true), drawx, drawy);

				if (state == GameState.GAME)
					setCamPosition(drawx, drawy);
			} else
				Utils.batch.draw(entity.getSprite(), drawx, drawy);

			entity.animateMovement();
		} else {
			Utils.batch.draw(entity.getSprite(), entity.globalPos.x, entity.globalPos.y);

			if ((entity instanceof Player) && (state == GameState.GAME))
				setCamPosition(entity.globalPos.x, entity.globalPos.y);
		}
	}

	public static void warn(String msg) {
		Gui.overlay.gameLog.warn(msg);
	}

	public static void print(String arg) {
		Gui.overlay.gameLog.print(arg);
	}

	public static void print(String msg, String color) {
		Gui.overlay.gameLog.print(msg, color);
	}

	public static void notice(String msg) {
		Gui.overlay.gameLog.notice(msg);
	}

	public void render() {
		Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Keyboard.pollGlobalHotkeys();

		if (state.equals(GameState.GAME)) {
			Mouse.mouseinworld.set(Gdx.input.getX(), Gdx.input.getY(), 0.0F);
			camera.unproject(Mouse.mouseinworld);
			Gui.overlay.pollGameConsole();
			if (Gui.overlay.getKeyboardFocus() != Gui.overlay.getConsoleField() && !Gui.isGameUnfocused()) {
				Mouse.updCoords();
				Mouse.pollMouseMovement();
				Keyboard.pollGameKeys();
			}
			Utils.batch.begin();
			drawGame();
			updateCamPosition();
			Gui.overlay.act();
			Gui.overlay.draw();
			Utils.batch.end();
			super.render();
		} else if (state.equals(GameState.TRADE)) {
			Utils.batch.begin();
			drawGame();
			Utils.batch.end();
			Gui.overlay.act();
			Gui.overlay.draw();
		} else if (state.equals(GameState.NMENU)) {
			updateCamPosition(cameraX, cameraY);
			camera.update();
			Utils.batch.begin();
			drawMenuBackground();
			Utils.batch.end();
			Gui.mainMenu.act();
			Gui.mainMenu.draw();
		} else if (state.equals(GameState.DEAD)) {
			Utils.batch.begin();
			drawGame();
			Utils.batch.end();
			Gui.deathStage.act();
			Gui.deathStage.draw();
		} else if (state.equals(GameState.CRAFT)) {
			Utils.batch.begin();
			drawGame();
			Utils.batch.end();
			Gui.craftMenu.act();
			Gui.craftMenu.draw();
		}
	}

	// New world is generated on game launch, so this thing ensures that new world won't be generated twice when you press "New Game" button
	public static void worldEntered() {
		if (MadSand.isWorldUntouched)
			MadSand.isWorldUntouched = false;

		world.enter();
	}

	public static void setWorldName(String arg) {
		WORLDNAME = arg;
	}

	public void resume() {

	}

	public void resize(int width, int height) {
		setViewport();
		Gui.overlay.getViewport().update(width, height, true);
		Gui.overlay.updateWidgetPositions();
	}

	public void pause() {

	}

	public void dispose() {
		Utils.out("End");
	}
}