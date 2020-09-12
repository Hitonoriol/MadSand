package hitonoriol.madsand;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;

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

	static final String SAVEDIR = "MadSand_Saves/";
	static String QUESTFILE = SAVEDIR + "quests.json";
	static String GENFILE = SAVEDIR + "worldgen.json";
	static String TUTORIALFILE = SAVEDIR + "tutorial.json";
	static String TRADELISTFILE = SAVEDIR + "tradelists.json";

	static String TILEFILE = SAVEDIR + "tiles.json";
	static String OBJECTFILE = SAVEDIR + "objects.json";
	static String NPCFILE = SAVEDIR + "npcs.json";
	static String ITEMSFILE = SAVEDIR + "items.json";
	public static String GLOBALSFILE = SAVEDIR + "globals.json";

	static String SKILLFILE = SAVEDIR + "defskills.xml";
	public static final String MAPDIR = SAVEDIR + "worlds/";
	static final String SCRIPTDIR = SAVEDIR + "scripts/";
	static final String PLAYERFILE = "/Player" + SAVE_EXT;
	static final String NPCSFILE = "NPCs";
	static final String WORLDFILE = "/World" + SAVE_EXT;
	static final String ERRFILE = "MadSandCritical.log";

	public static final int XDEF = 1280;
	public static final int YDEF = 720;

	public static String WORLDNAME = "";

	public static final float DEFAULT_ZOOM = 1.5F;
	public static float ZOOM = DEFAULT_ZOOM;
	static final int DEFAULT_FOV = 12;

	public static boolean justStarted = true; // flag for once-per-launch actions

	static OrthographicCamera camera;
	public static int camxoffset = 17;
	public static int camyoffset = 37;
	private float elapsedTime;// For player animation

	public static GameState state = GameState.LAUNCHER;
	public static int MAXSAVESLOTS = 10;

	public static ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	public static TypeFactory typeFactory = mapper.getTypeFactory();

	public static World world;

	private static PairFloat[] renderArea;
	private static final int TEST_POINT = 50;

	static float ymid;
	static float xmid = ymid = TILESIZE * (World.MAPSIZE / 2);
	static float ymenu;
	static float xmenu = ymenu = xmid;
	private static float menuXStep = 0.8f, menuYStep = 0f;
	private static float menuOffset = 250;

	public static void switchStage(GameState state, Stage stage) {

		if (state != GameState.INVENTORY)
			World.player.hideInventory();

		if (Gui.gameUnfocused)
			Gui.gamecontext.setVisible(false);

		if (state == GameState.GAME)
			Gui.mousemenu.setVisible(true);

		if (state == GameState.INVENTORY)
			World.player.showInventory();

		Gdx.input.setInputProcessor(stage);
		MadSand.state = state;
	}

	static void reset() {
		switchStage(GameState.GAME, Gui.overlay);
	}

	public void create() {
		Utils.out("Starting initialization!");

		// Gdx.graphics.setContinuousRendering(false);
		mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		setRenderRadius(DEFAULT_FOV);
		setRenderRadius();
		Utils.out("Render area: " + renderArea.length);
		setErrFile();
		Utils.init();
		Gui.createBasicSkin();
		World.player = new Player();
		Gui.initmenu();
		camera = new OrthographicCamera();
		camera.update();

		world = new World(MadSand.WORLDSIZE);
		World.player.updCoords();
		world.generate();
		LuaUtils.init();
		Utils.out("End of initialization!");

		LuaUtils.executeScript(LuaUtils.initScript); // Init script for new worlds
	}

	static int countRcells() {
		int i = 0;
		int ii = 0, cl = 0;
		while (i < World.MAPSIZE + World.BORDER) {
			while (ii < World.MAPSIZE + World.BORDER) {
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
		while (i < World.MAPSIZE + World.BORDER) {
			while (ii < World.MAPSIZE + World.BORDER) {
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

			npc = loc.getNpc(x, y);
			if (npc != Map.nullNpc)
				drawEntity(npc);

			objid = loc.getObject(x, y).id;

			if ((objid != MapObject.NULL_OBJECT_ID) && (objid != MapObject.COLLISION_MASK_ID))
				Utils.batch.draw(Resources.objects[objid], x * TILESIZE, y * TILESIZE);

			if (player.standingOnLoot(x, y))
				Utils.batch.draw(Resources.objects[OBJECT_LOOT], x * TILESIZE, y * TILESIZE);

			++i;
		}

		if (!player.isInBackground())
			drawEntity(player);

		if (!Gui.gameUnfocused)
			Utils.batch.draw(Resources.mapcursor, Mouse.wx * TILESIZE, Mouse.wy * TILESIZE);

		Utils.batch.end();
		Utils.batch.begin();
	}

	float randSide(float n) {
		float ret = (float) -(n);
		return ret;
	}

	void drawMenuBackground() {

		if (xmenu > (xmid + menuOffset))
			menuXStep = randSide(menuXStep);

		if (ymenu > (ymid + menuOffset))
			menuYStep = randSide(menuYStep);

		if (xmenu < (xmid - menuOffset))
			menuXStep = randSide(menuXStep);

		if (ymenu < (ymid - menuOffset))
			menuYStep = randSide(menuYStep);

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

				if (((Player) entity).isMain && (state == GameState.GAME))
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

			if ((entity instanceof Player) && ((Player) entity).isMain && (state == GameState.GAME))
				updateCamToxy(entity.globalPos.x, entity.globalPos.y);
		}
	}

	static int repeat = 1;
	static int li;
	static String oldarg = "";

	public static void print(String arg) {
		if (!oldarg.equals(arg)) {
			repeat = 1;
			oldarg = arg;
			int i = Gui.log.length - 1;
			while (i >= 0) {
				if (i != 0)
					Gui.log[i].setText(Gui.log[i - 1].getText());
				else {
					Gui.log[i].setText(arg);
					li = i;
				}
				i--;
			}
		} else
			Gui.log[li].setText(oldarg + " x" + (++repeat));
	}

	public static void print(String msg, String color) {
		print(color + msg + "[]");
	}

	public static void notice(String msg) {
		print("* " + msg, Gui.noticeMsgColor);
	}

	public void render() {
		if (state.equals(GameState.GAME)) {

			if (justStarted)
				justStarted = false;

			Mouse.mouseinworld.set(Gdx.input.getX(), Gdx.input.getY(), 0.0F);
			camera.unproject(Mouse.mouseinworld);
			Gdx.input.setInputProcessor(Gui.overlay);
			Utils.checkConsoleFocus();
			Utils.pollStatWindowKey();
			if (Gui.overlay.getKeyboardFocus() != Gui.inputField && !Gui.gameUnfocused) {
				Mouse.updCoords();
				Mouse.mouseClickAction();
				Utils.gameKeyCheck();
				Utils.invKeyCheck();
			}
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			Utils.batch.begin();
			drawGame();
			Gui.overlay.act();
			Gui.overlay.draw();
			Utils.batch.end();
		} else if (state.equals(GameState.INVENTORY)) {
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			Utils.batch.begin();
			drawGame();
			Utils.batch.end();
			Mouse.mouseinworld.set(Gdx.input.getX(), Gdx.input.getY(), 0.0F);
			Utils.invKeyCheck();

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
			camera.viewportWidth = (Gdx.graphics.getWidth() / ZOOM);
			camera.viewportHeight = (Gdx.graphics.getHeight() / ZOOM);
			camera.update();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			Utils.batch.begin();
			drawMenuBackground();
			Utils.batch.end();
			Gui.menu.act();
			Gui.menu.draw();
		} else if (state.equals(GameState.WORLDGEN)) {
			Utils.batch.begin();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			updateCamToxy(World.player.globalPos.x, World.player.globalPos.y);
			drawMenuBackground();
			Gui.worldg.draw();
			Utils.batch.end();
		} else if (state.equals(GameState.LOAD)) {
			Utils.batch.begin();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			drawMenuBackground();
			Gui.loadg.draw();
			Utils.batch.end();
		} else if (state.equals(GameState.GOT)) {
			Utils.batch.begin();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			drawMenuBackground();
			Gui.gotodg.draw();
			Utils.batch.end();
		} else if (state.equals(GameState.DEAD)) {
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Utils.batch.begin();
			drawGame();
			Utils.batch.end();
			Gui.dead.act();
			Gui.dead.draw();
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
			Gui.craft.act();
			Gui.craft.draw();
		}
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