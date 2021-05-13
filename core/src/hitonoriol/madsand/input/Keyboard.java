package hitonoriol.madsand.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.world.World;

public class Keyboard {

	private static KeyBindManager keyBinds = new KeyBindManager();
	private static boolean ignoreInput = false;

	public static void initDefaultKeyBinds() {
		Player player = MadSand.player();
		World world = MadSand.world();

		/* Turning keys */
		keyBinds.bind(Keys.UP, () -> player.meleeAttack(Direction.UP))
				.bind(Keys.DOWN, () -> player.meleeAttack(Direction.DOWN))
				.bind(Keys.LEFT, () -> player.meleeAttack(Direction.LEFT))
				.bind(Keys.RIGHT, () -> player.meleeAttack(Direction.RIGHT));

		/* Action keys */
		keyBinds.bind(Keys.ENTER, () -> player.interact())
				.bind(Keys.U, () -> player.useItem())
				.bind(Keys.F, () -> player.freeHands())
				.bind(Keys.SPACE, () -> player.rest())
				.bind(Keys.N, () -> world.travel());

		/* Function keys */
		keyBinds.bind(Keys.ESCAPE, () -> MadSand.switchScreen(MadSand.mainMenu))
				.bind(Keys.G, () -> GameSaver.saveWorld());
	}

	public static void pollGameKeys() {
		if (!ignoreInput)
			pollMovementKeys();

		pollDebugKeys();
	}

	public static void pollGlobalHotkeys() {
		if (Gdx.input.isKeyJustPressed(Keys.F12))
			Resources.takeScreenshot();
	}

	public static void initListener() {
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

				keyBinds.runBoundAction(keycode);
				return true;
			}
		});
	}

	public static boolean inputIgnored() {
		return ignoreInput || Mouse.hasClickAction();
	}

	public static void stopInput() {
		ignoreInput = true;
	}

	public static void resumeInput() {
		ignoreInput = false;
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
			Globals.debugMode = !Globals.debugMode;

		if (!Globals.debugMode)
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
			Mouse.setClickAction((x, y) -> World.player.move(Mouse.getPathToCursor()), World.player.fov);

		if (Gdx.input.isKeyPressed(Keys.NUMPAD_3)) {
			MadSand.getRenderer().changeZoom(0.01f);
		}

		if (Gdx.input.isKeyPressed(Keys.NUMPAD_1)) {
			MadSand.getRenderer().changeZoom(-0.01f);
		}

		if (Gdx.input.isKeyJustPressed(Keys.F5))
			MadSand.world.timeTick(150);
	}

	public static KeyBindManager getKeyBindManager() {
		return keyBinds;
	}

}
