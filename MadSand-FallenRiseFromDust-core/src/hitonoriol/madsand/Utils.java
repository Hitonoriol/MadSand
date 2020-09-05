package hitonoriol.madsand;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.enums.GameState;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.world.World;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Random;
import java.util.StringTokenizer;

public class Utils {
	public static boolean debugMode = true;
	public static float pspeed = 33.0F;
	static SpriteBatch batch;
	static boolean admin = true;

	static long seed = new Random().nextLong();
	public static Random random = new Random(seed);

	static int selected;

	public static void init() {
		try {
			Resources.init();
			batch = new SpriteBatch();
		} catch (Exception e) {
			die("Exception on init: " + ExceptionUtils.getStackTrace(e));
		}
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

	public static void pollStatWindowKey() {
		if (Gdx.input.isKeyJustPressed(Keys.Q))
			Gui.showStatsWindow();
	}

	public static void gameKeyCheck() {
		if (Gdx.input.isKeyJustPressed(Keys.GRAVE)) {
			Gui.inputField.setVisible(!Gui.inputField.isVisible());
			Gui.overlay.setKeyboardFocus(Gui.inputField);
		}
		if (Gdx.input.isButtonPressed(Buttons.MIDDLE))
			World.player.lookAtMouse(Mouse.wx, Mouse.wy);
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
			World.player.attack(Direction.UP);
		}
		if (Gdx.input.isKeyJustPressed(Keys.DOWN) && (!World.player.isStepping())) {
			World.player.attack(Direction.DOWN);
		}
		if (Gdx.input.isKeyJustPressed(Keys.LEFT) && (!World.player.isStepping())) {
			World.player.attack(Direction.LEFT);
		}
		if (Gdx.input.isKeyJustPressed(Keys.RIGHT) && (!World.player.isStepping())) {
			World.player.attack(Direction.RIGHT);
		}
		if (Gdx.input.isKeyJustPressed(Keys.N) && MadSand.world.curlayer == World.LAYER_OVERWORLD) {
			if (World.player.x == World.MAPSIZE - 1 && World.player.stats.look == Direction.RIGHT)
				gotoSector(World.player.stats.look);
			if (World.player.y == World.MAPSIZE - 1 && World.player.stats.look == Direction.UP)
				gotoSector(World.player.stats.look);
			if (World.player.x < World.BORDER && World.player.stats.look == Direction.LEFT)
				gotoSector(World.player.stats.look);
			if (World.player.y < World.BORDER && World.player.stats.look == Direction.DOWN)
				gotoSector(World.player.stats.look);
			MadSand.world.curlayer = 0;

		}

		if ((Gdx.input.isKeyJustPressed(Keys.Y)) && (debugMode)) {
			World.player.teleport(Mouse.wx, Mouse.wy);
		}
		if ((Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) && (Gdx.input.isKeyJustPressed(Keys.R)) && (debugMode)) {
			MadSand.world.generate();
		}
		if ((Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) && (Gdx.input.isKeyJustPressed(Keys.DOWN)) && (debugMode)) {
			MadSand.world.descend();
		}
		if ((Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) && (Gdx.input.isKeyJustPressed(Keys.UP)) && (debugMode)) {
			MadSand.world.ascend();
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
		if ((Gdx.input.isKeyJustPressed(Keys.G))) {
			GameSaver.saveWorld();
		}
		if ((Gdx.input.isKeyJustPressed(Keys.L))) {
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

	public static int rand(int min, int max) {
		return Utils.random.nextInt((max - min) + 1) + min;
	}

	public static void gotoSector(Direction dir) {
		GameSaver.saveWorld();
		MadSand.state = GameState.GOT;
		MadSand.world.switchLocation(dir);
		MadSand.state = GameState.GAME;
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

	public static HashMap<String, Integer> toValMap(HashMap<String, String> map) {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		for (Entry<String, String> str : map.entrySet()) {
			ret.put(str.getKey(), val(str.getValue()));
		}
		return ret;
	}

	public static ArrayList<Integer> parseList(String str) {
		StringTokenizer list = new StringTokenizer(str, ",");
		ArrayList<Integer> ret = new ArrayList<Integer>();
		while (list.hasMoreTokens()) {
			ret.add(val(list.nextToken()));
		}
		return ret;
	}

	public static ArrayList<String> parseList(String str, String delim) {
		StringTokenizer list = new StringTokenizer(str, delim);
		ArrayList<String> ret = new ArrayList<String>();
		while (list.hasMoreTokens()) {
			ret.add(list.nextToken().trim());
		}
		return ret;
	}

	public static int randElement(ArrayList<Integer> list) {
		return list.get(rand(0, list.size() - 1));
	}

	public static double round(double curWeight) {
		return (Math.round(curWeight * 100) / 100.00);
	}

	public static int randPercent() {
		return rand(1, 100);
	}

	public static boolean percentRoll(int percent) {
		return ((randPercent() - 1) < percent);
	}

	public static int val(boolean bool) {
		return bool ? 1 : 0;
	}

	public static boolean bool(int val) {
		return (val == 1);
	}
}
