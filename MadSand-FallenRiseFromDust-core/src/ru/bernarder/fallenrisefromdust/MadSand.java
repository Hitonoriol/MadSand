package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.enums.GameState;

import java.io.File;
import java.io.PrintStream;

public class MadSand extends Game {
	public static String VER = "";
	static int[][] quests;

	static double PartisanRep = 0.0D;
	static double OutlawRep = 0.0D;
	static double MarauderRep = 0.0D;

	static String gameVrf;

	static Vector3 mouseinworld = new Vector3(0.0F, 0.0F, 0.0F);
	static int wclickx = 0;
	static int wclicky = 0;

	static int dialogresult;
	static int questid = 0;
	static boolean dialogClosed = true;
	static Table dialog;
	static Table maindialog;

	static int mx = 0;
	static int my = 0;

	static final int OBJECT_LOOT = 7;
	static final int TILESIZE = 33;
	public static int OBJLEVELS = 2;
	static final int WORLDSIZE = 10;

	static int renderradius;

	static final String SAVEDIR = "MadSand_Saves/";
	static String QUESTFILE = SAVEDIR + "quests.xml";
	static String GENFILE = SAVEDIR + "worldgen.xml";
	static String RESFILE = SAVEDIR + "res.xml";
	static String SKILLFILE = SAVEDIR + "defskills.xml";
	static final String MAPDIR = SAVEDIR + "worlds/";
	static final String SCRIPTDIR = SAVEDIR + "scripts";
	static final String PLAYERFILE = "/Player.mc";
	static final String NPCSFILE = "/NPCs.ms";
	static final String WORLDFILE = "/World.mw";
	static final String INVFILE = "/PlayerInventory.mpi";
	static final String ERRFILE = "MadSandCritical.log";

	static int numlook = 0;

	static final int XDEF = 1280;
	static final int YDEF = 720;
	static float GUISTART = 20.0F;
	static boolean renderc = false;

	static String WORLDNAME = "Save Slot #1";

	public static int[] craftableid;
	public static int CRAFTABLES = 0;
	public static int LASTITEMID;
	public static int LASTOBJID;
	public static int NPCSPRITES;
	public static int LASTTILEID;
	public static int OREFIELDCOUNT = 5;
	public static int BIOMES = 4;
	public static int MAXMOBSONMAP = 35;
	static int QUESTS = 0;

	public static final int GUILABELS = 4;
	public static int ENCOUNTERCHANCE = 10;
	public static int[] COSMETICSPRITES = { 17 };
	public static int SEED = 100;
	static int SPEED = 100;
	static final float DEFAULT_ZOOM = 1.5F;
	static float ZOOM = DEFAULT_ZOOM;
	static final int DEFAULT_FOV = 25;

	static final String FONT_CHARS = "АБВГДЕЁЖЗИЙКЛМНОПРСТФХЦЧШЩЪЬЫЭЮЯабвгдеёжзийклмнопрстфхцчшщыъьэюяabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;:,{}\"'<>";
	static final String FONT_PATH = "fonts/8bitoperator.ttf";

	boolean started = true;
	static int rendered = 2;
	float percent = 0.0F;

	public static float[][] rawWorld;
	static OrthographicCamera camera;

	private float elapsedTime;
	static boolean charcrt = false;;
	public static GameState state = GameState.LAUNCHER;
	public static int wmx = 0;
	public static int wmy = 0;
	public static int camxoffset = 17;
	public static int camyoffset = 37;
	public static int OREFIELDS = 10;
	public static int OVERWORLD = 1;
	public static int UNDERWORLD = 0;
	public static int MAXSAVESLOTS = 999;
	public static int CROPS;
	public static boolean tonext = false;
	public static int tempwx, tempwy;
	public static boolean encounter = false;

	static PairFloat[] renderArea;
	static ObjectMapper mapper = new ObjectMapper();

	public static World world;

	static final int TEST_POINT = 50;

	static float mid = TILESIZE * (World.MAPSIZE / 2);
	static float ymenu;
	static float xmenu = ymenu = mid;
	static float menuXStep = 1, menuYStep = -1;
	static float menuOffset = 250;

	static void switchStage(GameState state, Stage stage) {

		if (state != GameState.INVENTORY)
			World.player.hideInventory();

		if (Gui.contextMenuActive)
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
		world.Generate();

		Utils.out("End of initialization!");
	}

