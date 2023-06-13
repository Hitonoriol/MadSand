package hitonoriol.madsand.input;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.textgenerator.CellInfoGenerator;
import hitonoriol.madsand.gui.textgenerator.NotificationGenerator;
import hitonoriol.madsand.gui.textgenerator.StaticTextGenerator;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.pathfinding.Path;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Functional;

public class Mouse {
	public static int x = 0, y = 0; // Coords of mouse cursor on the screen
	public static Pair prevCoords = new Pair();
	public static int wx = 0, wy = 0; // Coords of the cell of map that mouse is currently pointing at
	private static Set<Integer> heldButtons = new HashSet<>();

	private static Vector3 worldCoords = new Vector3(0.0F, 0.0F, 0.0F);

	private static BiConsumer<Integer, Integer> clickAction = null;

	private static CellInfoGenerator cellInfo = new CellInfoGenerator();
	private static NotificationGenerator notifications = new NotificationGenerator();
	private static StaticTextGenerator clickActionText = new StaticTextGenerator(
		"[LMB] choose this tile" + Resources.LINEBREAK + "[RMB] cancel" + Resources.LINEBREAK
	);

	private static Path rangedPath;
	private static Path pathToCursor = new Path();
	private static int DEF_CUR_PATH_LEN = 8, maxCurPathLen = DEF_CUR_PATH_LEN;

