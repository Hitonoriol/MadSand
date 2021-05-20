package hitonoriol.madsand.input;

import java.util.HashSet;
import java.util.Set;

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
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.World;

public class Keyboard {

	private static KeyBindManager keyBinds = new KeyBindManager();
	private static boolean ignoreInput = false;
	private static Set<Integer> heldKeys = new HashSet<>();

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

		/* Debug keys */
		keyBinds.bind(Keys.Z, () -> Globals.debugMode = !Globals.debugMode)
				.bind(Keys.BACKSPACE, () -> Utils.out("%d", 1337 / 0))
				.bind(key -> pollDebugKeys(key));
	}

	public static void pollGameKeys() {
		if (!ignoreInput)
			pollMovementKeys();
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
				heldKeys.remove(keycode);
				return true;
			}

			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				heldKeys.add(keycode);
				return super.keyDown(event, keycode);
			}
		});
	}

	public static boolean isKeyPressed(int key) {
		return heldKeys.contains(key);
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

	private static void pollDebugKeys(int key) {
		if (!Globals.debugMode)
			return;

		if (key == Keys.GRAVE) {
			Gui.gameUnfocus();
			TextField console = Gui.overlay.getConsoleField();
			console.setVisible(!console.isVisible());
			Gui.overlay.setKeyboardFocus(console);
		}

		if (key == Keys.Y)
			Mouse.setClickAction((x, y) -> MadSand.player().run(Mouse.getPathToCursor()), MadSand.player().fov);

		if (key == Keys.NUMPAD_3)
			MadSand.getRenderer().changeZoom(0.01f);

		if (key == Keys.NUMPAD_1) {
			MadSand.getRenderer().changeZoom(-0.01f);
		}

		if (key == Keys.F5)
			MadSand.world.timeTick(150);

		if (isKeyPressed(Keys.CONTROL_LEFT)) {
			if (key == Keys.DOWN)
				MadSand.world.descend();

			else if (key == Keys.UP)
				MadSand.world.ascend();

			else if (key == Keys.R) {
				MadSand.world.generate();
				MadSand.worldEntered();
			}

			else if (key == Keys.W)
				World.player.skipTime();
		}
	}

	public static KeyBindManager getKeyBindManager() {
		return keyBinds;
	}

}
