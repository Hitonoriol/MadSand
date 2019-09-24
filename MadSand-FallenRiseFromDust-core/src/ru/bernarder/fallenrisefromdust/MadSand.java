package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.MapSerializer;

import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.enums.Faction;
import ru.bernarder.fallenrisefromdust.enums.GameState;
import ru.bernarder.fallenrisefromdust.properties.ItemProp;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;

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

	static int renderradius = 12 * 33;

	static int mx = 0;
	static int my = 0;

	static final int TILESIZE = 33;
	public static int OBJLEVELS = 2;
	static final int WORLDSIZE = 10;

	public static boolean stepping = false;
	public static int stepx = TILESIZE;
	public static int stepy = TILESIZE;
	public static int movespeed = 2;
	public static int runspeed = movespeed * 2;

	static final String SAVEDIR = "MadSand_Saves/";
	static String QUESTFILE = SAVEDIR + "quest.xml";
	static String RESFILE = SAVEDIR + "res.xml";
	static final String VERFILE = SAVEDIR + "ver.dat";
	static final String MAPDIR = SAVEDIR + "worlds/";
	static final String SCRIPTDIR = SAVEDIR + "scripts";
	static final String PLAYERFILE = "/Player.mc";

	static int numlook = 0;

	static final int XDEF = 1280;
	static final int YDEF = 720;
	static float GUISTART = 20.0F;

	static final int TREESDENSITY = 30;
	static final int BOULDERDENSITY = 3;
	static final int EASTERS = 2; // TODO move worldgen params to .xml
	static final int BUSHDENSITY = 20;
	static final int CACTDENS = 35;
	static final int MAXOREFIELDSIZE = 10;
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
	static float ZOOM = 1.5F;

	static final String FONT_CHARS = "АБВГДЕЁЖЗИЙКЛМНОПРСТФХЦЧШЩЪЬЫЭЮЯабвгдеёжзийклмнопрстфхцчшщыъьэюяabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;:,{}\"'<>";
	static final String FONT_PATH = "fonts/8bitoperator.ttf";

	boolean started = true;
	public static int turn = 0;
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

	static Vector2[] rcoords;

	static int countRcells() {
		int i = 0;
		int ii = 0, cl = 0;
		while (i < World.MAPSIZE + World.BORDER) {
			while (ii < World.MAPSIZE + World.BORDER) {
				if (calcDistance(50 * 33, 50 * 33, i * 33, ii * 33) <= renderradius) {
					cl++;
				}
				ii++;
			}
			ii = 0;
			i++;
		}
		return cl;
	}

	static Vector2[] getAllRcells(Vector2[] cl) {
		int i = 0;
		int ii = 0, clc = 0;
		while (i < World.MAPSIZE + World.BORDER) {
			while (ii < World.MAPSIZE + World.BORDER) {
				if (calcDistance(50 * 33, 50 * 33, i * 33, ii * 33) <= renderradius) {
					cl[clc] = new Vector2(50 - ii, 50 - i);
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
		rcoords = new Vector2[countRcells()];
		rcoords = getAllRcells(rcoords);
	}

	public static World world;
	public static Player player;

	static Kryo kryo;
	static MapSerializer<HashMap<MapID, Map>> mapSerializer = new MapSerializer<HashMap<MapID, Map>>();

	public void create() {
		kryo = new Kryo();
		kryo.register(Pair.class);
		kryo.register(int[].class);
		kryo.register(Faction.class);
		kryo.register(MapID.class);
		kryo.register(Map.class);
		kryo.register(Array.class);
		kryo.register(Object[].class);
		kryo.register(Direction.class);
		kryo.register(java.util.Vector.class);
		kryo.register(Stats.class);
		kryo.register(HashMap.class);
		kryo.register(MapObject.class);
		kryo.register(Tile.class);
		kryo.register(Loot.class);
		kryo.register(Npc.class);
		kryo.register(Location.class);

		int radius = 13;
		if (new File(SAVEDIR + "lastrend.dat").exists())
			radius = (Integer.parseInt(Gui.getExternal("lastrend.dat")));
		setParams(radius);
		player = new Player();
		Utils.out("Starting initialization!");
		setRenderRadius();
		Utils.out("Render area: " + rcoords.length);
		try {
			PrintStream ge = new PrintStream(new File("MadSandCritical.log"));
			System.setErr(ge);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Utils.Initf();
		world = new World(MadSand.WORLDSIZE);
		this.objn = new ItemProp();
		Gui.createBasicSkin();
		Gui.chat = new Label[15];
		int vc = 0;
		while (vc < 15) {
			Gui.chat[vc] = new Label(" ", Gui.skin);
			vc++;
		}

		Gui.gui = new Label[4];
		vc = 0;
		while (vc < Gui.gui.length) {
			Gui.gui[vc] = new Label(" ", Gui.skin);
			++vc;
		}
		QuestUtils.init();
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.local(SAVEDIR + FONT_PATH));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.characters = FONT_CHARS;
		parameter.size = 24;
		parameter.color = Color.BLUE;
		Gui.font1 = generator.generateFont(parameter);
		generator.dispose();
		Gdx.graphics.setContinuousRendering(true);
		Utils.ubound = (World.worldCoord(World.MAPSIZE) - 33);
		Utils.lbound = (World.worldCoord(World.MAPSIZE) - 33);
		camera = new OrthographicCamera();
		this.invcamera = new OrthographicCamera();
		this.invbatch = new SpriteBatch();
		camera.update();
		Gui.font = new BitmapFont();
		Utils.ppos.x = (player.x * 33);
		Utils.ppos.y = (player.y * 33);
		Gui.equip = new Image[5];
		Gui.equip[0] = new Image(new TextureRegionDrawable(new TextureRegion(Utils.placeholder)));
		Gui.equip[1] = new Image(new TextureRegionDrawable(new TextureRegion(Utils.placeholder)));
		Gui.equip[2] = new Image(new TextureRegionDrawable(new TextureRegion(Utils.placeholder)));
		Gui.equip[3] = new Image(new TextureRegionDrawable(new TextureRegion(Utils.placeholder)));
		Gui.equip[4] = new Image(new TextureRegionDrawable(new TextureRegion(Utils.placeholder)));
		Gui.initmenu();
		Gui.font.getData().markupEnabled = true;
		Gui.font1.getData().markupEnabled = true;
		player.initInventory();
		Utils.out("End of initialization!");
	}

	public static void setParams(int radius) {
		renderradius = radius * MadSand.TILESIZE;
		setRenderRadius();
	}

	static void setUpScene() {
		Utils.out("Setting starting scene up!");
		world.getCurLoc().addObject(50, 49, 6);
	}

	public void updateCamToxy(float f, float y2) {
		camera.position.set(f + 17.0F + camxoffset, y2 + 37.0F + camyoffset, 0.0F);
		if (camera.position.y < 260.0F)
			camera.position.y = 250.0F;
		if (camera.position.x < 320.0F)
			camera.position.x = 320.0F;
		if (camera.position.x > (World.MAPSIZE * 33) + 100)
			camera.position.x = (World.MAPSIZE * 33) + 100;
		if (camera.position.y > (World.MAPSIZE * 33) + 100)
			camera.position.y = (World.MAPSIZE * 33) + 100;
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
			while (i < rcoords.length) {
				Utils.batch.draw(Utils.tile[world.rend(player.x + (int) rcoords[i].x, player.y + (int) rcoords[i].y)],
						Utils.ppos.x + rcoords[i].x * 33, Utils.ppos.y + rcoords[i].y * 33);
				i++;
			}
			if (Player.isCollisionMask(player.x, player.y)) {
				drawPlayer();
			}
			i = 0;
			while (i < rcoords.length) {
				trdx = player.x + (int) rcoords[i].x;
				trdy = player.y + (int) rcoords[i].y;
				if (trdx < 0)
					trdx = 0;
				if (trdy < 0)
					trdy = 0;
				if (trdx >= World.MAPSIZE + World.BORDER)
					trdx = World.MAPSIZE + World.BORDER;
				if (trdy >= World.MAPSIZE + World.BORDER)
					trdy = World.MAPSIZE + World.BORDER;
				int objid = world.getCurLoc().getObject(trdx, trdy).id;
				if ((objid != 0) && (objid != 666)) {
					Utils.batch.draw(Utils.objects[objid], Utils.ppos.x + (int) rcoords[i].x * 33,
							Utils.ppos.y + (int) rcoords[i].y * 33);
				}
				if (Player.standingOnLoot(trdx, trdy)) {
					Utils.batch.draw(Utils.objects[7], Utils.ppos.x + (int) rcoords[i].x * 33,
							Utils.ppos.y + (int) rcoords[i].y * 33);
				}
				/*
				 * if (MobLayer.getMobId(trdx, trdy) != 0) {
				 * Utils.batch.draw(Utils.npc[MobLayer.getMobId(trdx, trdy)], Utils.ppos.x +
				 * (int) rcoords[i].x * 33, Utils.ppos.y + (int) rcoords[i].y * 33); }
				 */

				i++;
			}
			if (!Player.isCollisionMask(player.x, player.y)) {
				drawPlayer();
			}
			i = 0;
			Utils.batch.draw(Utils.mapcursor, wmx * 33, wmy * 33);
			Utils.batch.end();
			Gui.refreshOverlay();
			Utils.batch.begin();
		} catch (Exception e) {
			e.printStackTrace();
			Utils.out("Render error. See errorlog.");
		}
	}

	void drawWorld() {
		int i = 0;
		while (i < rcoords.length) {
			Utils.batch.draw(Utils.tile[world.rend(player.y + (int) rcoords[i].y, player.x + (int) rcoords[i].x)],
					Utils.ppos.x + rcoords[i].x * 33, Utils.ppos.y + rcoords[i].y * 33);
			i++;
		}
		Utils.batch.draw(Utils.Splayer, Utils.ppos.x, Utils.ppos.y + stepy);
		i = 0;
	}

	public static void teleport(int x, int y) {
		Utils.ppos.x = (x * 33);
		Utils.ppos.y = (y * 33);
		player.x = x;
		player.y = y;
	}

	static void showDialog(final int type, String text, final int qid) {
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

	int countCoordFromWorld(int arg) {
		return arg * MadSand.TILESIZE;
	}

	void drawPlayer() {
		if (stepping) {
			this.elapsedTime += Gdx.graphics.getDeltaTime();
			if (player.stats.look == Direction.RIGHT) {
				Utils.batch.draw((TextureRegion) Utils.ranim.getKeyFrame(this.elapsedTime, true), Utils.ppos.x - stepx,
						Utils.ppos.y);
				updateCamToxy(Utils.ppos.x - stepx, Utils.ppos.y);
			}
			if (player.stats.look == Direction.LEFT) {
				Utils.batch.draw((TextureRegion) Utils.lanim.getKeyFrame(this.elapsedTime, true), Utils.ppos.x + stepx,
						Utils.ppos.y);
				updateCamToxy(Utils.ppos.x + stepx, Utils.ppos.y);
			}
			if (player.stats.look == Direction.UP) {
				Utils.batch.draw((TextureRegion) Utils.uanim.getKeyFrame(this.elapsedTime, true), Utils.ppos.x,
						Utils.ppos.y - stepy);
				updateCamToxy(Utils.ppos.x, Utils.ppos.y - stepy);
			}
			if (player.stats.look == Direction.DOWN) {
				Utils.batch.draw((TextureRegion) Utils.danim.getKeyFrame(this.elapsedTime, true), Utils.ppos.x,
						Utils.ppos.y + stepy);
				updateCamToxy(Utils.ppos.x, Utils.ppos.y + stepy);
			}

			stepx -= movespeed;
			stepy -= movespeed;
			if (stepx <= 1) {
				stepping = false;
				stepx = 33;
				stepy = 33;
			}
		} else {
			Utils.batch.draw(Utils.Splayer, Utils.ppos.x, Utils.ppos.y);
			updateCamToxy(Utils.ppos.x, Utils.ppos.y);
		}
	}

	public static void createDirs() {
		File saveloc = new File(SAVEDIR);
		File maploc = new File(MAPDIR);
		File curworld = new File(MadSand.MAPDIR + MadSand.WORLDNAME);

		if (!saveloc.exists()) {
			saveloc.mkdirs();
		}
		if (!maploc.exists()) {
			maploc.mkdirs();
		}
		if (!curworld.exists())
			curworld.mkdirs();
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
				Utils.KeyCheck();
				// TODO: loot collision
				Utils.InvKeyCheck();
			}
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			Utils.batch.begin();
			DrawGame();
			Gui.overlay.draw();
			Gui.overlay.act();
			Utils.batch.end();
		} else if (state.equals(GameState.INVENTORY)) {
			try {
				Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
				Utils.batch.begin();
				DrawGame();
				Utils.batch.end();
				mouseinworld.set(Gdx.input.getX(), Gdx.input.getY(), 0.0F);
				Utils.InvKeyCheck();
				Utils.inInvKeyCheck();

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
			updateCamToxy(Utils.ppos.x, Utils.ppos.y);

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
			updateCamToxy(Utils.ppos.x, Utils.ppos.y);
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
			Utils.batch.begin();
			DrawGame();
			Utils.batch.end();
			Gui.craft.act();
			Gui.craft.draw();
		}
	}

	public static void setTime(int arg) {
		World.wtime = arg;
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