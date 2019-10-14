package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.enums.GameState;
import ru.bernarder.fallenrisefromdust.properties.ItemProp;

import java.io.File;
import java.io.PrintStream;

public class MadSand extends com.badlogic.gdx.Game {
	public static String VER = "";
	static int[][] quests;

	static double PartisanRep = 0.0D;
	static double OutlawRep = 0.0D;
	static double MarauderRep = 0.0D;

	static String gameVrf;

	static Vector3 mouseinworld = new Vector3(0.0F, 0.0F, 0.0F);
	static int wclickx = 0;
	static int wclicky = 0;

	static boolean dontlisten = false;
	static int dialogresult;
	static int questid = 0;
	static boolean dialogflag = true;
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
	static final String WORLDFILE = "/World.mw";
	static final String INVFILE = "/PlayerInventory.mpi";

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

	public static final int EQ_SLOTS = 5;
	public static final int GUILABELS = 4;
	public static int ENCOUNTERCHANCE = 10;
	public static int[] COSMETICSPRITES = { 17 };
	public static int SEED = 100;
	static int SPEED = 100;
	static float ZOOM = 1.5F;
	static final int DEFAULT_FOV = 13;

	static final String FONT_CHARS = "АБВГДЕЁЖЗИЙКЛМНОПРСТФХЦЧШЩЪЬЫЭЮЯабвгдеёжзийклмнопрстфхцчшщыъьэюяabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;:,{}\"'<>";
	static final String FONT_PATH = "fonts/8bitoperator.ttf";

	boolean started = true;
	static int rendered = 2;
	float percent = 0.0F;

	public static float[][] rawWorld;
	ItemProp objn;
	static OrthographicCamera camera;

	SpriteBatch invbatch;
	OrthographicCamera invcamera;
	private float elapsedTime;
	static boolean charcrt = false;;
	public static GameState state = GameState.LAUNCHER;
	public static int wmx = 0;
	public static int wmy = 0;
	public static boolean contextopened = false;
	public static int camxoffset;
	public static int camyoffset;
	public static int OREFIELDS = 10;
	public static int OVERWORLD = 1;
	public static int UNDERWORLD = 0;
	public static int MAXSAVESLOTS = 11;
	public static int CROPS;
	public static boolean tonext = false;
	public static int tempwx, tempwy;
	public static boolean encounter = false;

	static PairFloat[] renderArea;
	static ObjectMapper mapper = new ObjectMapper();

	public static World world;

	static final int TEST_POINT = 50;

