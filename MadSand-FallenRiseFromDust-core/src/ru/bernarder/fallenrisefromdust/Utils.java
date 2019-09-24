package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.enums.GameState;
import ru.bernarder.fallenrisefromdust.properties.ItemProp;
import ru.bernarder.fallenrisefromdust.properties.ObjectProp;
import ru.bernarder.fallenrisefromdust.properties.TileProp;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Utils {
	public static boolean tester = true;
	public static int ubound;
	public static int lbound;
	public static float x = MadSand.player.x * 33;
	public static float y = MadSand.player.y * 33;
	public static Vector2 ppos;
	public static boolean invent = false;
	public static float pspeed = 33.0F;
	static SpriteBatch batch;
	static Texture hp;
	static Texture dtex;
	static Texture utex;
	static Texture ltex;
	static Texture rtex;
	static Texture inv;
	static Texture dark;
	static Texture curs;
	static Texture placeholder;
	static Sprite cursor;
	static Sprite Splayer;
	Texture[] lgt = new Texture[7];
	static Texture[] item;
	static Texture[] objects;
	static Texture[] tile;
	static Texture[] npc;
	static boolean admin = true;
	static TextureRegion[] animdown = new TextureRegion[2];
	static TextureRegion[][] tmpAnim;
	static Texture animsheet;
	static TextureRegion[] animup = new TextureRegion[2];
	static TextureRegion[] animleft = new TextureRegion[2];
	static TextureRegion[] animright = new TextureRegion[2];
	static Animation<TextureRegion> uanim;
	static Animation<TextureRegion> danim;
	static Animation<TextureRegion> lanim;
	static Texture mapcursor;
	public static Random random = new Random();
	static Animation<TextureRegion> ranim;
	static int selected;
	static Document resdoc;

	public static Document XMLString(String xml) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xml));
			return builder.parse(is);
		} catch (Exception e) {
			out("Oopsie (" + e.getMessage() + ")");
			e.printStackTrace();
			return null;
		}
	}

	static int countKeys(Document doc, String list) {
		try {
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(list);
			int temp, c = 0;
			for (temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					c++;
				}
			}
			return c;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	static String getKey(Document doc, String list, String id, String element) {
		try {
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(list);
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (eElement.getAttribute("id").equals(id)) {
						return eElement.getElementsByTagName(element).item(0).getTextContent();
					}
				}
			}
			return "-1";
		} catch (Exception e) {
			return "-1";
		}
	}

	public static void Initf() {
		MadSand.gameVrf = getSHA1(new File(MadSand.RESFILE));
		out("Resfile hash: " + MadSand.gameVrf);
		resdoc = XMLString(GameSaver.getExternalNl(MadSand.RESFILE));
		MadSand.QUESTS = countKeys(resdoc, "quest");
		MadSand.quests = new int[MadSand.QUESTS][2];
		mapcursor = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/cur.png"));
		animsheet = new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/anim.png"));
		tmpAnim = TextureRegion.split(animsheet, 35, 74);
		animdown[0] = tmpAnim[0][0];
		animdown[1] = tmpAnim[0][1];
		animleft[0] = tmpAnim[1][0];
		animleft[1] = tmpAnim[1][1];
		animright[0] = tmpAnim[2][0];
		animright[1] = tmpAnim[2][1];
		animup[0] = tmpAnim[3][0];
		animup[1] = tmpAnim[3][1];
		uanim = new Animation<TextureRegion>(0.2F, animup);
		danim = new Animation<TextureRegion>(0.2F, animdown);
		lanim = new Animation<TextureRegion>(0.2F, animleft);
		ranim = new Animation<TextureRegion>(0.2F, animright);
		com.badlogic.gdx.graphics.Cursor customCursor = Gdx.graphics
				.newCursor(new com.badlogic.gdx.graphics.Pixmap(Gdx.files.local(MadSand.SAVEDIR + "cursor.png")), 0, 0);
		Gdx.graphics.setCursor(customCursor);
		MadSand.LASTITEMID = countKeys(resdoc, "item");
		MadSand.CROPS = countKeys(resdoc, "stages");

		out(MadSand.CROPS + " crops");
		MadSand.LASTOBJID = countKeys(resdoc, "object");
		out(MadSand.LASTOBJID + " objects");
		CropLayer.stages = new String[MadSand.LASTOBJID];
		MadSand.LASTTILEID = countKeys(resdoc, "tile");
		MadSand.NPCSPRITES = countKeys(resdoc, "npc");
		MadSand.CRAFTABLES = countKeys(resdoc, "recipe");
		out(MadSand.CRAFTABLES + " craftable items");
		out(MadSand.LASTTILEID + " tiles");
		out(MadSand.NPCSPRITES + " npcs");
		MadSand.craftableid = new int[MadSand.CRAFTABLES];
		item = new Texture[MadSand.LASTITEMID + 1];
		objects = new Texture[MadSand.LASTOBJID];
		tile = new Texture[MadSand.LASTTILEID + 1];
		npc = new Texture[MadSand.NPCSPRITES + 1];
		int i = 0, cc = 0;
		while (i < MadSand.LASTITEMID) {
			item[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "inv/" + i + ".png"));
			if (!getKey(resdoc, "item", "" + i, "recipe").equals("-1")) {
				MadSand.craftableid[cc] = i;
				cc++;
			}
			if (!getKey(resdoc, "item", "" + i, "stages").equals("-1")) {
				CropLayer.stages[i] = getKey(resdoc, "item", "" + i, "stages");
			}
			ItemProp.name.put(i, getKey(resdoc, "item", "" + i, "name"));
			ItemProp.type.put(i, Integer.parseInt(getKey(resdoc, "item", "" + i, "type")));
			ItemProp.altObject.put(i, Integer.parseInt(getKey(resdoc, "item", "" + i, "altobject")));
			ItemProp.cost.put(i, Integer.parseInt(getKey(resdoc, "item", "" + i, "cost")));
			ItemProp.craftable.put(i, Integer.parseInt(getKey(resdoc, "item", "" + i, "craftable")) != 0);
			ItemProp.recipe.put(i, getKey(resdoc, "item", "" + i, "recipe"));
			ItemProp.heal.put(i, getKey(resdoc, "item", "" + i, "heal"));
			ItemProp.useAction.put(i, getKey(resdoc, "item", "" + i, "onuse"));
			i++;
		}
		i = 0;
		while (i < MadSand.LASTOBJID) {
			objects[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "obj/" + i + ".png"));
			ObjectProp.name.put(i, getKey(resdoc, "object", "" + i, "name"));
			ObjectProp.hp.put(i, Integer.parseInt(getKey(resdoc, "object", "" + i, "tough")));
			ObjectProp.altitems.put(new Tuple<Integer, String>(i, "altitem"),
					getKey(resdoc, "object", "" + i, "altitem"));
			ObjectProp.altitems.put(new Tuple<Integer, String>(i, "hand"), getKey(resdoc, "object", "" + i, "hand"));
			ObjectProp.altitems.put(new Tuple<Integer, String>(i, "skillbonus"),
					getKey(resdoc, "object", "" + i, "skillbonus"));
			ObjectProp.vRendMasks.put(i, Integer.parseInt(getKey(resdoc, "object", "" + i, "vmask")));
			ObjectProp.hRendMasks.put(i, Integer.parseInt(getKey(resdoc, "object", "" + i, "hmask")));
			ObjectProp.interactAction.put(i, getKey(resdoc, "object", "" + i, "oninteract"));
			i++;
		}
		i = 0;
		while (i < MadSand.LASTTILEID) {
			tile[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "terrain/" + i + ".png"));
			TileProp.name.put(i, getKey(resdoc, "tile", "" + i, "name"));
			TileProp.damage.put(i, Integer.parseInt(getKey(resdoc, "tile", "" + i, "damage")));
			i++;
		}
		i = 0;
		while (i < MadSand.NPCSPRITES) {
			npc[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "npc/" + i + ".png"));
			getKey(resdoc, "npc", "" + i, "hp");
			getKey(resdoc, "npc", "" + i, "maxhp");
			getKey(resdoc, "npc", "" + i, "rewardexp");
			getKey(resdoc, "npc", "" + i, "drop");
			getKey(resdoc, "npc", "" + i, "name");
			getKey(resdoc, "npc", "" + i, "atk");
			getKey(resdoc, "npc", "" + i, "accuracy");
			getKey(resdoc, "npc", "" + i, "friendly");
			getKey(resdoc, "npc", "" + i, "fraction");
			getKey(resdoc, "npc", "" + i, "spawnonce");
			getKey(resdoc, "npc", "" + i, "qids");
			i++;
		}

		hp = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/heart.png"));
		dark = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/darkness.png"));
		curs = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/cursor.png"));
		placeholder = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/placeholder.png"));
		inv = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/invsheet.png"));
		ppos = new Vector2(x, y);
		com.badlogic.gdx.files.FileHandle pfhandle = Gdx.files.local(MadSand.SAVEDIR + "player/d1.png");
		batch = new com.badlogic.gdx.graphics.g2d.SpriteBatch();
		dtex = new Texture(pfhandle);
		pfhandle = Gdx.files.local(MadSand.SAVEDIR + "player/u1.png");
		utex = new Texture(pfhandle);
		pfhandle = Gdx.files.local(MadSand.SAVEDIR + "player/r1.png");
		rtex = new Texture(pfhandle);
		pfhandle = Gdx.files.local(MadSand.SAVEDIR + "player/l1.png");
		ltex = new Texture(pfhandle);
		cursor = new Sprite(curs);
		Splayer = new Sprite(dtex);
	}

	public static String getItem(int id) {
		return ItemProp.name.get(id);
	}

	static void toggleInventory() {
		if (invent) {
			funcButtonsSet(false);
			Gdx.input.setInputProcessor(Gui.overlay);
			MadSand.contextopened = false;
			MadSand.state = GameState.GAME;
			Gui.mousemenu.setVisible(true);
			Gui.overlay.setDebugAll(false);
			MadSand.player.inventory.inventoryUI.toggleVisible();
			invent = false;
		} else {
			Gui.overlay.setDebugAll(true);
			MadSand.player.inventory.inventoryUI.toggleVisible();
			Gui.gamecontext.setVisible(false);
			MadSand.contextopened = false;
			Gui.mousemenu.setVisible(false);
			funcButtonsSet(true);
			Gdx.input.setInputProcessor(Gui.overlay);
			MadSand.state = GameState.INVENTORY;
			invent = true;
		}
	}

	public static void InvKeyCheck() {
		if (Gdx.input.isKeyJustPressed(Keys.E)) {
			toggleInventory();
		}
	}

	public static void funcButtonsSet(boolean visible) {
		Gui.craftButton.setVisible(visible);
		Gui.exitButton.setVisible(visible);
	}

	public static void inInvKeyCheck() {
		// TODO remove dis
		if (Gdx.input.isKeyJustPressed(66)) {
			inventoryAction();
		}
	}

	public static void inventoryAction() {
		// TODO
	}

	public static void isInFront() {
		int obj = MadSand.world.getObjID(MadSand.player.x, MadSand.player.y, MadSand.player.stats.look);
		if ((obj != 666) && (obj != 0)) {
			MadSand.print("You see: " + ObjectProp.name.get(obj));
		}
	}

	public static void KeyCheck() {
		if (Gdx.input.isKeyJustPressed(Keys.GRAVE))
			Gui.inputField.setVisible(!Gui.inputField.isVisible());
		if (Gdx.input.isKeyJustPressed(Keys.Q))
			Gui.showStatsWindow();
		if (Gdx.input.isKeyJustPressed(Keys.ENTER))
			MadSand.player.interact(MadSand.player.stats.look);
		if (Gdx.input.isKeyPressed(Keys.NUMPAD_3))
			MadSand.ZOOM = (float) (MadSand.ZOOM + 0.01D);
		if (Gdx.input.isKeyPressed(Keys.NUMPAD_1))
			MadSand.ZOOM = (float) (MadSand.ZOOM - 0.01D);
		if (Gdx.input.isKeyPressed(Keys.NUMPAD_4))
			MadSand.camxoffset -= 2;
		if (Gdx.input.isKeyPressed(Keys.NUMPAD_6))
			MadSand.camxoffset += 2;
		if (Gdx.input.isKeyPressed(Keys.NUMPAD_8))
			MadSand.camyoffset += 2;
		if (Gdx.input.isKeyPressed(Keys.NUMPAD_2))
			MadSand.camyoffset -= 2;
		if (Gdx.input.isKeyPressed(Keys.NUMPAD_5)) {
			MadSand.camyoffset = 0;
			MadSand.camxoffset = 0;
		}
		if (Gdx.input.isKeyJustPressed(Keys.U)) {
			MadSand.player.useItem();
		}
		if (Gdx.input.isKeyJustPressed(Keys.UP) && (!MadSand.stepping)) {
			turn(Direction.UP);
		}
		if (Gdx.input.isKeyJustPressed(Keys.DOWN) && (!MadSand.stepping)) {
			turn(Direction.DOWN);
		}
		if (Gdx.input.isKeyJustPressed(Keys.LEFT) && (!MadSand.stepping)) {
			turn(Direction.LEFT);
		}
		if (Gdx.input.isKeyJustPressed(Keys.RIGHT) && (!MadSand.stepping)) {
			turn(Direction.RIGHT);
		}
		if (Gdx.input.isKeyJustPressed(Keys.N) && MadSand.world.curxwpos != 0 && MadSand.world.curywpos != 0) {
			if (MadSand.player.x == World.MAPSIZE - 1 && MadSand.player.stats.look == Direction.RIGHT)
				gotoSector("right");
			if (MadSand.player.y == World.MAPSIZE - 1 && MadSand.player.stats.look == Direction.UP)
				gotoSector("up");
			if (MadSand.player.x == World.BORDER && MadSand.player.stats.look == Direction.LEFT)
				gotoSector("left");
			if (MadSand.player.y == World.BORDER && MadSand.player.stats.look == Direction.DOWN)
				gotoSector("down");

		}

		if ((Gdx.input.isKeyJustPressed(Keys.Y)) && (tester)) {
			MadSand.teleport(MadSand.wmx, MadSand.wmy);
		}
		if ((Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) && (Gdx.input.isKeyPressed(Keys.R)) && (tester)) {
			MadSand.world.Generate();
		}
		if ((Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) && (Gdx.input.isKeyPressed(Keys.DOWN)) && (tester)) {
			MadSand.world.curlayer = 1;
		}
		if ((Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) && (Gdx.input.isKeyPressed(Keys.UP)) && (tester)) {
			MadSand.world.curlayer = 0;
		}
		if (Gdx.input.isKeyJustPressed(Keys.F11)) {
			Boolean fullScreen = Boolean.valueOf(Gdx.graphics.isFullscreen());
			Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
			if (fullScreen.booleanValue()) {
				Gdx.graphics.setWindowedMode(1280, 720);
			} else
				Gdx.graphics.setFullscreenMode(currentMode);
		}
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			Gui.resumeButton.setVisible(true);
			Gdx.input.setInputProcessor(Gui.menu);
			MadSand.state = GameState.NMENU;
		}
		if ((Gdx.input.isKeyJustPressed(Keys.G)) && (tester)) {
			GameSaver saver = new GameSaver();
			saver.saver.start();
		}
		if ((Gdx.input.isKeyJustPressed(Keys.L)) && (tester)) {
			GameSaver.loadWorld(MadSand.WORLDNAME);
		}
		if ((Gdx.input.isKeyJustPressed(Keys.H)) && (tester)) {
			MadSand.player.damage(10);
		}
		if ((Gdx.input.isKeyJustPressed(Keys.F)) && (MadSand.player.stats.hand != 0)) {
			MadSand.player.stats.hand = 0;
			MadSand.print("You freed your hands.");
			Gui.equip[4].setDrawable(new SpriteDrawable(new Sprite(cursor)));
		}
		if ((Gdx.input.isKeyPressed(Keys.A)) && (!MadSand.stepping)) {
			walk(Direction.LEFT);
		}
		if ((Gdx.input.isKeyPressed(Keys.D)) && (!MadSand.stepping)) {
			walk(Direction.RIGHT);
		}
		if ((Gdx.input.isKeyPressed(Keys.W)) && (!MadSand.stepping)) {
			walk(Direction.UP);
		}
		if ((Gdx.input.isKeyPressed(Keys.S)) && (!MadSand.stepping)) {
			walk(Direction.DOWN);
		}
	}

	static void walk(Direction dir) {
		MadSand.player.stats.look = dir;
		turn(MadSand.player.stats.look);
		if (!VerifyPosition(MadSand.player.stats.look))
			move(MadSand.player.stats.look);
		isInFront();
	}

	static void mouseMovement() {
		if ((Gdx.input.isButtonPressed(0)) && (MadSand.state == GameState.GAME) && (!MadSand.stepping)
				&& (!MadSand.contextopened)) {
			if (MadSand.wmx > MadSand.player.x)
				walk(Direction.RIGHT);
			else if (MadSand.wmx < MadSand.player.x)
				walk(Direction.LEFT);
			else if (MadSand.wmy > MadSand.player.y)
				walk(Direction.UP);
			else if (MadSand.wmy < MadSand.player.y)
				walk(Direction.DOWN);
		}
	}

	public static void turn(Direction dir) {
		MadSand.player.stats.look = dir;
		if (!MadSand.stepping) {
			if (dir == Direction.UP) {
				Splayer = new Sprite(utex);
			}
			if (dir == Direction.DOWN) {
				Splayer = new Sprite(dtex);
			}
			if (dir == Direction.LEFT) {
				Splayer = new Sprite(ltex);
			}
			if (dir == Direction.RIGHT) {
				Splayer = new Sprite(rtex);
			}
		}
	}

	public static int rand(int min, int max) {
		return Utils.random.nextInt((max - min) + 1) + min;
	}

	public static void passHour() {
		World.worldtime += 1;
		if (((World.worldtime >= 0) && (World.worldtime <= 5))
				|| ((World.worldtime >= 21) && (World.worldtime <= 23))) {
			// spawn mobs
		}
	}

	public static void makeTurn() {
		tileDmg();
		passHour();
		CropLayer.updCrops();
		if (World.worldtime == 24)
			World.worldtime = 0;
		if (((World.worldtime >= 0) && (World.worldtime <= 5))
				|| ((World.worldtime >= 21) && (World.worldtime <= 23))) {
			Gui.darkness.setVisible(true);
		} else {
			Gui.darkness.setVisible(false);
		}
	}

	public static void tileDmg() {
		int tid = MadSand.world.getTileId(MadSand.player.x, MadSand.player.y);
		int dmg = TileProp.damage.get(tid);
		if (dmg > 0) {
			MadSand.print("You took " + dmg + " damage from " + (TileProp.name.get(tid)));
			MadSand.player.damage(dmg);
		}
	}

	public static void move(Direction dir) {
		if ((!MadSand.player.isCollision(dir, 0)) && (MadSand.dialogflag)) {
			if ((dir == Direction.UP) && (!VerifyPosition(dir))) {
				MadSand.player.y += 1;
				ppos.y += 33.0F;
			}
			if ((dir == Direction.DOWN) && (!VerifyPosition(dir))) {
				MadSand.player.y -= 1;
				ppos.y -= 33.0F;
			}
			if ((dir == Direction.LEFT) && (!VerifyPosition(dir))) {
				MadSand.player.x -= 1;
				ppos.x -= 33.0F;
			}
			if ((dir == Direction.RIGHT) && (!VerifyPosition(dir))) {
				MadSand.player.x += 1;
				ppos.x += 33.0F;
			}
			if (MadSand.player.x == World.MAPSIZE - 1 || MadSand.player.y == World.MAPSIZE - 1
					|| MadSand.player.x == World.BORDER || MadSand.player.y == World.BORDER) {
				MadSand.print("Press [GRAY]N[WHITE] to move to the next sector.");
			}
			MadSand.stepping = true;
		}
	}

	public static boolean VerifyPosition(Direction dir) {
		boolean ret = false;
		if (MadSand.player.x >= World.MAPSIZE - 1 && (dir == Direction.RIGHT)) {
			ret = true;
		}
		if (MadSand.player.y >= World.MAPSIZE - 1 && (dir == Direction.UP)) {
			ret = true;
		}
		if (MadSand.player.x <= 1 && (dir == Direction.LEFT)) {
			ret = true;
		}
		if (MadSand.player.y <= 1 && (dir == Direction.DOWN)) {
			ret = true;
		}
		return ret;
	}

	static void updCoords() {
		ppos.x = (MadSand.player.x * 33);
		ppos.y = (MadSand.player.y * 33);
	}

	public static String gotodir = "";

	public static void gotoSector(String dir) {
		gotodir = dir;
		MadSand.state = GameState.GOT;
		new ThreadedUtils().gotoSector.start();
	}

	public static void updMouseCoords() {
		MadSand.mx = Gdx.input.getX();
		MadSand.my = Gdx.graphics.getHeight() - Gdx.input.getY();
		MadSand.wmx = (int) Math.floor(MadSand.mouseinworld.x / 33.0F);
		MadSand.wmy = (int) Math.floor(MadSand.mouseinworld.y / 33.0F);
		Gui.mousemenu.addAction(
				com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo(MadSand.mx + 60, MadSand.my - 70, 0.1F));
		try {
			Gui.mouselabel[0].setText("World coords: " + MadSand.wmx + ", " + MadSand.wmy);
			Gui.mouselabel[1].setText(
					"Tile: " + TileProp.name.get(MadSand.world.getCurLoc().getTile(MadSand.wmx, MadSand.wmy).id));
			Gui.mouselabel[2].setText("Object: " + " ()");
			Gui.mouselabel[3].setText("Creature: " + " ()");
			Gui.mouselabel[4].setText("Turn: " + MadSand.turn + "\nWorld time: " + World.worldtime
					+ "\nPlayer position: (" + MadSand.player.x + ", " + MadSand.player.y + ")\nStamina: "
					+ Math.round(MadSand.player.stats.stamina));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static Calendar cal = Calendar.getInstance();
	static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	public static void out(String arg) {
		if (tester) {

			System.out.print("[" + sdf.format(cal.getTime()) + "] " + arg + "\n");
		}
	}

	public static void outnonl(String arg) {
		if (tester) {
			System.out.print(arg);
		}
	}

	static String getSHA1(File file) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			FileInputStream fis = new FileInputStream(file);
			byte[] byteArray = new byte[1024];
			int bytesCount = 0;
			while ((bytesCount = fis.read(byteArray)) != -1) {
				digest.update(byteArray, 0, bytesCount);
			}
			;
			fis.close();
			byte[] bytes = digest.digest();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public void checkMsgKeys() {
		if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
			MadSand.state = GameState.GAME;
		}
	}

	public static void checkFocus() {
		if (Gdx.input.isKeyJustPressed(Keys.T)) {
			Gui.overlay.setKeyboardFocus(Gui.inputField);
		}
		if ((Gdx.input.isKeyJustPressed(Keys.ENTER)) && (Gui.overlay.getKeyboardFocus() == Gui.inputField) && tester) {
			String cmd = Gui.inputField.getText().trim();
			try {
				BuildScript.execute(cmd);
			} catch (Exception e) {
				MadSand.print("Syntax error");
			}
			Gui.inputField.setText("");
			Gui.overlay.unfocus(Gui.inputField);
		}
	}
}
