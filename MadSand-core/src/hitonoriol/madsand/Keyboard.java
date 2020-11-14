package hitonoriol.madsand;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.enums.GameState;
import hitonoriol.madsand.gui.dialogs.BestiaryDialog;
import hitonoriol.madsand.world.Location;
import hitonoriol.madsand.world.World;

public class Keyboard {

	private static boolean ignoreInput = false;

	public static void pollGameKeys() {
		if (!ignoreInput) {
			pollMovementKeys();
			pollTurnKeys();
			pollActionKeys();
		}

		pollDebugKeys();
		pollInventoryKey();
		pollFunctionKeys();
	}
	
	public static boolean inputIgnored() {
		return ignoreInput;
	}
	
	public static void stopInput() {
		ignoreInput = true;
	}

	public static void resumeInput() {
		ignoreInput = false;
	}
	
	public static void pollScreenshotKey() {
		if (Gdx.input.isKeyJustPressed(Keys.F12))
			Resources.takeScreenshot();
	}

	public static void pollInventoryKey() {
		if (Gdx.input.isKeyJustPressed(Keys.E))
			Gui.toggleInventory();
	}

	private static void pollFunctionKeys() {
		if (Gdx.input.isKeyJustPressed(Keys.F11)) {
			Boolean fullScreen = Boolean.valueOf(Gdx.graphics.isFullscreen());
			Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
			if (fullScreen.booleanValue()) {
				Gdx.graphics.setWindowedMode(1280, 720);
			} else
				Gdx.graphics.setFullscreenMode(currentMode);
		}

		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			Gui.mainMenu.showResumeTable();
			MadSand.xmid = MadSand.cameraX = World.player.globalPos.x;
			MadSand.ymid = MadSand.cameraY = World.player.globalPos.y;
			Gdx.input.setInputProcessor(Gui.mainMenu);
			MadSand.state = GameState.NMENU;
		}

		if ((Gdx.input.isKeyJustPressed(Keys.G)))
			GameSaver.saveWorld();

		if ((Gdx.input.isKeyJustPressed(Keys.L)))
			GameSaver.loadWorld(MadSand.WORLDNAME);
		
		if (Gdx.input.isKeyJustPressed(Keys.J))
			Gui.overlay.showJournal();

		if (Gdx.input.isKeyJustPressed(Keys.X))
			new BestiaryDialog(World.player).show();

		if (Gdx.input.isKeyJustPressed(Keys.B))
			Gui.overlay.showBuildMenu();

		if (Gdx.input.isKeyJustPressed(Keys.Q))
			Gui.overlay.toggleStatsWindow();
	}

	private static void pollTurnKeys() {
		if (World.player.isStepping())
			return;

		if (Gdx.input.isKeyJustPressed(Keys.UP))
			World.player.attack(Direction.UP);

		else if (Gdx.input.isKeyJustPressed(Keys.DOWN))
			World.player.attack(Direction.DOWN);

		else if (Gdx.input.isKeyJustPressed(Keys.LEFT))
			World.player.attack(Direction.LEFT);

		else if (Gdx.input.isKeyJustPressed(Keys.RIGHT))
			World.player.attack(Direction.RIGHT);
	}

	private static void pollActionKeys() {
		if (Gdx.input.isButtonPressed(Buttons.MIDDLE))
			World.player.lookAtMouse(Mouse.wx, Mouse.wy);

		if (Gdx.input.isKeyJustPressed(Keys.ENTER))
			World.player.interact();

		if (Gdx.input.isKeyJustPressed(Keys.U))
			World.player.useItem();

		if ((Gdx.input.isKeyJustPressed(Keys.F)) && (World.player.stats.hand().id != 0))
			World.player.freeHands();

		if (Gdx.input.isKeyJustPressed(Keys.SPACE))
			World.player.rest();

		if (Gdx.input.isKeyJustPressed(Keys.N) && MadSand.world.curLayer() == Location.LAYER_OVERWORLD)
			MadSand.world.travel();

	}

	private static void pollMovementKeys() {

		if (Gdx.input.isKeyPressed(Keys.A))
			World.player.walk(Direction.LEFT);

		else if (Gdx.input.isKeyPressed(Keys.D))
			World.player.walk(Direction.RIGHT);

		else if (Gdx.input.isKeyPressed(Keys.W))
			World.player.walk(Direction.UP);

		else if (Gdx.input.isKeyPressed(Keys.S))
			World.player.walk(Direction.DOWN);

		if (Keyboard.movementKeyJustPressed())
			World.player.attackHostile();

	}

	private static boolean movementKeyJustPressed() {
		return (Gdx.input.isKeyJustPressed(Keys.W) ||
				Gdx.input.isKeyJustPressed(Keys.A) ||
				Gdx.input.isKeyJustPressed(Keys.S) ||
				Gdx.input.isKeyJustPressed(Keys.D));
	}

	private static void pollDebugKeys() {

		if (Gdx.input.isKeyJustPressed(Keys.Z))
			Utils.debugMode = !Utils.debugMode;

		if (!Utils.debugMode)
			return;

		if (Gdx.input.isKeyJustPressed(Keys.GRAVE)) {
			TextField console = Gui.overlay.getConsoleField();
			console.setVisible(!console.isVisible());
			Gui.overlay.setKeyboardFocus(console);
		}

		if ((Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) && (Gdx.input.isKeyJustPressed(Keys.DOWN)) && (Utils.debugMode))
			MadSand.world.descend();

		if ((Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) && (Gdx.input.isKeyJustPressed(Keys.UP)) && (Utils.debugMode))
			MadSand.world.ascend();

		if (Gdx.input.isKeyJustPressed(Keys.Y))
			World.player.teleport(Mouse.wx, Mouse.wy);

		if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && Gdx.input.isKeyJustPressed(Keys.R)) {
			MadSand.world.generate();
			MadSand.worldEntered();
		}

		if (Gdx.input.isKeyPressed(Keys.NUMPAD_3)) {
			MadSand.ZOOM += 0.01f;
			MadSand.setViewport();
		}

		if (Gdx.input.isKeyPressed(Keys.NUMPAD_1)) {
			MadSand.ZOOM -= 0.01f;
			MadSand.setViewport();
		}

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

		if (Gdx.input.isKeyJustPressed(Keys.F5))
			MadSand.world.timeTick(150);

	}

}