	public void create() {
		Gdx.graphics.setContinuousRendering(false);
		setRenderRadius(DEFAULT_FOV);
		World.player = new Player();
		Utils.out("Starting initialization!");
		setRenderRadius();
		Utils.out("Render area: " + renderArea.length);
		try {
			PrintStream ge = new PrintStream(new File("MadSandCritical.log"));
			System.setErr(ge);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Utils.init();
		this.objn = new ItemProp();
		Gui.createBasicSkin();
		QuestUtils.init();
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.local(SAVEDIR + FONT_PATH));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.characters = FONT_CHARS;
		parameter.size = 24;
		parameter.color = Color.BLUE;
		Gui.font1 = generator.generateFont(parameter);
		generator.dispose();
		Utils.ubound = (World.worldCoord(World.MAPSIZE) - 33);
		Utils.lbound = (World.worldCoord(World.MAPSIZE) - 33);
		camera = new OrthographicCamera();
		this.invcamera = new OrthographicCamera();
		this.invbatch = new SpriteBatch();
		camera.update();
		Gui.font = new BitmapFont();
		world = new World(MadSand.WORLDSIZE);
		World.player.init();
		World.player.globalPos.x = (World.player.x * TILESIZE);
		World.player.globalPos.y = (World.player.y * TILESIZE);
		Gui.equip = new Image[EQ_SLOTS];
		for (int i = 0; i < EQ_SLOTS; ++i) {
			Gui.equip[i] = new Image();
			Gui.equip[i].setDrawable(Resource.noEquip);
		}
		Gui.initmenu();
		Gui.font.getData().markupEnabled = true;
		Gui.font1.getData().markupEnabled = true;
		World.player.initInventory();
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
		camera.position.set(f + 17.0F + camxoffset, y2 + 37.0F + camyoffset, 0.0F);
		if (camera.position.y < 260.0F)
			camera.position.y = 250.0F;
		if (camera.position.x < 320.0F)
			camera.position.x = 320.0F;
		if (camera.position.x > (World.MAPSIZE * TILESIZE))
			camera.position.x = (World.MAPSIZE * TILESIZE);
		if (camera.position.y > (World.MAPSIZE * TILESIZE))
			camera.position.y = (World.MAPSIZE * TILESIZE);
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

	static int trdx, trdy;

	void DrawGame() {
		try {
			int i = 0;
			while (i < renderArea.length) {
				Utils.batch.draw(
						Resource.tile[world.getTileOrDefault(World.player.x + (int) renderArea[i].x,
								World.player.y + (int) renderArea[i].y)],
						World.player.globalPos.x + renderArea[i].x * TILESIZE,
						World.player.globalPos.y + renderArea[i].y * TILESIZE);
				i++;
			}
			if (Player.isCollisionMask(World.player.x, World.player.y)) {
				drawEntity(World.player);
			}
			i = 0;
			while (i < renderArea.length) {
				trdx = World.player.x + (int) renderArea[i].x;
				trdy = World.player.y + (int) renderArea[i].y;
				/*
				 * if (trdx < 0) trdx = 0; if (trdy < 0) trdy = 0; if (trdx >= World.MAPSIZE +
				 * World.BORDER) trdx = World.MAPSIZE + World.BORDER; if (trdy >= World.MAPSIZE
				 * + World.BORDER) trdy = World.MAPSIZE + World.BORDER;
				 */
				int objid = world.getCurLoc().getObject(trdx, trdy).id;
				if ((objid != 0) && (objid != 666)) {
					Utils.batch.draw(Resource.objects[objid],
							World.player.globalPos.x + (int) renderArea[i].x * TILESIZE,
							World.player.globalPos.y + (int) renderArea[i].y * TILESIZE);
				}
				if (World.player.standingOnLoot(trdx, trdy)) {
					Utils.batch.draw(Resource.objects[OBJECT_LOOT],
							World.player.globalPos.x + (int) renderArea[i].x * TILESIZE,
							World.player.globalPos.y + (int) renderArea[i].y * TILESIZE);
				}

				i++;
			}
			if (!Player.isCollisionMask(World.player.x, World.player.y)) {
				drawEntity(World.player);
			}
			i = 0;
			Utils.batch.draw(Resource.mapcursor, wmx * 33, wmy * 33);
			Utils.batch.end();
			Gui.refreshOverlay();
			Utils.batch.begin();
		} catch (Exception e) {
			e.printStackTrace();
			Utils.out("Render error. See errorlog.");
			System.exit(-1);
		}
	}

	void drawWorld() {
		int i = 0;
		while (i < renderArea.length) {
			Utils.batch.draw(
					Resource.tile[world.getTileOrDefault(World.player.y + (int) renderArea[i].y,
							World.player.x + (int) renderArea[i].x)],
					World.player.globalPos.x + renderArea[i].x * 33, World.player.globalPos.y + renderArea[i].y * 33);
			i++;
		}
		Utils.batch.draw(Resource.playerSprite, World.player.globalPos.x,
				World.player.globalPos.y + World.player.stepy);
		i = 0;
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
		contextopened = false;
		Gui.acceptD.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.dialogflag = true;
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
				MadSand.dialogflag = true;
				MadSand.dialogresult = 1;
				MadSand.maindialog.setVisible(false);
			}
		});

		if (type != -1) {
			dialogflag = false;
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

	void drawEntity(Player entity) {
		if (entity.isStepping()) {
			float camx, camy;
			this.elapsedTime += Gdx.graphics.getDeltaTime();
			if (entity.stats.look == Direction.RIGHT) {
				Utils.batch.draw((TextureRegion) Resource.ranim.getKeyFrame(this.elapsedTime, true),
						entity.globalPos.x - entity.stepx, entity.globalPos.y);
				camx = entity.globalPos.x - entity.stepx;
				camy = entity.globalPos.y;
			} else if (entity.stats.look == Direction.LEFT) {
				Utils.batch.draw((TextureRegion) Resource.lanim.getKeyFrame(this.elapsedTime, true),
						entity.globalPos.x + entity.stepx, entity.globalPos.y);
				camx = entity.globalPos.x + entity.stepx;
				camy = entity.globalPos.y;
			} else if (entity.stats.look == Direction.UP) {
				Utils.batch.draw((TextureRegion) Resource.uanim.getKeyFrame(this.elapsedTime, true), entity.globalPos.x,
						entity.globalPos.y - entity.stepy);

				camx = entity.globalPos.x;
				camy = entity.globalPos.y - entity.stepy;
			} else {
				Utils.batch.draw((TextureRegion) Resource.danim.getKeyFrame(this.elapsedTime, true), entity.globalPos.x,
						entity.globalPos.y + entity.stepy);
				camx = entity.globalPos.x;
				camy = entity.globalPos.y + entity.stepy;
			}

			if (entity.isMain)
				updateCamToxy(camx, camy);
			entity.stepx -= entity.movespeed;
			entity.stepy -= entity.movespeed;
			if (entity.stepx <= 1) {
				entity.setStepping(false);
				entity.stepx = TILESIZE;
				entity.stepy = TILESIZE;
			}
		} else {
			Utils.batch.draw(Resource.playerSprite, entity.globalPos.x, entity.globalPos.y);
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
			updateCamToxy(World.player.globalPos.x, World.player.globalPos.y);

			camera.viewportWidth = (Gdx.graphics.getWidth() / ZOOM);
			camera.viewportHeight = (Gdx.graphics.getHeight() / ZOOM);
			camera.update();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			Utils.batch.begin();
			drawWorld();
			Utils.batch.end();
			Gui.menu.act();
			Gui.menu.draw();
		} else if (state.equals(GameState.WORLDGEN)) {
			Utils.batch.begin();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			updateCamToxy(World.player.globalPos.x, World.player.globalPos.y);
			drawWorld();
			Gui.worldg.draw();
			Utils.batch.end();
		} else if (state.equals(GameState.LOAD)) {
			Utils.batch.begin();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			drawWorld();
			Gui.loadg.draw();
			Utils.batch.end();
		} else if (state.equals(GameState.GOT)) {
			Utils.batch.begin();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			drawWorld();
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
}