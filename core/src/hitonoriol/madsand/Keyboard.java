package hitonoriol.madsand;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import hitonoriol.madsand.enums.Direction;
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
		pollFunctionKeys();
	}

	public static void pollGlobalHotkeys() {
		if (Gdx.input.isKeyJustPressed(Keys.F12))
			Resources.takeScreenshot();
	}

	public static void initKeyListener() {
		Gui.overlay.addListener(new InputListener() {
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if (keycode == Keys.E) {
					if ((!Gui.inventoryActive && !Gui.isGameUnfocused())
							|| ((Gui.inventoryActive && Gui.isGameUnfocused())))
						Gui.toggleInventory();
					return true;
				}

				if (Gui.isGameUnfocused())
					return true;

				if (Gui.overlay.bottomMenu.isKeyBoundToButton(keycode))
					Gui.overlay.bottomMenu.toggleButton(keycode);

				return true;
			}
		});
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

	private static void pollFunctionKeys() {
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE))
			MadSand.switchScreen(MadSand.mainMenu);

		if (Gdx.input.isKeyJustPressed(Keys.G))
			GameSaver.saveWorld();
	}

	private static void pollTurnKeys() {
		if (World.player.isStepping())
			return;

		if (Gdx.input.isKeyJustPressed(Keys.UP))
			World.player.meleeAttack(Direction.UP);

		else if (Gdx.input.isKeyJustPressed(Keys.DOWN))
			World.player.meleeAttack(Direction.DOWN);

		else if (Gdx.input.isKeyJustPressed(Keys.LEFT))
			World.player.meleeAttack(Direction.LEFT);

		else if (Gdx.input.isKeyJustPressed(Keys.RIGHT))
			World.player.meleeAttack(Direction.RIGHT);
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
			Gui.gameUnfocus();
			TextField console = Gui.overlay.getConsoleField();
			console.setVisible(!console.isVisible());
			Gui.overlay.setKeyboardFocus(console);
		}

		if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
			if (Gdx.input.isKeyJustPressed(Keys.DOWN))
				MadSand.world.descend();
			else if (Gdx.input.isKeyJustPressed(Keys.UP))
				MadSand.world.ascend();

			else if (Gdx.input.isKeyJustPressed(Keys.R)) {
				MadSand.world.generate();
				MadSand.worldEntered();
			}

			else if (Gdx.input.isKeyJustPressed(Keys.W))
				World.player.skipTime();
		}

		if (Gdx.input.isKeyJustPressed(Keys.Y))
			World.player.teleport(Mouse.wx, Mouse.wy);

		if (Gdx.input.isKeyPressed(Keys.NUMPAD_3)) {
			MadSand.gameWorld.changeZoom(0.01f);
		}

		if (Gdx.input.isKeyPressed(Keys.NUMPAD_1)) {
			MadSand.gameWorld.changeZoom(-0.01f);
		}

		if (Gdx.input.isKeyJustPressed(Keys.F5))
			MadSand.world.timeTick(150);

	}

}