	public static void initListener() {
		var overlay = Gui.overlay;
		initTooltip();
		overlay.addListener(new ClickListener(Buttons.LEFT) {
			private boolean ignoreClick = false;

			boolean skipClick() {
				boolean ret = ignoreClick;
				if (ignoreClick)
					ignoreClick = false;
				return ret;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				heldButtons.remove(button);

				if (overlay.getKeyboardFocus() != null)
					return;

				if (Mouse.hasClickAction()) {
					if (event.getButton() == Buttons.LEFT)
						Mouse.performClickAction();
					else if (event.getButton() == Buttons.RIGHT) {
						MadSand.print("You change your mind");
						Mouse.cancelClickAction();
					}
				}

				else if (event.getButton() == Buttons.RIGHT) {
					if (Gui.isDialogActive())
						return;

					toggleContextMenu();
				}
			}

			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (skipClick())
					return;

				handleMouseClick();
			}

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (ignoreClick = Gui.isGameUnfocused() || !Mouse.isInteractionPossible())
					heldButtons.add(button);

				super.touchDown(event, x, y, pointer, button);
				return true;
			}

			@Override
			public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
				if (Keyboard.isKeyPressed(Keys.CONTROL_LEFT)) {
					MadSand.getRenderer().changeZoom(0.05f * Math.signum(-amountY));
				}
				return super.scrolled(event, x, y, amountX, amountY);
			}
		});
	}

	private static void toggleContextMenu() {
		var menu = Gui.overlay.getContextMenu();
		if (Gui.overlay.getTooltip().isVisible()) {
			menu.open();
			cellInfo.getCell().populateContextMenu(menu);
		} else
			menu.close();
	}

	private static void initTooltip() {
		notifications.setEnabled(false);
		clickActionText.setEnabled(false);
		Functional.with(Gui.overlay.getTooltip(), tooltip -> {
			tooltip.addTextGenerator(new StaticTextGenerator((x, y) -> String.format("Looking at (%d, %d)", x, y)))
				.addTextGenerator(clickActionText)
				.addTextGenerator(notifications)
				.addTextGenerator(cellInfo);
		});
	}

	public static NotificationGenerator getNotificator() {
		return notifications;
	}

	public static void updScreenCoords() {
		x = Gdx.input.getX();
		y = Gdx.graphics.getHeight() - Gdx.input.getY();
	}

	public static void update() {
		updScreenCoords();
		Gui.overlay.getTooltip().moveTo(x, y);

		worldCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		MadSand.getRenderer().getCamera().unproject(worldCoords);
		wx = (int) Math.floor(worldCoords.x / Resources.TILESIZE);
		wy = (int) Math.floor(worldCoords.y / Resources.TILESIZE);

		if (Gui.isGameUnfocused() || prevCoords.equals(wx, wy))
			return;
		prevCoords.set(wx, wy);

		if (hasClickAction())
			refreshPathToCursor();

		refreshTooltip();
	}

	public static void refreshTooltip() {
		Gui.overlay.getTooltip().refresh(wx, wy);
		highlightRangedTarget();
	}

	private static void refreshPathToCursor() {
		pathToCursor.clear();
		MadSand.world().getCurLoc().getPathfindingEngine()
			.searchPath(MadSand.player().x, MadSand.player().y, wx, wy, pathToCursor);

		if (!pathToCursor.isEmpty() && pathToCursor.getCount() > maxCurPathLen)
			pathToCursor.truncate(maxCurPathLen);
	}

	private static void highlightRangedTarget() {
		var player = MadSand.player();
		var renderer = MadSand.getRenderer();

		if (!player.canPerformRangedAttack())
			return;

		int x = cellInfo.getX(), y = cellInfo.getY();
		if (!cellInfo.getCell().hasNpc()) {
			if (rangedPath != null)
				renderer.removePath(rangedPath);
		}

		else if (
			rangedPath == null
				|| (rangedPath != null && !rangedPath.getDestination().at(x, y))
		) {
			renderer.removePath(rangedPath);
			renderer.queuePath(rangedPath = Path.create(player.x, player.y, x, y));
		}

		if (rangedPath != null) {
			var destNode = rangedPath.getDestination();
			if (!MadSand.world().getCurLoc().npcExists(destNode.x, destNode.y))
				renderer.removePath(rangedPath);

			if (!renderer.isPathQueued(rangedPath))
				rangedPath = null;
		}
	}

	public static Path getPathToCursor() {
		return pathToCursor;
	}

	public static CellInfoGenerator pointingAt() {
		return cellInfo;
	}

	public static boolean hasClickAction() {
		return clickAction != null;
	}

	public static void setClickAction(BiConsumer<Integer, Integer> coordConsumer, int maxPathLen) {
		maxCurPathLen = maxPathLen;
		clickAction = coordConsumer;
		clickActionText.setEnabled(true);
		refreshPathToCursor();
		refreshTooltip();
		MadSand.getRenderer().enableFloatingCamera(true);
	}

	public static void setClickAction(BiConsumer<Integer, Integer> coordConsumer) {
		setClickAction(coordConsumer, DEF_CUR_PATH_LEN);
		MadSand.print("You think about your next move...");
	}

	public static void cancelClickAction() {
		clickAction = null;
		clickActionText.setEnabled(false);
		MadSand.getRenderer().enableFloatingCamera(false);
		refreshTooltip();
	}

	public static void performClickAction() {
		if (pathToCursor.isEmpty())
			return;

		var action = clickAction;
		cancelClickAction();

		var destination = pathToCursor.getDestination();
		action.accept(destination.x, destination.y);
	}

	public static boolean pointingAtObject() {
		return cellInfo.getCell().isOccupied();
	}

	private static final int CLICK_CUR_TILE = 0, CLICK_ADJ_TILE = 1;

	public static int getClickDistance() {
		return (int) Line.calcDistance(MadSand.player().x, MadSand.player().y, wx, wy);
	}

	public static boolean isInteractionPossible() {
		int clickDst = getClickDistance();
		return pointingAtObject() &&
			(clickDst == CLICK_CUR_TILE || clickDst == CLICK_ADJ_TILE || MadSand.player().canPerformRangedAttack());
	}

	public static void handleMouseClick() {
		int clickDst = getClickDistance();
		boolean adjacentTileClicked = (clickDst == CLICK_ADJ_TILE);
		boolean currentTileClicked = (clickDst == CLICK_CUR_TILE);
		var cell = cellInfo.getCell();
		var loot = cell.getLoot();
		var npc = cell.getNpc();
		var object = cell.getObject();
		var target = object != Map.nullObject ? cell.getObject() : npc;
		var player = MadSand.player();

		if ((player.isMoving()) || Gui.isGameUnfocused() || !pointingAtObject())
			return;

		if (currentTileClicked) {
			if (loot != Map.nullLoot)
				player.pickUpLoot();
			else
				player.rest();
		}

		else if (adjacentTileClicked) {
			player.lookAtMouse(wx, wy, true);

			if (!npc.isNeutral())
				player.meleeAttack();
			else
				player.interact();
		}

		else if (clickDst > 1 && !target.isEmpty())
			player.rangedAttack(target.getPosition());

		refreshTooltip();
	}

	public static void pollMouseMovement() {
		if (Keyboard.inputIgnored())
			return;

		if (isButtonPressed(Buttons.MIDDLE))
			MadSand.player().lookAtMouse(wx, wy);

		else if (isButtonPressed(Buttons.LEFT)) {
			MadSand.player().lookAtMouse(wx, wy);
			MadSand.player().walk(MadSand.player().stats.look);
		}
	}

	public static int screenX() {
		return x;
	}

	public static int screenY() {
		return y;
	}

	public static float worldX() {
		return worldCoords.x;
	}

	public static float worldY() {
		return worldCoords.y;
	}

	public static boolean isButtonPressed(int button) {
		return heldButtons.contains(button);
	}

	public static boolean isAnyButtonPressed() {
		return !heldButtons.isEmpty();
	}
}
