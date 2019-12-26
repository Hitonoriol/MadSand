package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import ru.bernarder.fallenrisefromdust.entities.Npc;
import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.enums.GameState;
import ru.bernarder.fallenrisefromdust.map.Map;
import ru.bernarder.fallenrisefromdust.map.MapObject;
import ru.bernarder.fallenrisefromdust.properties.ItemProp;
import ru.bernarder.fallenrisefromdust.properties.ObjectProp;
import ru.bernarder.fallenrisefromdust.properties.TileProp;
import ru.bernarder.fallenrisefromdust.world.World;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

public class Utils {
	public static boolean debugMode = true;
	public static float pspeed = 33.0F;
	static SpriteBatch batch;
	static boolean admin = true;

	static long seed = new Random().nextLong();
	public static Random random = new Random(seed);

	static int selected;

	public static void init() {
		Resources.init();
		batch = new SpriteBatch();
	}

	static HashMap<Integer, Vector<Integer>> getAitem(int id, String field) {
		int i = 0;
		String hand = "";
		HashMap<Integer, Vector<Integer>> ret = new HashMap<Integer, Vector<Integer>>();
		Vector<Integer> block;
		StringTokenizer tok;
		int hid;
		while (hand != "-1") {
			hand = XMLUtils.getAttrValues(Resources.resdoc, field, str(id), "altitem", str(i));
			if (hand == "-1")
				break;
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

	public static String str(int val) {
		return Integer.toString(val);
	}

	public static int val(String str) {
		return Integer.parseInt(str);
	}

	public static String getItem(int id) {
		return ItemProp.name.get(id);
	}

	public static void toggleInventory() {
		if (Gui.inventoryActive)
			World.player.hideInventory();
		else
			World.player.showInventory();
	}

	public static void invKeyCheck() {
		if (Gdx.input.isKeyJustPressed(Keys.E)) {
			toggleInventory();
		}
	}

	public static void invBtnSetVisible(boolean visible) {
		Gui.craftBtn.setVisible(visible);
		Gui.exitToMenuBtn.setVisible(visible);
	}

	static Npc dummy;

	public static void gameKeyCheck() {
		if (Gdx.input.isKeyJustPressed(Keys.GRAVE)) {
			Gui.inputField.setVisible(!Gui.inputField.isVisible());
			Gui.overlay.setKeyboardFocus(Gui.inputField);
		}
		if (Gdx.input.isButtonPressed(Buttons.MIDDLE))
			World.player.lookAtMouse(MadSand.wmx, MadSand.wmy);
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
		if (Gdx.input.isKeyJustPressed(Keys.N) && MadSand.world.curlayer == World.LAYER_OVERWORLD) {
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

		if ((Gdx.input.isKeyJustPressed(Keys.Y)) && (debugMode)) {
			World.player.teleport(MadSand.wmx, MadSand.wmy);
		}
		if ((Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) && (Gdx.input.isKeyJustPressed(Keys.R)) && (debugMode)) {
			MadSand.world.Generate();
		}
		if ((Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) && (Gdx.input.isKeyJustPressed(Keys.DOWN)) && (debugMode)) {
			MadSand.world.curlayer = 1;
		}
		if ((Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) && (Gdx.input.isKeyJustPressed(Keys.UP)) && (debugMode)) {
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
			Gui.resumeBtn.setVisible(true);
			MadSand.xmid = MadSand.xmenu = World.player.globalPos.x;
			MadSand.ymid = MadSand.ymenu = World.player.globalPos.y;
			Gdx.input.setInputProcessor(Gui.menu);
			MadSand.state = GameState.NMENU;
		}
		if ((Gdx.input.isKeyJustPressed(Keys.G)) && (debugMode)) {
			GameSaver.saveWorld();
		}
		if ((Gdx.input.isKeyJustPressed(Keys.L)) && (debugMode)) {
			GameSaver.loadWorld(MadSand.WORLDNAME);
		}
		if ((Gdx.input.isKeyJustPressed(Keys.H)) && (debugMode)) {
			World.player.damage(10);
		}
		if ((Gdx.input.isKeyJustPressed(Keys.F)) && (World.player.stats.hand.id != 0)) {
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
		if (Gdx.input.isKeyJustPressed(Keys.Z))
			debugMode = !debugMode;
		if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
			World.player.rest();
		}
	}

	static void mouseMovement() {
		if ((Gdx.input.isButtonPressed(Buttons.LEFT)) && (MadSand.state == GameState.GAME) && (!World.player.isStepping())
				&& (!Gui.contextMenuActive)) {
			World.player.lookAtMouse(MadSand.wmx, MadSand.wmy);
			World.player.walk(World.player.stats.look);
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
			GameSaver.loadLocation();
		} else {
			MadSand.state = GameState.WORLDGEN;
			/*
			 * if (Utils.rand(0, MadSand.ENCOUNTERCHANCE) == MadSand.ENCOUNTERCHANCE) TODO
			 * Begin random encounter
			 */
			MadSand.world.Generate();
			MadSand.state = GameState.GAME;
		}
	}

	public static void updMouseCoords() {
		MadSand.mx = Gdx.input.getX();
		MadSand.my = Gdx.graphics.getHeight() - Gdx.input.getY();

		MadSand.wmx = (int) Math.floor(MadSand.mouseinworld.x / MadSand.TILESIZE);
		MadSand.wmy = (int) Math.floor(MadSand.mouseinworld.y / MadSand.TILESIZE);

		Map loc = MadSand.world.getCurLoc();
		Npc npc = loc.getNpc(MadSand.wmx, MadSand.wmy);
		MapObject object = loc.getObject(MadSand.wmx, MadSand.wmy);
		String info = "";

		Gui.mousemenu.addAction(Actions.moveTo(MadSand.mx + 65, MadSand.my - 70, 0.1F));

		info += ("Looking at (" + MadSand.wmx + ", " + MadSand.wmy + ")") + Gui.LINEBREAK;
		info += ("Tile: " + TileProp.name.get(loc.getTile(MadSand.wmx, MadSand.wmy).id)) + Gui.LINEBREAK;

		if (object != Map.nullObject)
			info += ("Object: " + ObjectProp.name.get(object.id)) + Gui.LINEBREAK;
		if (npc != Map.nullNpc)
			info += ("Creature: " + " " + npc.stats.name) + Gui.LINEBREAK;

		if (debugMode)
			info += ("Global ticks: " + MadSand.world.globalTick + "\nWorld time: " + MadSand.world.worldtime
					+ "\nPlayer position: (" + World.player.x + ", " + World.player.y + ")");

		Gui.mouselabel.setText(info);
	}

	public static void out(String arg) {
		if (debugMode) {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			System.out.print("[" + sdf.format(cal.getTime()) + "] " + arg + "\n");
		}
	}

	public static void die(String... msg) {
		out("Seems like some fatal error occured. Check " + MadSand.ERRFILE + " for details.");
		if (msg.length > 0) {
			for (String m : msg) {
				out(m);
			}
		}
		System.exit(-1);
	}

	public static void checkConsoleFocus() {
		if (!debugMode)
			return;
		if ((Gdx.input.isKeyJustPressed(Keys.ENTER)) && (Gui.overlay.getKeyboardFocus() == Gui.inputField)) {
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
