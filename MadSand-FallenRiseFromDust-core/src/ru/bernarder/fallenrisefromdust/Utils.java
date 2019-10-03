package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.enums.GameState;
import ru.bernarder.fallenrisefromdust.enums.ItemType;
import ru.bernarder.fallenrisefromdust.properties.CropProp;
import ru.bernarder.fallenrisefromdust.properties.ItemProp;
import ru.bernarder.fallenrisefromdust.properties.ObjectProp;
import ru.bernarder.fallenrisefromdust.properties.TileProp;
import ru.bernarder.fallenrisefromdust.properties.WorldGenProp;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Utils {
	public static boolean tester = true;
	public static int ubound;
	public static int lbound;
	public static float x = World.player.x * 33;
	public static float y = World.player.y * 33;
	public static boolean invent = false;
	public static float pspeed = 33.0F;
	static SpriteBatch batch;
	static Texture dtex;
	static Texture utex;
	static Texture ltex;
	static Texture rtex;
	static Texture dark;
	static Texture curs;
	static Texture placeholder;
	static Sprite cursor;
	static Sprite Splayer;
	Texture[] lgt = new Texture[7];
	public static Texture[] item;
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
	static Document questdoc;
	static Document gendoc;

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

	static String getAttr(Document doc, String list, String id, String attr) {
		try {
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(list);
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (eElement.getAttribute("id").equals(id) || !eElement.hasAttribute("id")) {
						return eElement.getAttribute(attr);
					}
				}
			}
			return "-1";
		} catch (Exception e) {
			return "-1";
		}
	}

	static String nodeMapDump(NamedNodeMap map) {
		if (map == null)
			return "-1";
		String ret = "";
		int len = map.getLength();
		for (int i = 1; i < len; ++i) {
			Node attr = map.item(i);
			ret += attr.getNodeValue();
			if (i < len - 1)
				ret += ",";
		}
		if (ret.equals("")) {
			out("nodemapdump oopsie");
			return "-1";
		}
		// out("ret: " + ret);
		return ret;
	}

	static HashMap<String, Integer> nodeMapToHashMap(NamedNodeMap map) {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		if (map == null) {
			ret.put("tid", -1);
			return ret;
		}
		int len = map.getLength();
		for (int i = 0; i < len; ++i) {
			Node attr = map.item(i);
			ret.put(attr.getNodeName(), val(attr.getNodeValue()));
			// out(attr.getNodeName() + ": " + attr.getNodeValue());
		}
		return ret;
	}

	static NamedNodeMap getNested(Document doc, String list, String id, String name, String iid) {
		try {
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(list);
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (eElement.getAttribute("id").equals(id)) {
						NodeList cList = eElement.getChildNodes();
						for (int pos = 0; pos < cList.getLength(); pos++) {
							Node cNode = cList.item(pos);
							if (cNode.getNodeType() == Node.ELEMENT_NODE) {
								Element cElement = (Element) cNode;
								if (cElement.getTagName().equals(name) && cElement.getAttribute("id").equals(iid)) {
									return (cElement.getAttributes());
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
		return null;
	}

	static String getAttrValues(Document doc, String list, String id, String name, String iid) {
		return nodeMapDump(getNested(doc, list, id, name, iid));
	}

	static Vector<String> getGroup(int biome, String gname) {
		Vector<String> group = new Vector<String>();
		int j = 0;
		String tmp = "";
		while (!tmp.equals("-1")) {
			tmp = getAttrValues(gendoc, "biome", str(biome), gname, str(j));
			if (tmp.equals("-1"))
				break;
			group.add(tmp);
			++j;
		}
		return group;
	}

	public static void Initf() {
		MadSand.gameVrf = getSHA1(new File(MadSand.RESFILE));
		out("Resfile hash: " + MadSand.gameVrf);
		resdoc = XMLString(GameSaver.getExternalNl(MadSand.RESFILE));
		questdoc = XMLString(GameSaver.getExternalNl(MadSand.QUESTFILE));
		gendoc = XMLString(GameSaver.getExternalNl(MadSand.GENFILE));
		MadSand.QUESTS = countKeys(questdoc, "quest");
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
		MadSand.LASTTILEID = countKeys(resdoc, "tile");
		MadSand.NPCSPRITES = countKeys(resdoc, "npc");
		MadSand.CRAFTABLES = countKeys(resdoc, "recipe");
		MadSand.BIOMES = countKeys(gendoc, "biome");
		out(MadSand.BIOMES + " biomes");
		out(MadSand.CRAFTABLES + " craftable items");
		out(MadSand.LASTTILEID + " tiles");
		out(MadSand.NPCSPRITES + " npcs");
		MadSand.craftableid = new int[MadSand.CRAFTABLES];
		item = new Texture[MadSand.LASTITEMID + 1];
		objects = new Texture[MadSand.LASTOBJID];
		tile = new Texture[MadSand.LASTTILEID + 1];
		npc = new Texture[MadSand.NPCSPRITES + 1];
		String stgs, stglen;
		String[] cont;
		Vector<Integer> stages, slens;
		int i = 0, cc = 0;

		// Loading worldgen config
		Vector<Integer> def;
		Vector<String> group;
		HashMap<String, Integer> lake;

		Vector<String> objGroup;
		Vector<String> ore = new Vector<String>();
		String defT, defO;
		HashMap<String, Integer> vdungeon;
		out("Initializing worldgen...");
		while (i < MadSand.BIOMES) {
			def = new Vector<Integer>();
			lake = new HashMap<String, Integer>();

			WorldGenProp.name.add(getAttr(gendoc, "biome", str(i), "name"));
			group = getGroup(i, "tile_group");
			objGroup = getGroup(i, "object_group");

			def.add(Integer.parseInt(getAttrValues(gendoc, "biome", str(i), "def_tile", str(-1))));
			lake = nodeMapToHashMap(getNested(gendoc, "biome", str(i), "lake", str(-1)));

			WorldGenProp.loadTileBlock(i, def, group, lake);
			WorldGenProp.loadObjectBlock(i, objGroup);
			defT = getAttrValues(gendoc, "biome", str(i), "cave_tile", str(-1));
			defO = getAttrValues(gendoc, "biome", str(i), "cave_object", str(-1));
			ore.add(getAttrValues(gendoc, "biome", str(i), "ore", str(-1)));
			vdungeon = nodeMapToHashMap(getNested(gendoc, "biome", str(i), "dungeon", str(-1)));
			WorldGenProp.loadUnderworldBlock(i, defT, defO, ore, vdungeon);
			++i;
		}
		out("Done initializing WorldGen!");
		i = 0;
		// Loading everything about inventory items
		// Craft recipes
		while (i < MadSand.LASTITEMID) {
			item[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "inv/" + i + ".png"));
			if (!getKey(resdoc, "item", "" + i, "recipe").equals("-1")) {
				MadSand.craftableid[cc] = i;
				cc++;
			}

			// Crops
			stgs = getKey(resdoc, "item", "" + i, "stages");
			if (!stgs.equals("-1")) {
				cont = stgs.split("\\,");
				stages = new Vector<Integer>();
				for (String stage : cont)
					stages.add(Integer.parseInt(stage));
				CropProp.stages.put(i, stages);
				stglen = getKey(resdoc, "item", "" + i, "stages");
				cont = stglen.split("\\,");
				slens = new Vector<Integer>();
				for (String slen : cont)
					slens.add(Integer.parseInt(slen));
				CropProp.stagelen.put(i, slens);
			}

			// Item properties
			ItemProp.name.put(i, getKey(resdoc, "item", "" + i, "name"));
			ItemProp.type.put(i, ItemType.get(Integer.parseInt(getKey(resdoc, "item", "" + i, "type"))));
			ItemProp.altObject.put(i, Integer.parseInt(getKey(resdoc, "item", "" + i, "altobject")));
			ItemProp.cost.put(i, Integer.parseInt(getKey(resdoc, "item", "" + i, "cost")));
			ItemProp.craftable.put(i, Integer.parseInt(getKey(resdoc, "item", "" + i, "craftable")) != 0);
			ItemProp.recipe.put(i, getKey(resdoc, "item", "" + i, "recipe"));
			ItemProp.heal.put(i, getKey(resdoc, "item", "" + i, "heal"));
			ItemProp.useAction.put(i, getKey(resdoc, "item", "" + i, "onuse"));
			i++;
		}
		i = 0;

		// Loading map objects
		while (i < MadSand.LASTOBJID) {
			objects[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "obj/" + i + ".png"));
			ObjectProp.name.put(i, getKey(resdoc, "object", str(i), "name"));
			ObjectProp.hp.put(i, Integer.parseInt(getKey(resdoc, "object", str(i), "tough")));
			ObjectProp.harvestHp.put(i, Integer.parseInt(getKey(resdoc, "object", str(i), "harvesthp")));

			ObjectProp.altitems.put(makeTuple(i, "altitem"), getAitem(i, "object"));

			ObjectProp.vRendMasks.put(i, Integer.parseInt(getKey(resdoc, "object", str(i), "vmask")));
			ObjectProp.hRendMasks.put(i, Integer.parseInt(getKey(resdoc, "object", str(i), "hmask")));
			ObjectProp.interactAction.put(i, getKey(resdoc, "object", str(i), "oninteract"));
			i++;
		}
		i = 0;

		// Loading tiles
		while (i < MadSand.LASTTILEID) {
			tile[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "terrain/" + i + ".png"));
			TileProp.name.put(i, getKey(resdoc, "tile", "" + i, "name"));
			TileProp.damage.put(i, val(getKey(resdoc, "tile", "" + i, "damage")));
			i++;
		}
		i = 0;

		// Loading NPCs
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

		// UI resource fuckery
		dark = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/darkness.png"));
		curs = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/cursor.png"));
		placeholder = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/placeholder.png"));
		World.player.globalPos = new PairFloat(x, y);
		FileHandle pfhandle = Gdx.files.local(MadSand.SAVEDIR + "player/d1.png");
		batch = new SpriteBatch();
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

	static HashMap<Integer, Vector<Integer>> getAitem(int id, String field) {
		int i = 0;
		String hand = "";
		HashMap<Integer, Vector<Integer>> ret = new HashMap<Integer, Vector<Integer>>();
		Vector<Integer> block;
		StringTokenizer tok;
		int hid;
		while (hand != "-1") {
			hand = getAttrValues(resdoc, field, str(id), "altitem", str(i));
			if (hand == "-1")
				break;
			Utils.out("altitem for " + field + " id " + id + ": " + hand);
			block = new Vector<Integer>();
			tok = new StringTokenizer(hand, ",");
			while (tok.hasMoreTokens()) {
				block.add(val(tok.nextToken()));
			}
			hid = block.remove(0);
			ret.put(hid, block);
			++i;
		}
		return ret;
	}

	public static Tuple<Integer, String> makeTuple(int key, String val) {
		return new Tuple<Integer, String>(key, val);
	}

	public static String str(int val) {
		return Integer.toString(val);
	}

	public static int val(String str) {
		return Integer.parseInt(str);
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
			World.player.inventory.inventoryUI.toggleVisible();
			invent = false;
		} else {
			World.player.inventory.inventoryUI.toggleVisible();
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

	public static void isInFront() {
		int obj = MadSand.world.getObjID(World.player.x, World.player.y, World.player.stats.look);
		if ((obj != 666) && (obj != 0)) {
			MadSand.print("You see: " + ObjectProp.name.get(obj));
		}
	}

	public static void KeyCheck() {
		if (Gdx.input.isKeyJustPressed(Keys.GRAVE)) {
			Gui.inputField.setVisible(!Gui.inputField.isVisible());
			Gui.overlay.setKeyboardFocus(Gui.inputField);
		}
		if (Gdx.input.isKeyJustPressed(Keys.Q))
			Gui.showStatsWindow();
		if (Gdx.input.isKeyJustPressed(Keys.ENTER))
			World.player.interact(World.player.stats.look);
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
			World.player.useItem();
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
			if (World.player.x == World.MAPSIZE - 1 && World.player.stats.look == Direction.RIGHT)
				gotoSector(World.player.stats.look);
			if (World.player.y == World.MAPSIZE - 1 && World.player.stats.look == Direction.UP)
				gotoSector(World.player.stats.look);
			if (World.player.x == World.BORDER && World.player.stats.look == Direction.LEFT)
				gotoSector(World.player.stats.look);
			if (World.player.y == World.BORDER && World.player.stats.look == Direction.DOWN)
				gotoSector(World.player.stats.look);

		}

		if ((Gdx.input.isKeyJustPressed(Keys.Y)) && (tester)) {
			World.player.teleport(MadSand.wmx, MadSand.wmy);
		}
		if ((Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) && (Gdx.input.isKeyJustPressed(Keys.R)) && (tester)) {
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
			GameSaver.saveWorld();
		}
		if ((Gdx.input.isKeyJustPressed(Keys.L)) && (tester)) {
			GameSaver.loadWorld(MadSand.WORLDNAME);
		}
		if ((Gdx.input.isKeyJustPressed(Keys.H)) && (tester)) {
			World.player.damage(10);
		}
		if ((Gdx.input.isKeyJustPressed(Keys.F)) && (World.player.stats.hand != 0)) {
			World.player.freeHands();
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
		World.player.stats.look = dir;
		turn(World.player.stats.look);
		if (MadSand.world.getCurLoc().getObject(World.player.x, World.player.y, World.player.stats.look).id != 0
				|| VerifyPosition(World.player.stats.look))
			return;
		World.player.doAction(Stats.AP_WALK);
		move(World.player.stats.look);
		isInFront();
	}

	static void mouseMovement() {
		if ((Gdx.input.isButtonPressed(0)) && (MadSand.state == GameState.GAME) && (!MadSand.stepping)
				&& (!MadSand.contextopened)) {
			if (MadSand.wmx > World.player.x)
				walk(Direction.RIGHT);
			else if (MadSand.wmx < World.player.x)
				walk(Direction.LEFT);
			else if (MadSand.wmy > World.player.y)
				walk(Direction.UP);
			else if (MadSand.wmy < World.player.y)
				walk(Direction.DOWN);
		}
	}

	public static void turn(Direction dir) {
		World.player.stats.look = dir;
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

	public static void tileDmg() {
		int tid = MadSand.world.getTileId(World.player.x, World.player.y);
		int dmg = TileProp.damage.getOrDefault(tid, 0);
		if (dmg > 0) {
			MadSand.print("You took " + dmg + " damage from " + (TileProp.name.get(tid)));
			World.player.damage(dmg);
		}
	}

	public static void move(Direction dir) {
		if ((!World.player.isCollision(dir, 0)) && (MadSand.dialogflag)) {
			if ((dir == Direction.UP) && (!VerifyPosition(dir))) {
				World.player.y += 1;
				World.player.globalPos.y += MadSand.TILESIZE;
			}
			if ((dir == Direction.DOWN) && (!VerifyPosition(dir))) {
				World.player.y -= 1;
				World.player.globalPos.y -= MadSand.TILESIZE;
			}
			if ((dir == Direction.LEFT) && (!VerifyPosition(dir))) {
				World.player.x -= 1;
				World.player.globalPos.x -= MadSand.TILESIZE;
			}
			if ((dir == Direction.RIGHT) && (!VerifyPosition(dir))) {
				World.player.x += 1;
				World.player.globalPos.x += MadSand.TILESIZE;
			}
			if (World.player.x == World.MAPSIZE - 1 || World.player.y == World.MAPSIZE - 1
					|| World.player.x == World.BORDER || World.player.y == World.BORDER) {
				MadSand.print("Press [GRAY]N[WHITE] to move to the next sector.");
			}
			MadSand.stepping = true;
		}
	}

	public static boolean VerifyPosition(Direction dir) {
		boolean ret = false;
		if (World.player.x >= World.MAPSIZE - 1 && (dir == Direction.RIGHT)) {
			ret = true;
		}
		if (World.player.y >= World.MAPSIZE - 1 && (dir == Direction.UP)) {
			ret = true;
		}
		if (World.player.x <= 1 && (dir == Direction.LEFT)) {
			ret = true;
		}
		if (World.player.y <= 1 && (dir == Direction.DOWN)) {
			ret = true;
		}
		return ret;
	}

	public static Direction gotodir;

	public static void gotoSector(Direction dir) {
		gotodir = dir;
		MadSand.state = GameState.GOT;
		MadSand.tempwx = MadSand.world.curxwpos;
		MadSand.tempwy = MadSand.world.curywpos;
		MadSand.tonext = true;
		MadSand.encounter = false;

		if ((Utils.gotodir == Direction.LEFT) && (MadSand.world.curxwpos > 0)) {
			MadSand.world.curxwpos -= 1;
			World.player.x = World.MAPSIZE - 2;
			World.player.updCoords();
		}
		if ((Utils.gotodir == Direction.RIGHT) && (MadSand.world.curxwpos < 9)) {
			MadSand.world.curxwpos += 1;
			World.player.x = 0;
			World.player.updCoords();
		}
		if ((Utils.gotodir == Direction.DOWN) && (MadSand.world.curywpos > 0)) {
			MadSand.world.curywpos -= 1;
			World.player.y = World.MAPSIZE - 2;
			World.player.updCoords();
		}
		if ((Utils.gotodir == Direction.UP) && (MadSand.world.curywpos < 9)) {
			MadSand.world.curywpos += 1;
			World.player.y = 0;
			World.player.updCoords();
		}
		MadSand.print("Going to (" + MadSand.world.curxwpos + ", " + MadSand.world.curywpos + ")");
		if (GameSaver.verifyNextSector(MadSand.world.curxwpos, MadSand.world.curywpos)) {
			GameSaver.loadSector();
		} else {
			MadSand.state = GameState.WORLDGEN;
			if (MadSand.tonext) {
				if (Utils.rand(0, MadSand.ENCOUNTERCHANCE) == MadSand.ENCOUNTERCHANCE) {
					try {
						MadSand.world.curxwpos = MadSand.tempwx;
						MadSand.world.curywpos = MadSand.tempwy;
						MadSand.encounter = true;
						MadSand.print("You came to a strange place...");
						BuildScript.execute((GameSaver.getExternal("MadSand_Saves/scripts/encounter.msl")));
					} catch (Exception e) {
						e.printStackTrace();
						Utils.out("Error on random encounter start: " + e.getMessage());
					}
				} else
					MadSand.world.Generate();
			} else {
				MadSand.world.Generate();
			}
			MadSand.tonext = false;
			MadSand.state = GameState.GAME;
		}
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
			Gui.mouselabel[2].setText("Object: " + " ("
					+ ObjectProp.name.get(MadSand.world.getCurLoc().getObject(MadSand.wmx, MadSand.wmy).id) + ")");
			Gui.mouselabel[3].setText("Creature: " + " ()");
			Gui.mouselabel[4].setText("Global ticks: " + MadSand.world.globalTick + "\nWorld time: "
					+ MadSand.world.worldtime + "\nPlayer position: (" + World.player.x + ", " + World.player.y);
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
		if ((Gdx.input.isKeyJustPressed(Keys.ENTER)) && (Gui.overlay.getKeyboardFocus() == Gui.inputField) && tester) {
			String cmd = Gui.inputField.getText().trim();
			try {
				BuildScript.execute(cmd);
				Gui.inputField.setVisible(!Gui.inputField.isVisible());
			} catch (Exception e) {
				MadSand.print("Syntax error");
			}
			Gui.inputField.setText("");
			Gui.overlay.unfocus(Gui.inputField);
		}
	}
}
