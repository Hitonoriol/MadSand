package hitonoriol.madsand.input;

import static hitonoriol.madsand.MadSand.player;
import static hitonoriol.madsand.MadSand.world;

import java.util.Map;
import java.util.stream.Stream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.MadSand.Screens;
import hitonoriol.madsand.containers.HashMapFactory;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.dialogs.SelectDialog;
import hitonoriol.madsand.gui.dialogs.SliderDialog;
import hitonoriol.madsand.minigames.blackjack.BlackJackUI;
import hitonoriol.madsand.minigames.farkle.FarkleUI;
import hitonoriol.madsand.minigames.videopoker.VideoPokerUI;
import hitonoriol.madsand.pathfinding.Node;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.screens.WorldRenderer;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.World;

public class Keyboard {
	private static final GameInputListener inputListener = new GameInputListener();
	private static final KeyBindManager keyBinds = inputListener.getKeyBindManager();
	private static int ignoreInput = 0;

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
		Direction.forEachBase(dir -> {
			keyBinds.bind(() -> player().meleeAttack(dir), dir.toKey());
		});

		/* Action keys */
		keyBinds
				.bind(player()::interact, Keys.ENTER)
				.bind(player()::useItem, Keys.U)
				.bind(player()::freeHands, Keys.F)
				.bind(player()::rest, Keys.SPACE)
				.bind(world()::travel, Keys.N);

		/* Function keys */
		keyBinds
				.bind(() -> {
					if (Gui.isDialogActive())
						Gui.overlay.closeAllDialogs();
					else
						MadSand.switchScreen(Screens.MainMenu);
				}, true, Keys.ESCAPE)
				.bind(() -> {
					WorldRenderer renderer = MadSand.getRenderer();
					if (renderer.getCamZoom() != 1)
						renderer.setZoom(1);
					else
						renderer.setZoom(WorldRenderer.DEFAULT_ZOOM);
				}, Keys.NUMPAD_5)
				.bind(world()::save, Keys.G);

		/* Debug keys */
		if (Globals.debugMode) {
			bind(() -> Globals.debugMode = !Globals.debugMode, Keys.Z);
			bind(Gui.overlay::toggleGameConsole, Keys.GRAVE);

			/* Unlock all recipes */
			bind(() -> {
				ItemProp.craftReq.keySet()
						.forEach(player()::unlockCraftRecipe);
				ItemProp.buildReq.keySet()
						.forEach(player()::unlockBuildRecipe);
			}, Keys.CONTROL_LEFT, Keys.U);

			/* Populate inventory with random items */
			bind(() -> {
				new SliderDialog(1, 100)
						.setTitle("Give random items")
						.setSliderTitle("Item stacks to give:")
						.setOnUpdateText("stacks")
						.setConfirmAction(n -> {
							for (int i = 0; i < n; ++i)
								player().addItem(Item.createRandom().setQuantity(Utils.rand(1, 10)));
						})
						.show();
			}, Keys.CONTROL_LEFT, Keys.I);

			/* Print NPC info */
			bind(() -> {
				AbstractNpc npc = Mouse.pointingAt().getCell().getNpc();
				if (npc.isEmpty())
					return;

				Gui.drawOkDialog("NPC Info", String.format("%s\n%s\n%s", npc, npc.stats().baseStats, npc.inventory));
			}, Keys.CONTROL_LEFT, Keys.SHIFT_LEFT, Keys.I);

			/* Teleport to selected tile */
			bind(() -> {
				Mouse.setClickAction((x, y) -> {
					Node dest = Mouse.getPathToCursor().getDestination();
					player().teleport(dest.x, dest.y);
				}, player().getFov());
			}, Keys.Y);

			/* Skip one hour of world time */
			bind(() -> {
				World world = world();
				world.skipToNextHour();
				world.updateLight();
				Gui.refreshOverlay();
			}, Keys.F5);

			/* Regenerate current location */
			bind(() -> {
				world().close();
				world().generate();
				MadSand.enterWorld();
			}, Keys.CONTROL_LEFT, Keys.R);

			/* Ascend / descend one layer */
			bind(world()::descend, Keys.CONTROL_LEFT, Keys.SHIFT_LEFT, Keys.DOWN);
			bind(world()::ascend, Keys.CONTROL_LEFT, Keys.SHIFT_LEFT, Keys.UP);

			/* Move between sectors */
			Direction.forEachBase(dir -> {
				bind(() -> {
					Player player = player();
					hitonoriol.madsand.map.Map map = world().getCurLoc();
					Pair tpVector = Pair.directionToCoord(dir);
					Pair tpDelta = dir.isPositive()
							? new Pair(map.getWidth() - player.x, map.getHeight() - player.y)
									.multiply(tpVector)
							: player.getPosition().multiply(tpVector);

					player.teleport(tpDelta.add(player.getPosition()));
					player.turn(dir);
					world().travel();
				}, Keys.CONTROL_LEFT, dir.toKey());
			});

			/* Force update light */
			bind(world()::updateLight, Keys.CONTROL_LEFT, Keys.L);
			
			/* Quick access to minigames */
			bind(() -> {
				new SelectDialog("Minigames")
						.addOption("BlackJack", () -> new BlackJackUI().show())
						.addOption("Videopoker", () -> new VideoPokerUI().show())
						.addOption("Farkle", () -> new FarkleUI().show())
						.show();
			}, Keys.CONTROL_LEFT, Keys.M);
		}
	}

	public static KeyBindManager bind(Runnable action, int key, int... modifiers) {
		return keyBinds.bind(action, key, modifiers);
	}

	public static void pollGameKeys() {
		if (inputListener.keysHeld() && !inputIgnored())
			keyBinds.pollKeys();
	}

	public static void pollGlobalHotkeys() {
		if (Gdx.input.isKeyJustPressed(Keys.F12))
			Resources.takeScreenshot();
	}

	public static boolean isKeyPressed(int key) {
		return inputListener.isKeyPressed(key);
	}

	public static void resetState() {
		inputListener.resetState();
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

	public static GameInputListener getListener() {
		return inputListener;
	}

	public static KeyBindManager getKeyBindManager() {
		return keyBinds;
	}
}
