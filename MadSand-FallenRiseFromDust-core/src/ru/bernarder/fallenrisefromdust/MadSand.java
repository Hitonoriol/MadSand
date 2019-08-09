package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.strings.InventoryNames;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class MadSand extends com.badlogic.gdx.Game {
	public static String VER = "";
	static int[][] quests;

	static double PartisanRep = 0.0D;
	static double OutlawRep = 0.0D;
	static double MarauderRep = 0.0D;

	static String gameVrf;

	static int QUESTS = 0;
	static int day = 1;

	public static boolean roguelike = false;

	static boolean dontlisten = false;

	static Vector3 mouseinworld = new Vector3(0.0F, 0.0F, 0.0F);
	static int wclickx = 0;
	static int wclicky = 0;

	static int worldtime = 12;
	static int lastpass = 0;
	static int TURNSTOCHANGE = 12;

	public static boolean buyflag = false;
	public static boolean tradeflag = false;
	public String resp;
	static String[] raw;
	static String[] info;
	static int dialogresult;
	static int questid = 0;
	static boolean dialogflag = true;
	static Table dialog;
	static Table maindialog;
	static int wtime = 12;
	static int port = 3265;
	public static String ip = "applang.tk";
	static InetAddress ipAddress;
	static Socket socket;
	static int renderradius = 12 * 33;

	static java.io.InputStream sin;
	static java.io.OutputStream sout;
	static DataInputStream in;
	static DataOutputStream out;
	String map;
	static int mx = 0;
	static int my = 0;

	public static int curlayer = 0;
	public static int OBJLEVELS = 2;
	public static boolean stepping = false;
	public static int stepx = 33;
	public static int stepy = 33;
	public static int movespeed = 2;
	public static int runspeed = movespeed * 2;

	static final int TILESIZE = 33;
	static final int LOGYPOS = 100;
	static final int LOGXPOS = 300;
	static int MAPSIZE = 100;
	static final int BORDER = 1;
	static final int OBJPROPS = 2;
	public static final String SAVEDIR = "MadSand_Saves/";
	static String QUESTFILE = SAVEDIR + "quest.xml";
	static String RESFILE = SAVEDIR + "res.xml";
	static int numlook = 0;

	static final int XDEF = 1280;
	static final int YDEF = 720;
	static float GUISTART = 20.0F;

	static final int TREESDENSITY = 30;
	static final int BOULDERDENSITY = 3;
	static final int EASTERS = 2;
	static final int BUSHDENSITY = 20;
	static final int CACTDENS = 35;
	static final int MAXOREFIELDSIZE = 10;
	static boolean renderc = false;

	static final int INVCELLSIZE = 82;
	static String WORLDNAME = "My world";

	public static int[] craftableid;
	public static int CRAFTABLES = 0;
	public static int LASTITEMID;
	public static int LASTOBJID;
	public static int LASTTILEID;
	public static int OREFIELDCOUNT = 5;
	public static int MAXMOBSONMAP = 35;

	public static final int GUILABELS = 4;
	public static final int BIOMES = 4;
	public static int NPCSPRITES;
	public static int ENCOUNTERCHANCE = 10;
	public static int[] COSMETICSPRITES = { 17 };
	public static int SEED = 100;
	static int SPEED = 100;
	static float ZOOM = 1.5F;
	static final String FONT_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;:,{}\"'<>";
	static final String FONT_PATH = "fonts/8bitoperator.ttf";

	boolean started = true;
	public static int x = new Random().nextInt(MadSand.MAPSIZE);
	public static int y = new Random().nextInt(MadSand.MAPSIZE);
	public static int curxwpos = 5;
	public static int curywpos = 5;
	public static int tx;
	public static int ty;
	public static int cx = 0;
	public static int cy = 0;

	static int INVENTORYSIZE = 30;
	public static int turn = 0;
	static int rendered = 2;
	float percent = 0.0F;
	public static String name = "";
	public static int[][] inv = new int[INVENTORYSIZE][4];
	public static int[][] trade = new int[INVENTORYSIZE][4];

	public static float[][] rawWorld;
	InventoryNames objn;
	static OrthographicCamera camera;

	SpriteBatch invbatch;
	OrthographicCamera invcamera;
	private float elapsedTime;
	static boolean charcrt = false;;
	public static String state = "LAUNCHER";
	public static int wmx = 0;
	public static int wmy = 0;
	public static boolean contextopened = false;
	public static int camoffset;
	public static int camyoffset;
	public static int OREFIELDS = 10;
	public static int OVERWORLD = 1;
	public static int UNDERWORLD = 0;
	public static int MAXSAVESLOTS = 4;
	public static int CROPS;
	public static boolean tonext = false;
	public static int tempwx, tempwy;
	public static boolean encounter = false;

	static Vector2[] rcoords;
	static Utils sm = new Utils();
	GameSaver gs;

	static int countRcells() {
		int i = 0;
		int ii = 0, cl = 0;
		while (i < MadSand.MAPSIZE + MadSand.BORDER) {
			while (ii < MadSand.MAPSIZE + MadSand.BORDER) {
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
		while (i < MadSand.MAPSIZE + MadSand.BORDER) {
			while (ii < MadSand.MAPSIZE + MadSand.BORDER) {
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

	public void create() {
		player = new Player(name);
		Utils.out("Starting initialization!");
		setRenderRadius();
		Utils.out("Render area: " + rcoords.length);
		try {
			Resource.eps = new PrintStream(Resource.file);
			PrintStream ge = new PrintStream(new File("MadSandCritical.log"));
			System.setErr(ge);
		} catch (Exception e) {
			e.printStackTrace(Resource.eps);
		}
		createDirs();
		sm.Initf();
		world = new World(10);
		this.objn = new InventoryNames();
		Gui.createBasicSkin();
		Gui.chat = new Label[15];
		int vc = 0;
		while (vc < 15) {
			Gui.chat[vc] = new Label(" ", Gui.skin);
			vc++;
		}

		Gui.gui = new Label[4];
		Gui.gui[0] = new Label("HP: " + player.hp + "/" + player.mhp, Gui.skin);
		Gui.gui[1] = new Label("Level: " + player.lvl, Gui.skin);
		Gui.gui[2] = new Label("Experience: " + player.exp + "/" + player.requiredexp, Gui.skin);
		Gui.gui[3] = new Label("", Gui.skin);
		Gui.log = new Label[10];
		QuestUtils.init();
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.local(SAVEDIR + FONT_PATH));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.characters = "Ã�â„¢Ã�Â¦Ã�Â£Ã�Å¡Ã�â€¢Ã�ï¿½Ã�â€œÃ�Â¨Ã�Â©Ã�â€”Ã�Â¥Ã�ÂªÃ�Â¤Ã�Â«Ã�â€™Ã�ï¿½Ã�Å¸Ã�Â Ã�Å¾Ã�â€ºÃ�â€�Ã�â€“Ã�Â­Ã�Â¯Ã�Â§Ã�Â¡Ã�Å“Ã�ËœÃ�Â¢Ã�Â¬Ã�â€˜Ã�Â®Ã�ï¿½Ã�Â¹Ã‘â€ Ã‘Æ’Ã�ÂºÃ�ÂµÃ�Â½Ã�Â³Ã‘Ë†Ã‘â€°Ã�Â·Ã‘â€¦Ã‘Å Ã‘â€žÃ‘â€¹Ã�Â²Ã�Â°Ã�Â¿Ã‘â‚¬Ã�Â¾Ã�Â»Ã�Â´Ã�Â¶Ã‘ï¿½Ã‘ï¿½Ã‘â€¡Ã‘ï¿½Ã�Â¼Ã�Â¸Ã‘â€šÃ‘Å’Ã�Â±Ã‘Å½Ã‘â€˜abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\\\/?-+=()*&.;:,{}\\\"'<>";
		parameter.size = 24;
		parameter.color = Color.BLUE;
		Gui.font1 = generator.generateFont(parameter);
		generator.dispose();
		Gdx.graphics.setContinuousRendering(true);
		int cxxc = 0;
		while (cxxc < 10) {
			Gui.log[cxxc] = new Label(" ", Gui.skin);
			cxxc++;
		}
		sm.ubound = (World.GetParam(MadSand.MAPSIZE) - 33);
		sm.lbound = (World.GetParam(MadSand.MAPSIZE) - 33);
		camera = new OrthographicCamera();
		this.invcamera = new OrthographicCamera();
		this.invbatch = new SpriteBatch();
		camera.update();
		Gui.font = new BitmapFont();
		Utils.ppos.x = (x * 33);
		Utils.ppos.y = (y * 33);
		Gui.equip = new Image[5];
		Gui.equip[0] = new Image(new TextureRegionDrawable(new TextureRegion(sm.placeholder)));
		Gui.equip[1] = new Image(new TextureRegionDrawable(new TextureRegion(sm.placeholder)));
		Gui.equip[2] = new Image(new TextureRegionDrawable(new TextureRegion(sm.placeholder)));
		Gui.equip[3] = new Image(new TextureRegionDrawable(new TextureRegion(sm.placeholder)));
		Gui.equip[4] = new Image(new TextureRegionDrawable(new TextureRegion(sm.placeholder)));
		Gui.initmenu();
		Gui.font.getData().markupEnabled = true;
		Gui.font1.getData().markupEnabled = true;
		Utils.out("End of initialization!");
	}

	public static void setName(String arg) {
		name = arg;
	}

	public static void setParams(int radius, int mapsize) {
		renderradius = radius * MadSand.TILESIZE;
		setRenderRadius();
		MAPSIZE = mapsize;
	}

	static void setUpScene() {
		Utils.out("Setting starting scene up!");
		world.getCurLoc().addObject(50, 49, 6);
	}

	public void updateCamToxy(float f, float y2) {
		camera.position.set(f + 17.0F + camoffset, y2 + 37.0F + camyoffset, 0.0F);
		if (camera.position.y < 260.0F)
			camera.position.y = 250.0F;
		if (camera.position.x < 320.0F)
			camera.position.x = 320.0F;
		if (camera.position.x > (MAPSIZE * 33) + 100)
			camera.position.x = (MAPSIZE * 33) + 100;
		if (camera.position.y > (MAPSIZE * 33) + 100)
			camera.position.y = (MAPSIZE * 33) + 100;
		camera.viewportWidth = (Gdx.graphics.getWidth() / ZOOM);
		camera.viewportHeight = (Gdx.graphics.getHeight() / ZOOM);
		camera.update();

		mouseinworld.set(Gdx.input.getX(), Gdx.input.getY(), 0.0F);
		camera.unproject(mouseinworld);
		sm.batch.setProjectionMatrix(camera.combined);
	}

	public static double calcDistance(int x1, int y1, int x2, int y2) {
		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}

	static int trdx, trdy;

	void DrawGame() {
		try {
			int i = 0;
			while (i < rcoords.length) {
				sm.batch.draw(sm.tile[world.rend(y + (int) rcoords[i].y, x + (int) rcoords[i].x)],
						Utils.ppos.x + rcoords[i].x * 33, Utils.ppos.y + rcoords[i].y * 33);
				i++;
			}
			if (Player.isCollisionMask(x, y)) {
				drawPlayer();
			}
			i = 0;
			while (i < rcoords.length) {
				trdx = x + (int) rcoords[i].x;
				trdy = y + (int) rcoords[i].y;
				if (trdx < 0)
					trdx = 0;
				if (trdy < 0)
					trdy = 0;
				if (trdx >= MAPSIZE + BORDER)
					trdx = MAPSIZE + BORDER;
				if (trdy >= MAPSIZE + BORDER)
					trdy = MAPSIZE + BORDER;
				int objid = world.getCurLoc().getObject(trdx, trdy).id;
				if ((objid != 0) && (objid != 666)) {
					sm.batch.draw(sm.objects[objid], Utils.ppos.x + (int) rcoords[i].x * 33,
							Utils.ppos.y + (int) rcoords[i].y * 33);
				}
				if (Player.standingOnLoot(trdx, trdy)) {
					sm.batch.draw(sm.objects[7], Utils.ppos.x + (int) rcoords[i].x * 33,
							Utils.ppos.y + (int) rcoords[i].y * 33);
				}
				/*if (MobLayer.getMobId(trdx, trdy) != 0) {
					sm.batch.draw(sm.npc[MobLayer.getMobId(trdx, trdy)], Utils.ppos.x + (int) rcoords[i].x * 33,
							Utils.ppos.y + (int) rcoords[i].y * 33);
				}*/

				i++;
			}
			if (!Player.isCollisionMask(x, y)) {
				drawPlayer();
			}
			i = 0;
			sm.batch.draw(sm.mapcursor, wmx * 33, wmy * 33);
			sm.batch.end();
			Gui.gui[0].setText("HP: " + player.hp + "/" + player.mhp);
			Gui.gui[1].setText("Level: " + player.lvl);
			Gui.gui[2].setText("Experience: " + player.exp + "/" + player.requiredexp);
			Gui.gui[3].setText("Hand: " + sm.getItem(player.hand));

			sm.batch.begin();
		} catch (Exception e) {
			e.printStackTrace(Resource.eps);
			Utils.out("Render error. See errorlog.");
		}
	}

	void drawWorld() {
		int i = 0;
		while (i < rcoords.length) {
			sm.batch.draw(sm.tile[world.rend(y + (int) rcoords[i].y, x + (int) rcoords[i].x)],
					Utils.ppos.x + rcoords[i].x * 33, Utils.ppos.y + rcoords[i].y * 33);
			i++;
		}
		sm.batch.draw(sm.Splayer, Utils.ppos.x, Utils.ppos.y + stepy);
		i = 0;
	}

	public static void teleport(int x, int y) {
		Utils.ppos.x = (x * 33);
		Utils.ppos.y = (y * 33);
		MadSand.x = x;
		MadSand.y = y;
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
				MadSand.state = "GAME";
				MadSand.dialogresult = 0;
				MadSand.maindialog.setVisible(false);
				if (lott)
					MadSand.showDialog(type, ads, qid);
			}
		});
		Gui.refuseD.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.state = "GAME";
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
		return arg * 33;
	}

	void drawPlayer() {
		if (stepping) {
			this.elapsedTime += Gdx.graphics.getDeltaTime();
			if (player.look == Direction.RIGHT) {
				sm.batch.draw((TextureRegion) Utils.ranim.getKeyFrame(this.elapsedTime, true), Utils.ppos.x - stepx,
						Utils.ppos.y);
				updateCamToxy(Utils.ppos.x - stepx, Utils.ppos.y);
			}
			if (player.look == Direction.LEFT) {
				sm.batch.draw((TextureRegion) Utils.lanim.getKeyFrame(this.elapsedTime, true), Utils.ppos.x + stepx,
						Utils.ppos.y);
				updateCamToxy(Utils.ppos.x + stepx, Utils.ppos.y);
			}
			if (player.look == Direction.UP) {
				sm.batch.draw((TextureRegion) Utils.uanim.getKeyFrame(this.elapsedTime, true), Utils.ppos.x,
						Utils.ppos.y - stepy);
				updateCamToxy(Utils.ppos.x, Utils.ppos.y - stepy);
			}
			if (player.look == Direction.DOWN) {
				sm.batch.draw((TextureRegion) Utils.danim.getKeyFrame(this.elapsedTime, true), Utils.ppos.x,
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
			sm.batch.draw(sm.Splayer, Utils.ppos.x, Utils.ppos.y);
			updateCamToxy(Utils.ppos.x, Utils.ppos.y);
		}
	}

	public static void createDirs() {
		if (!new File("MadSand_Saves/").exists()) {
			new File("MadSand_Saves/").mkdirs();
		}

		if (!new File("MadSand_Saves/worlds").exists()) {
			new File("MadSand_Saves/worlds").mkdirs();
		}
		if (!new File("MadSand_Saves/scripts").exists()) {
			new File("MadSand_Saves/scripts").mkdirs();
		}
	}

	public static void print(String arg) {
		String ar = Gui.log[0].getText().toString();
		if (!ar.equals(arg)) {
			int i = Gui.log.length - 1;
			while (i >= 0) {
				if (i != 0)
					Gui.log[i].setText(Gui.log[i - 1].getText());
				else
					Gui.log[i].setText(arg);
				;
				i--;
			}
		}
	}

	public void render() {
		if (state.equals("GAME")) {
			Gdx.input.setInputProcessor(Gui.overlay);
			sm.checkFocus();
			if (Gui.overlay.getKeyboardFocus() != Gui.msgf && !charcrt) {
				Utils.updMouseCoords();
				sm.mouseMovement();
				sm.KeyCheck();
				//TODO: loot collision
				sm.InvKeyCheck();
			}
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			sm.batch.begin();
			DrawGame();
			Gui.overlay.draw();
			Gui.overlay.act();
			sm.batch.end();
		} else if (state.equals("INVENTORY")) {
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			sm.batch.begin();
			DrawGame();
			sm.batch.end();

			this.invbatch.begin();
			this.invcamera.position.set(314.0F, 235.0F, 0.0F);
			this.invcamera.viewportWidth = ((float) (Gdx.graphics.getWidth() / 0.97D));
			this.invcamera.viewportHeight = ((float) (Gdx.graphics.getHeight() / 0.97D));
			this.invcamera.update();
			mouseinworld.set(Gdx.input.getX(), Gdx.input.getY(), 0.0F);
			this.invcamera.unproject(mouseinworld);
			this.invbatch.setProjectionMatrix(this.invcamera.combined);
			this.invbatch.draw(sm.inv, 0.0F, 0.0F);
			int i = 0;
			while (i < 30) {
				if (inv[i][0] != 0)
					this.invbatch.draw(Utils.item[inv[i][0]], InvUtils.getItemCellCoord(i).x,
							InvUtils.getItemCellCoord(i).y);
				if (inv[i][0] != 0)
					Gui.font.draw(this.invbatch, " " + inv[i][1], InvUtils.getItemCellCoord(i).x,
							InvUtils.getItemCellCoord(i).y + Gui.font.getLineHeight());
				i++;
			}
			this.invbatch.draw(Utils.cursor, cx, cy);
			sm.InvKeyCheck();
			sm.inInvKeyCheck();
			sm.checkInvKeys();
			this.invbatch.end();
			if (!tradeflag) {
				Gui.overlay.act();
				Gui.overlay.draw();
			}
		} else if (state.equals("BUY")) {
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			sm.batch.begin();
			DrawGame();
			sm.batch.end();
			this.invbatch.begin();
			this.invcamera.position.set(314.0F, 235.0F, 0.0F);
			this.invcamera.update();
			mouseinworld.set(Gdx.input.getX(), Gdx.input.getY(), 0.0F);
			this.invcamera.unproject(mouseinworld);
			this.invbatch.setProjectionMatrix(this.invcamera.combined);
			this.invbatch.draw(sm.inv, 0.0F, 0.0F);
			int i = 0;
			while (i < 30) {
				if (trade[i][0] != 0)
					this.invbatch.draw(Utils.item[trade[i][0]], InvUtils.getItemCellCoord(i).x,
							InvUtils.getItemCellCoord(i).y);
				if (trade[i][0] != 0)
					Gui.font.draw(this.invbatch, " " + trade[i][1], InvUtils.getItemCellCoord(i).x,
							InvUtils.getItemCellCoord(i).y + Gui.font.getLineHeight());
				i++;
			}
			this.invbatch.draw(Utils.cursor, cx, cy);
			sm.tradeCheckKeys();
			sm.checkInvKeys();
			this.invbatch.end();
			Gui.overlay.act();
			Gui.overlay.draw();
		} else if (state.equals("NMENU")) {
			Gdx.gl.glClearColor(1.0F, 1.0F, 1.0F, 1.0F);
			Gdx.gl.glClear(16384);
			sm.batch.begin();
			drawWorld();
			sm.batch.end();
			Gui.stage.act();
			Gui.stage.draw();
		} else if (state.equals("LAUNCHER")) {
			if (this.started) {
				Utils.out("Everything's loaded! I'm in launcher state!");
				this.started = false;
				updateCamToxy(Utils.ppos.x, Utils.ppos.y);
			}
			camera.viewportWidth = (Gdx.graphics.getWidth() / ZOOM);
			camera.viewportHeight = (Gdx.graphics.getHeight() / ZOOM);
			camera.update();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			sm.batch.begin();
			drawWorld();
			this.percent = (player.hp * 100 / player.mhp);
			sm.batch.end();
			Gui.launch.draw();
			Gui.launch.act();
		} else if (state.equals("WORLDGEN")) {
			sm.batch.begin();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			updateCamToxy(Utils.ppos.x, Utils.ppos.y);
			drawWorld();
			Gui.worldg.draw();
			sm.batch.end();
		} else if (state.equals("LOAD")) {
			sm.batch.begin();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			drawWorld();
			Gui.loadg.draw();
			sm.batch.end();
		} else if (state.equals("GOTO")) {
			sm.batch.begin();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			drawWorld();
			Gui.gotodg.draw();
			sm.batch.end();
		} else if (state.equals("DEAD")) {
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			sm.batch.begin();
			DrawGame();
			sm.batch.end();
			Gui.dead.act();
			Gui.dead.draw();
		} else if (state.equals("MSG")) {
			camera.position.set(0.0F, 0.0F, 0.0F);
			sm.batch.setProjectionMatrix(camera.combined);
			camera.update();
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			Gdx.gl.glClear(16384);
			sm.batch.begin();
			DrawGame();
			sm.batch.end();
		} else if (state.equals("CRAFT")) {
			Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
			sm.batch.begin();
			DrawGame();
			sm.batch.end();
			Gui.craft.act();
			Gui.craft.draw();
		}
	}

	public static void setTime(int arg) {
		wtime = arg;
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