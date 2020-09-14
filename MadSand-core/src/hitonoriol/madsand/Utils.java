package hitonoriol.madsand;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.enums.GameState;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.world.World;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Random;
import java.util.StringTokenizer;

public class Utils {
	public static boolean debugMode = true;
	static SpriteBatch batch;

	public static Random random = new Random();

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
		Gui.overlay.craftMenuButton.setVisible(visible);
		Gui.overlay.exitToMenuButton.setVisible(visible);
	}

	static Npc dummy;

	public static void pollStatWindowKey() {
		if (Gdx.input.isKeyJustPressed(Keys.Q))
			Gui.overlay.toggleStatsWindow();
	}

	public static void gameKeyCheck() {
		if (Gdx.input.isKeyJustPressed(Keys.GRAVE)) {
			TextField console = Gui.overlay.getConsoleField();
			console.setVisible(!console.isVisible());
			Gui.overlay.setKeyboardFocus(console);
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
				goToSector(World.player.stats.look);
			if (World.player.y == World.MAPSIZE - 1 && World.player.stats.look == Direction.UP)
				goToSector(World.player.stats.look);
			if (World.player.x < World.BORDER && World.player.stats.look == Direction.LEFT)
				goToSector(World.player.stats.look);
			if (World.player.y < World.BORDER && World.player.stats.look == Direction.DOWN)
				goToSector(World.player.stats.look);
			MadSand.world.curlayer = 0;

		}

		if ((Gdx.input.isKeyJustPressed(Keys.Y)) && (debugMode)) {
			World.player.teleport(Mouse.wx, Mouse.wy);
		}
		if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && Gdx.input.isKeyJustPressed(Keys.T)) {
			Utils.out("Trade test");
			Npc npc = MadSand.world.getCurLoc().getNpc(World.player.lookingAt());
			Utils.out("Looking at npc: " + npc.id);
			npc.inventory.putItem(Globals.getInt(Globals.CURRENCY_FIELD), rand(100, 500));
			World.player.tradeWithNPC(World.player.stats.look);
		}
		if ((Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) && (Gdx.input.isKeyJustPressed(Keys.R)) && (debugMode)) {
			MadSand.world.generate();
		}
		if (Gdx.input.isKeyJustPressed(Keys.J)) {
			// Quest Journal
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
			Gui.mainMenu.resumeButton.setVisible(true);
			MadSand.xmid = MadSand.xmenu = World.player.globalPos.x;
			MadSand.ymid = MadSand.ymenu = World.player.globalPos.y;
			Gdx.input.setInputProcessor(Gui.mainMenu);
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
		return random.nextInt((max - min) + 1) + min;
	}

	public static int rand(int max) {
		return random.nextInt(max);
	}

	public static void goToSector(Direction dir) {
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

	public static void executeConsoleInput() {
		if (!debugMode)
			return;

		TextField console = Gui.overlay.getConsoleField();

		if (Gui.overlay.getKeyboardFocus() != console)
			return;

		if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
			String cmd = console.getText().trim();

			try {
				LuaUtils.execute(cmd);
				console.setVisible(!console.isVisible());
			} catch (Exception e) {
				MadSand.print("Couldn't execute user input");
				e.printStackTrace();
			}

			console.setText("");
			Gui.overlay.unfocus(console);
		}
	}

	public static ArrayList<Integer> parseList(String str) {
		StringTokenizer list = new StringTokenizer(str, ",");
		ArrayList<Integer> ret = new ArrayList<Integer>();

		while (list.hasMoreTokens())
			ret.add(val(list.nextToken()));

		return ret;
	}

	public static int randElement(ArrayList<Integer> list) {
		return list.get(random.nextInt(list.size()));
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
