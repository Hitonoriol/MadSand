package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

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
	public static boolean invent = false;
	public static float pspeed = 33.0F;
	static SpriteBatch batch;
	static boolean admin = true;

	public static Random random = new Random();
	static int selected;

	public static void init() {
		MadSand.gameVrf = getSHA1(new File(MadSand.RESFILE));
		out("Resfile hash: " + MadSand.gameVrf);
		Resource.init();
		batch = new SpriteBatch();
	}

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

	static String getKey(Document doc, String list, String id, String element, String def) {
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
			return def;
		} catch (Exception e) {
			return def;
		}
	}

	static String getKey(Document doc, String list, String id, String element) {
		return getKey(doc, list, id, element, "-1");
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
			tmp = getAttrValues(Resource.gendoc, "biome", str(biome), gname, str(j));
			if (tmp.equals("-1"))
				break;
			group.add(tmp);
			++j;
		}
		return group;
	}

	static HashMap<Integer, Vector<Integer>> getAitem(int id, String field) {
		int i = 0;
		String hand = "";
		HashMap<Integer, Vector<Integer>> ret = new HashMap<Integer, Vector<Integer>>();
		Vector<Integer> block;
		StringTokenizer tok;
		int hid;
		while (hand != "-1") {
			hand = getAttrValues(Resource.resdoc, field, str(id), "altitem", str(i));
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

	public static Tuple<Integer, Double> makeTuple(int key, double val) {
		return new Tuple<Integer, Double>(key, val);
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
			invBtnSetVisible(false);
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
			invBtnSetVisible(true);
			Gdx.input.setInputProcessor(Gui.overlay);
			MadSand.state = GameState.INVENTORY;
			invent = true;
		}
	}

	public static void invKeyCheck() {
		if (Gdx.input.isKeyJustPressed(Keys.E)) {
			toggleInventory();
		}
	}

	public static void invBtnSetVisible(boolean visible) {
		Gui.craftButton.setVisible(visible);
		Gui.exitButton.setVisible(visible);
	}

	public static void gameKeyCheck() {
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
		if (Gdx.input.isKeyJustPressed(Keys.UP) && (!World.player.isStepping())) {
			World.player.turn(Direction.UP);
		}
		if (Gdx.input.isKeyJustPressed(Keys.DOWN) && (!World.player.isStepping())) {
			World.player.turn(Direction.DOWN);
		}
		if (Gdx.input.isKeyJustPressed(Keys.LEFT) && (!World.player.isStepping())) {
			World.player.turn(Direction.LEFT);
		}
		if (Gdx.input.isKeyJustPressed(Keys.RIGHT) && (!World.player.isStepping())) {
			World.player.turn(Direction.RIGHT);
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
			MadSand.world.curlayer = 0;

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
		if ((Gdx.input.isKeyPressed(Keys.A))) {
			World.player.walk(Direction.LEFT);
		}
		if ((Gdx.input.isKeyPressed(Keys.D))) {
			World.player.walk(Direction.RIGHT);
		}
		if ((Gdx.input.isKeyPressed(Keys.W))) {
			World.player.walk(Direction.UP);
		}
		if ((Gdx.input.isKeyPressed(Keys.S))) {
			World.player.walk(Direction.DOWN);
		}
		if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
			// rest
		}
	}

	static void mouseMovement() {
		if ((Gdx.input.isButtonPressed(0)) && (MadSand.state == GameState.GAME) && (!World.player.isStepping())
				&& (!MadSand.contextopened)) {
			if (MadSand.wmx > World.player.x)
				World.player.walk(Direction.RIGHT);
			else if (MadSand.wmx < World.player.x)
				World.player.walk(Direction.LEFT);
			else if (MadSand.wmy > World.player.y)
				World.player.walk(Direction.UP);
			else if (MadSand.wmy < World.player.y)
				World.player.walk(Direction.DOWN);
		}
	}

	public static int rand(int min, int max) {
		return Utils.random.nextInt((max - min) + 1) + min;
	}

	public static Direction gotodir;

	public static void gotoSector(Direction dir) {
		GameSaver.saveWorld();
		gotodir = dir;
		MadSand.state = GameState.GOT;
		MadSand.tempwx = MadSand.world.curxwpos;
		MadSand.tempwy = MadSand.world.curywpos;
		MadSand.tonext = true;
		MadSand.encounter = false;

		if ((Utils.gotodir == Direction.LEFT)) {
			MadSand.world.curxwpos -= 1;
			World.player.x = World.MAPSIZE - 2;
			World.player.updCoords();
		}
		if ((Utils.gotodir == Direction.RIGHT)) {
			MadSand.world.curxwpos += 1;
			World.player.x = 0;
			World.player.updCoords();
		}
		if ((Utils.gotodir == Direction.DOWN)) {
			MadSand.world.curywpos -= 1;
			World.player.y = World.MAPSIZE - 2;
			World.player.updCoords();
		}
		if ((Utils.gotodir == Direction.UP)) {
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
		Gui.mousemenu.addAction(Actions.moveTo(MadSand.mx + 60, MadSand.my - 70, 0.1F));
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

	public static void out(String arg) {
		if (tester) {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
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
