package hitonoriol.madsand;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.containers.PairFloat;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.enums.GameState;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.world.World;

import java.io.File;
import java.io.PrintStream;

public class MadSand extends Game {
	public static String VER = "";

	static final int OBJECT_LOOT = 7;
	public static final int TILESIZE = 33;
	static final int WORLDSIZE = 10;

	static int renderradius;

	static final String SAVE_EXT = ".msf";

	public static final String SAVEDIR = "MadSand_Saves/";
	static String QUESTFILE = SAVEDIR + "quests.json";
	static String GENFILE = SAVEDIR + "worldgen.json";
	static String TUTORIALFILE = SAVEDIR + "tutorial.json";
	static String TRADELISTFILE = SAVEDIR + "tradelists.json";

	static String TILEFILE = SAVEDIR + "tiles.json";
	static String OBJECTFILE = SAVEDIR + "objects.json";
	static String PRODSTATIONFILE = SAVEDIR + "productionstations.json";
	static String NPCFILE = SAVEDIR + "npcs.json";
	static String ITEMSFILE = SAVEDIR + "items.json";
	public static String GLOBALSFILE = SAVEDIR + "globals.json";
	public static String BUILDRECIPE = SAVEDIR + "buildrecipes.json";

	static String SKILLFILE = SAVEDIR + "defskills.xml";
	public static final String MAPDIR = SAVEDIR + "worlds/";
	public static final String SCRIPTDIR = SAVEDIR + "scripts/";
	static final String PLAYERFILE = "/Player" + SAVE_EXT;
	static final String LOGFILE = "/log" + SAVE_EXT;
	static final String NPCSFILE = "NPCs";
	static final String WORLDFILE = "/World" + SAVE_EXT;
	static final String ERRFILE = "MadSandCritical.log";
	public static final String ENCOUNTERDIR = "encounter/";

	public static final int XDEF = 1280;
	public static final int YDEF = 720;

	public static String WORLDNAME = "";

	public static final float DEFAULT_ZOOM = 1.5F;
	public static float ZOOM = DEFAULT_ZOOM;
	static final int DEFAULT_FOV = 12;

	public static boolean isWorldUntouched = true; // flag for once-per-launch actions

	static OrthographicCamera camera;
	public static int camxoffset = 17;
	public static int camyoffset = 37;
	private float elapsedTime;// For player animation

	public static GameState state = GameState.LAUNCHER;
	public static int MAXSAVESLOTS = 10;

	public static World world;

	private static PairFloat[] renderArea;
	private static final int TEST_POINT = 50;

	static float ymid;
	static float xmid = ymid = 0;
	static float ymenu;
	static float xmenu = ymenu = xmid;
	private static float menuXStep = 0.8f, menuYStep = 0f;
	private static float menuOffset = 250;

	public static Game game;

	public void create() {
		game = this;
		Gdx.graphics.setContinuousRendering(false);
		Utils.out("Starting initialization!");

		setRenderRadius(DEFAULT_FOV);
		setRenderRadius();
		Utils.out("Render area: " + renderArea.length);
		setErrFile();
		Utils.init();
		Gui.init();

		camera = new OrthographicCamera();
		camera.viewportWidth = (Gdx.graphics.getWidth() / ZOOM);
		camera.viewportHeight = (Gdx.graphics.getHeight() / ZOOM);
		camera.update();

		initNewGame();
		world.generate();

		initMenuAnimation();

		Utils.out("End of initialization!");

	}

	public static void initNewGame() {
		world = new World(MadSand.WORLDSIZE);
		World.player.updCoords();
		LuaUtils.init();
		Gui.overlay.gameLog.clear();
	}

	public static void switchScreen(Screen screen) {
		game.setScreen(screen);
	}