	static int countRcells() {
		int i = 0;
		int ii = 0, cl = 0;
		while (i < World.MAPSIZE + World.BORDER) {
			while (ii < World.MAPSIZE + World.BORDER) {
				if (calcDistance(TEST_POINT * TILESIZE, TEST_POINT * TILESIZE, i * TILESIZE,
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
				if (calcDistance(TEST_POINT * TILESIZE, TEST_POINT * TILESIZE, i * TILESIZE,
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

		mouseinworld.set(Gdx.input.getX(), Gdx.input.getY(), 0.0F);
		camera.unproject(mouseinworld);
		Utils.batch.setProjectionMatrix(camera.combined);
	}

	public static double calcDistance(int x1, int y1, int x2, int y2) {
		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}

	void DrawGame() {
		try {
			Map curLoc;
			int trdx, trdy;
			Npc npc;
			int objid;
			int i = 0;
			curLoc = world.getCurLoc();

			if (World.player.isInBackground())
				drawEntity(World.player);

			while (i < renderArea.length) {
				trdx = World.player.x + (int) renderArea[i].x;
				trdy = World.player.y + (int) renderArea[i].y;
				Utils.batch.draw(Resources.tile[world.getTileOrDefault(trdx, trdy)], trdx * TILESIZE, trdy * TILESIZE);
				i++;
			}

			i = 0;
			while (i < renderArea.length) {
				trdx = World.player.x + (int) renderArea[i].x;
				trdy = World.player.y + (int) renderArea[i].y;

				npc = curLoc.getNpc(trdx, trdy);
				objid = curLoc.getObject(trdx, trdy).id;

				if ((objid != MapObject.NULL_OBJECT_ID) && (objid != MapObject.COLLISION_MASK_ID))
					Utils.batch.draw(Resources.objects[objid], trdx * TILESIZE, trdy * TILESIZE);

				if (World.player.standingOnLoot(trdx, trdy))
					Utils.batch.draw(Resources.objects[OBJECT_LOOT], trdx * TILESIZE, trdy * TILESIZE);

				if (npc != Map.nullNpc)
					drawEntity(npc);

				i++;
			}

			if (!World.player.isInBackground())
				drawEntity(World.player);

			Utils.batch.draw(Resources.mapcursor, wmx * TILESIZE, wmy * TILESIZE);
			Utils.batch.end();
			Gui.refreshOverlay();
			Utils.batch.begin();
		} catch (Exception e) {
			e.printStackTrace();
			Utils.out("Render error. See errorlog.");
			System.exit(-1);
		}
	}

	float randSide() {
		int m = 1;
		if (Utils.random.nextBoolean())
			m *= -1;
		return (Utils.rand(0, 1) * Utils.random.nextFloat() + 1) * m;
	}

	void drawMenuBackground() {

		if (xmenu > (mid + menuOffset))
			menuXStep = randSide();

		if (ymenu > (mid + menuOffset))
			menuYStep = randSide();

		if (xmenu < (mid - menuOffset))
			menuXStep = randSide();

		if (ymenu < (mid - menuOffset))
			menuYStep = randSide();

		ymenu += menuYStep;
		xmenu += menuXStep;

		DrawGame();
	}

	static void showDialog(final int type, String text, final int qid) { // TODO rework and remove this shit
		boolean lot = false;
		String toin = "";
		if (text.indexOf("!@!") > -1) {
			lot = true;
			String[] brtext = text.split("\\!\\@\\!");
			text = brtext[0];
			int i = 1;
			while (i < brtext.length) {
				int tmp56_54 = i;
				String[] tmp56_52 = brtext;
				toin = toin + (tmp56_52[tmp56_54] = tmp56_52[tmp56_54] + "!@!");
				i++;
			}
		}
		if (toin.indexOf("!@!") == -1) {
			lot = false;
		}
		final boolean lott = lot;
		final String ads = toin;
		Gui.acceptD.setText("Accept");
		Gui.refuseD.setText("Refuse");
		Gui.mousemenu.setVisible(false);
		Gui.gamecontext.setVisible(false);
		Gui.contextMenuActive = false;
		Gui.acceptD.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.dialogClosed = true;
				MadSand.state = GameState.GAME;
				MadSand.dialogresult = 0;
				MadSand.maindialog.setVisible(false);
				if (lott)
					MadSand.showDialog(type, ads, qid);
			}
		});
		Gui.refuseD.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.state = GameState.GAME;
				MadSand.dialogClosed = true;
				MadSand.dialogresult = 1;
				MadSand.maindialog.setVisible(false);
			}
		});

		if (type != -1) {
			dialogClosed = false;
			maindialog.setVisible(true);
			Gui.dialMSG.setText(text);
			if (type == 1) {
				Gui.acceptD.setVisible(true);
				Gui.refuseD.setVisible(false);
			}
			if (type == 2) {
				Gui.acceptD.setVisible(true);
				Gui.refuseD.setVisible(true);
			}
			questid = qid;
		} else {
			maindialog.setVisible(false);
		}
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

	public void render() {
		if (state.equals(GameState.GAME)) {
			if (started) {
				started = false;
			}
			mouseinworld.set(Gdx.input.getX(), Gdx.input.getY(), 0.0F);
			camera.unproject(mouseinworld);
			Gdx.input.setInputProcessor(Gui.overlay);
			Utils.checkFocus();
			if (Gui.overlay.getKeyboardFocus() != Gui.inputField && !charcrt) {
				Utils.updMouseCoords();
				Utils.mouseMovement();
				Utils.gameKeyCheck();
				World.player.pickUpLoot();
				Utils.invKeyCheck();
			}
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			Utils.batch.begin();
			DrawGame();
			Gui.overlay.act();
			Gui.overlay.draw();
			Utils.batch.end();
		} else if (state.equals(GameState.INVENTORY)) {
			try {
				Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
				Gdx.gl.glClear(16384);
				Utils.batch.begin();
				DrawGame();
				Utils.batch.end();
				mouseinworld.set(Gdx.input.getX(), Gdx.input.getY(), 0.0F);
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
			DrawGame();
			Utils.batch.end();
			Gui.overlay.act();
			Gui.overlay.draw();
		} else if (state.equals(GameState.NMENU)) {
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
			DrawGame();
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
			DrawGame();
			Utils.batch.end();
		} else if (state.equals(GameState.CRAFT)) {
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			Utils.batch.begin();
			DrawGame();
			Utils.batch.end();
			Gui.craft.act();
			Gui.craft.draw();
		}
	}

	static void showMsg(String text) {
		showDialog(1, text, 0);
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