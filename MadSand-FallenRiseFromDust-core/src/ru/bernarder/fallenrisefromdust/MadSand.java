package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.bernarder.fallenrisefromdust.containers.Line;
import ru.bernarder.fallenrisefromdust.containers.PairFloat;
import ru.bernarder.fallenrisefromdust.entities.Entity;
import ru.bernarder.fallenrisefromdust.entities.Npc;
import ru.bernarder.fallenrisefromdust.entities.Player;
import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.enums.GameState;
import ru.bernarder.fallenrisefromdust.map.Map;
import ru.bernarder.fallenrisefromdust.map.MapObject;
import ru.bernarder.fallenrisefromdust.map.Tile;
import ru.bernarder.fallenrisefromdust.properties.Tutorial;
import ru.bernarder.fallenrisefromdust.world.World;

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
	static String QUESTFILE = SAVEDIR + "quests.xml";
	static String GENFILE = SAVEDIR + "worldgen.xml";
	static String TUTORIALFILE = SAVEDIR + "tutorial.xml";

	static String TILEFILE = SAVEDIR + "tiles.xml";
	static String OBJECTFILE = SAVEDIR + "objects.xml";
	static String NPCFILE = SAVEDIR + "npcs.xml";
	static String ITEMSFILE = SAVEDIR + "items.xml";

	static String SKILLFILE = SAVEDIR + "defskills.xml";
	static final String MAPDIR = SAVEDIR + "worlds/";
	static final String SCRIPTDIR = SAVEDIR + "scripts";
	static final String PLAYERFILE = "/Player" + SAVE_EXT;
	static final String NPCSFILE = "NPCs";
	static final String WORLDFILE = "/World" + SAVE_EXT;
	static final String ERRFILE = "MadSandCritical.log";

	static final int XDEF = 1280;
	static final int YDEF = 720;

	static String WORLDNAME = "";

	static final float DEFAULT_ZOOM = 1.5F;
	static float ZOOM = DEFAULT_ZOOM;
	static final int DEFAULT_FOV = 12;

	static boolean justStarted = true; // flag for once-per-launch actions

	static OrthographicCamera camera;
	public static int camxoffset = 17;
	public static int camyoffset = 37;
	private float elapsedTime;// For player animation

	public static GameState state = GameState.LAUNCHER;
	public static int MAXSAVESLOTS = 999;
	public static ObjectMapper mapper = new ObjectMapper();

	public static World world;

	private static PairFloat[] renderArea;
	private static final int TEST_POINT = 50;

	static float ymid;
	static float xmid = ymid = TILESIZE * (World.MAPSIZE / 2);
	static float ymenu;
	static float xmenu = ymenu = xmid;
	private static float menuXStep = 0.8f, menuYStep = 0f;
	private static float menuOffset = 250;

	static void switchStage(GameState state, Stage stage) {

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

		Utils.out("End of initialization!");

		// Test stuff:
		world.getCurLoc().spawnNpc(5, 55, 55);
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

		if (player.isInBackground())
			drawEntity(player);
		
		while (i < renderArea.length) {
			x = World.player.x + (int) renderArea[i].x;
			y = World.player.y + (int) renderArea[i].y;

			tile = loc.getTile(x, y);

			if (!tile.visible || ((x > xsz || y > ysz || x < 0 || y < 0) && MadSand.world.isUnderGround())) {
				++i;
				continue;
			}

			Utils.batch.draw(Resources.tile[world.getTileOrDefault(x, y)], x * TILESIZE, y * TILESIZE);
			++i;
		}

		i = 0;
		
		while (i < renderArea.length) {
			x = World.player.x + (int) renderArea[i].x;
			y = World.player.y + (int) renderArea[i].y;

			tile = loc.getTile(x, y);

			if (!tile.visible || ((x > xsz || y > ysz || x < 0 || y < 0) && MadSand.world.isUnderGround())) {
				++i;
				continue;
			}

			npc = loc.getNpc(x, y);
			objid = loc.getObject(x, y).id;

			if (npc != Map.nullNpc)
				drawEntity(npc);

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
			if (!Gui.gameUnfocused && World.player.isNewlyCreated()) {
				Tutorial.show(Tutorial.GAME_START);
				justStarted = false;
			}
			if (justStarted) {
				justStarted = false;
			}
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
			try {
				Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
				Gdx.gl.glClear(16384);
				Utils.batch.begin();
				drawGame();
				Utils.batch.end();
				Mouse.mouseinworld.set(Gdx.input.getX(), Gdx.input.getY(), 0.0F);
				Utils.invKeyCheck();

				Gui.overlay.act();
				Gui.overlay.draw();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (state.equals(GameState.BUY)) {
			// TODO: Trade menu
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
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