	public static void switchStage(GameState state, Stage stage) {

		if (state != GameState.INVENTORY)
			World.player.hideInventory();

		if (Gui.gameUnfocused)
			Gui.overlay.gameContextMenu.setVisible(false);

		if (state == GameState.GAME)
			Gui.overlay.gameTooltip.setVisible(true);

		if (state == GameState.INVENTORY)
			World.player.showInventory();

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
		while (i < World.DEFAULT_MAPSIZE + World.BORDER) {
			while (ii < World.DEFAULT_MAPSIZE + World.BORDER) {
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
		while (i < World.DEFAULT_MAPSIZE + World.BORDER) {
			while (ii < World.DEFAULT_MAPSIZE + World.BORDER) {
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

	public void updateCamToxy(float f, float y2) {
		camera.position.set(f + camxoffset, y2 + camyoffset, 0.0F);

		camera.viewportWidth = (Gdx.graphics.getWidth() / ZOOM);
		camera.viewportHeight = (Gdx.graphics.getHeight() / ZOOM);
		camera.update();

		Mouse.mouseinworld.set(Gdx.input.getX(), Gdx.input.getY(), 0.0F);
		camera.unproject(Mouse.mouseinworld);
		Utils.batch.setProjectionMatrix(camera.combined);
	}

	void drawGame() {
		Map loc = world.getCurLoc();
		Npc npc;
		Tile tile;
		Player player = World.player;
		int objid;

		int x, y;
		int xsz = loc.getWidth(), ysz = loc.getHeight();
		int i = 0;

		if (player.isInBackground()) // Draw player under tiles & objeccts if he is currently in the background
			drawEntity(player);

		while (i < renderArea.length) { // Render background tiles
			x = World.player.x + (int) renderArea[i].x;
			y = World.player.y + (int) renderArea[i].y;

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

		while (i < renderArea.length) { // Render objects & entities
			x = World.player.x + (int) renderArea[i].x;
			y = World.player.y + (int) renderArea[i].y;

			tile = loc.getTile(x, y);

			if ((x > xsz || y > ysz || x < 0 || y < 0) && MadSand.world.isUnderGround()) {
				++i;
				continue;
			}

			if (!tile.visible && !tile.visited) { // Don't render objects/entities on tiles which were never seen by player
				++i;
				continue;
			} else if (tile.visited && !tile.visible) { // Render visited & not currently visible tiles partially darkened
				Utils.batch.draw(Resources.visitedMask, x * TILESIZE, y * TILESIZE);
				++i;
				continue;
			}

			if (player.standingOnLoot(x, y))
				Utils.batch.draw(Resources.objects[OBJECT_LOOT], x * TILESIZE, y * TILESIZE);

			npc = loc.getNpc(x, y);
			if (npc != Map.nullNpc)
				drawEntity(npc);

			objid = loc.getObject(x, y).id;

			if ((objid != MapObject.NULL_OBJECT_ID) && (objid != MapObject.COLLISION_MASK_ID))
				Utils.batch.draw(Resources.objects[objid], x * TILESIZE, y * TILESIZE);

			++i;
		}

		if (!player.isInBackground())
			drawEntity(player);

		if (!Gui.gameUnfocused) {
			Utils.batch.draw(Resources.mapcursor, Mouse.wx * TILESIZE, Mouse.wy * TILESIZE);
			Utils.batch.end();
			Utils.batch.begin();
		}

	}

	private void initMenuAnimation() {
		xmenu = xmid = World.player.x * TILESIZE;
		ymenu = ymid = World.player.y * TILESIZE;
	}

	private float cameraBounce(float n) {
		float ret = (float) -(n);
		return ret;
	}

	private void drawMenuBackground() {

		if (xmenu > (xmid + menuOffset))
			menuXStep = cameraBounce(menuXStep);

		if (ymenu > (ymid + menuOffset))
			menuYStep = cameraBounce(menuYStep);

		if (xmenu < (xmid - menuOffset))
			menuXStep = cameraBounce(menuXStep);

		if (ymenu < (ymid - menuOffset))
			menuYStep = cameraBounce(menuYStep);

		ymenu += menuYStep;
		xmenu += menuXStep;

		drawGame();
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
					updateCamToxy(drawx, drawy);
			} else
				Utils.batch.draw(entity.getSprite(), drawx, drawy);

			entity.stepx -= entity.movespeed;
			entity.stepy -= entity.movespeed;

			if (entity.stepx <= 1) {
				entity.setStepping(false);
				entity.stepx = TILESIZE;
				entity.stepy = TILESIZE;
			}

		} else {
			Utils.batch.draw(entity.getSprite(), entity.globalPos.x, entity.globalPos.y);

			if ((entity instanceof Player) && (state == GameState.GAME))
				updateCamToxy(entity.globalPos.x, entity.globalPos.y);
		}
	}

	static int repeat = 1;
	static int li;
	static String oldarg = "";

	public static void print(String arg) {
		Label[] log = Gui.overlay.getLogLabels();
		if (!oldarg.equals(arg)) {
			repeat = 1;
			oldarg = arg;
			int i = log.length - 1;
			while (i >= 0) {

				if (i != 0)
					log[i].setText(log[i - 1].getText());
				else {
					log[i].setText(arg);
					li = i;
				}

				i--;
			}
		} else
			log[li].setText(oldarg + " x" + (++repeat));
	}

	public static void print(String msg, String color) {
		print(color + msg + "[]");
	}

	public static void notice(String msg) {
		print("* " + msg, Gui.noticeMsgColor);
	}

	public void render() {
		Keyboard.pollScreenshotKey();

		if (state.equals(GameState.GAME)) {

			Mouse.mouseinworld.set(Gdx.input.getX(), Gdx.input.getY(), 0.0F);
			camera.unproject(Mouse.mouseinworld);
			Gui.overlay.pollGameConsole();
			Keyboard.pollStatWindowKey();
			if (Gui.overlay.getKeyboardFocus() != Gui.overlay.getConsoleField() && !Gui.gameUnfocused) {
				Mouse.updCoords();
				Mouse.mouseClickAction();
				Keyboard.pollGameKeys();
			}
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			Utils.batch.begin();
			drawGame();
			Gui.overlay.act();
			Gui.overlay.draw();
			Utils.batch.end();
			super.render();
		} else if (state.equals(GameState.INVENTORY)) {
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			Utils.batch.begin();
			drawGame();
			Utils.batch.end();
			Mouse.mouseinworld.set(Gdx.input.getX(), Gdx.input.getY(), 0.0F);
			Keyboard.pollInventoryKey();

			Gui.overlay.act();
			Gui.overlay.draw();

		} else if (state.equals(GameState.TRADE)) {
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			Utils.batch.begin();
			drawGame();
			Utils.batch.end();

			Gui.overlay.act();
			Gui.overlay.draw();

		} else if (state.equals(GameState.NMENU)) {
			if (!Gdx.graphics.isContinuousRendering())
				Gdx.graphics.setContinuousRendering(true);
			Gdx.gl.glClearColor(1.0F, 1.0F, 1.0F, 1.0F);
			Gdx.gl.glClear(16384);
			updateCamToxy(xmenu, ymenu);
			camera.update();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			Utils.batch.begin();
			drawMenuBackground();
			Utils.batch.end();
			Gui.mainMenu.act();
			Gui.mainMenu.draw();
		} else if (state.equals(GameState.WORLDGEN)) {
			Utils.batch.begin();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			updateCamToxy(World.player.globalPos.x, World.player.globalPos.y);
			drawMenuBackground();
			Gui.worldGenStage.draw();
			Utils.batch.end();
		} else if (state.equals(GameState.LOAD)) {
			Utils.batch.begin();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			drawMenuBackground();
			Gui.loadWorldStage.draw();
			Utils.batch.end();
		} else if (state.equals(GameState.GOT)) {
			Utils.batch.begin();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			drawMenuBackground();
			//Gui.sectorChangeStage.draw();
			//Gui.sectorChangeStage.act();
			Utils.batch.end();
		} else if (state.equals(GameState.DEAD)) {
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Utils.batch.begin();
			drawGame();
			Utils.batch.end();
			Gui.deathStage.act();
			Gui.deathStage.draw();
		} else if (state.equals(GameState.MSG)) {
			camera.position.set(0.0F, 0.0F, 0.0F);
			Utils.batch.setProjectionMatrix(camera.combined);
			camera.update();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			Utils.batch.begin();
			drawGame();
			Utils.batch.end();
		} else if (state.equals(GameState.CRAFT)) {
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
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
		Gui.overlay.getViewport().update(width, height, true);
		Gui.overlay.getViewport().setScreenSize(width, height);
	}

	public void pause() {

	}

	public void dispose() {
		Utils.out("End");
	}

	void setErrFile() {
		try {
			PrintStream ge = new PrintStream(new File(ERRFILE));
			System.setErr(ge);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}