package hitonoriol.madsand.input;

import static hitonoriol.madsand.MadSand.player;
import static hitonoriol.madsand.MadSand.world;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.text.WordUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.MadSand.Screens;
import hitonoriol.madsand.containers.HashMapFactory;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.gui.dialogs.InputDialog;
import hitonoriol.madsand.gui.dialogs.SelectDialog;
import hitonoriol.madsand.minigames.blackjack.BlackJackUI;
import hitonoriol.madsand.minigames.farkle.FarkleUI;
import hitonoriol.madsand.minigames.videopoker.VideoPokerUI;
import hitonoriol.madsand.pathfinding.Node;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.screens.WorldRenderer;
import hitonoriol.madsand.util.TimeUtils;
import hitonoriol.madsand.world.World;

public class Keyboard {
	private static KeyBindManager keyBinds = new KeyBindManager();
	private static int ignoreInput = 0;
	private static Set<Integer> heldKeys = new HashSet<>();

	public static void initDefaultKeyBinds() {
		/* Movement key polling */
		final Map<Integer, Direction> keyDirs = HashMapFactory.create(map -> map
				.put(Keys.W, Direction.UP)
				.put(Keys.A, Direction.LEFT)
				.put(Keys.S, Direction.DOWN)
				.put(Keys.D, Direction.RIGHT));
		Stream.of(Keys.W, Keys.A, Keys.S, Keys.D)
				.forEach(key -> keyBinds.poll(key, () -> {
					player().walk(keyDirs.get(key));
					if (Gdx.input.isKeyJustPressed(key))
						player().attackHostile();
				}));

		/* Turning keys */
		Stream.of(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)
				.forEach(dir -> keyBinds.bind(
						/* Convert case: Direction.UP -> Keys.Up */
						Keys.valueOf(WordUtils.capitalizeFully(dir.toString())),
						() -> player().meleeAttack(dir)));

		/* Action keys */
		keyBinds
				.bind(Keys.ENTER, () -> player().interact())
				.bind(Keys.U, () -> player().useItem())
				.bind(Keys.F, () -> player().freeHands())
				.bind(Keys.SPACE, () -> player().rest())
				.bind(Keys.N, () -> world().travel());

		/* Function keys */
		keyBinds
				.bind(Keys.ESCAPE, true, () -> {
					if (Gui.isDialogActive())
						Gui.overlay.closeAllDialogs();
					else
						MadSand.switchScreen(Screens.MainMenu);
				})
				.bind(Keys.NUMPAD_5, () -> {
					WorldRenderer renderer = MadSand.getRenderer();
					if (renderer.getCamZoom() != 1)
						renderer.setZoom(1);
					else
						renderer.setZoom(WorldRenderer.DEFAULT_ZOOM);
				})
				.bind(Keys.G, () -> GameSaver.save());

		/* Debug keys */
		if (Globals.debugMode) {
			keyBinds.bind(Keys.Z, () -> Globals.debugMode = !Globals.debugMode)
					.bind(Keys.BACKSPACE, () -> {
						new SelectDialog("Minigame test")
								.addOption("BlackJack", () -> new BlackJackUI().show())
								.addOption("Videopoker", () -> new VideoPokerUI().show())
								.addOption("Farkle", () -> new FarkleUI().show())
								.show();
					})
					.bind(Keys.F5, () -> {
						World world = world();
						world.skipToNextHour();
						world.updateLight();
						Gui.refreshOverlay();
					})
					.bind(Keys.HOME,
							() -> new InputDialog("Input test", "Type somethin:", text -> Gui.drawOkDialog(text))
									.show())
					.bind(key -> pollDebugKeys(key));
		}
	}

	public static void pollGameKeys() {
		if (!heldKeys.isEmpty() && !inputIgnored())
			keyBinds.pollKeys();
	}

	public static void pollGlobalHotkeys() {
		if (Gdx.input.isKeyJustPressed(Keys.F12))
			Resources.takeScreenshot();
	}

	public static void initListener() {
		Gui.overlay.addListener(new InputListener() {
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
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
		return ignoreInput > 0 || Mouse.hasClickAction();
	}

	public static int ignoreInput() {
		return ++ignoreInput;
	}

	public static void resumeInput() {
		if (--ignoreInput < 0)
			ignoreInput = 0;
	}

	public static int resumeInput(int stopLevel) {
		ignoreInput -= stopLevel;

		if (ignoreInput < 0)
			ignoreInput = 0;

		return ignoreInput;
	}

	private static void pollDebugKeys(int key) {
		if (!Globals.debugMode)
			return;

		if (key == Keys.GRAVE)
			Gui.overlay.toggleGameConsole();

		if (key == Keys.Y)
			Mouse.setClickAction((x, y) -> {
				Node dest = Mouse.getPathToCursor().getDestination();
				MadSand.player().teleport(dest.x, dest.y);
			}, MadSand.player().getFov());

		if (key == Keys.NUMPAD_3)
			MadSand.getRenderer().changeZoom(0.1f);

		if (key == Keys.NUMPAD_1)
			MadSand.getRenderer().changeZoom(-0.1f);

		if (isKeyPressed(Keys.CONTROL_LEFT)) {
			/* Unlock everything */
			if (key == Keys.U) {
				ItemProp.craftReq.keySet()
						.forEach(recipe -> player().unlockCraftRecipe(recipe));
				ItemProp.buildReq.keySet()
						.forEach(recipe -> player().unlockBuildRecipe(recipe));
				TimeUtils.scheduleTask(() -> Gui.overlay.closeAllDialogs(), 2f);
			}

			else if (key == Keys.I) {
				AbstractNpc npc = Mouse.pointingAt().getCell().getNpc();
				if (npc.isEmpty())
					return;

				Gui.drawOkDialog("NPC Info", String.format("%s\n%s\n%s", npc, npc.stats().baseStats, npc.inventory));
			}

			else if (key == Keys.DOWN)
				MadSand.world().descend();

			else if (key == Keys.UP)
				MadSand.world().ascend();

			else if (key == Keys.R) {
				MadSand.world().close();
				MadSand.world().generate();
				MadSand.enterWorld();
			}

			else if (key == Keys.W)
				MadSand.player().skipTime();
		}
	}

	public static KeyBindManager getKeyBindManager() {
		return keyBinds;
	}

}
