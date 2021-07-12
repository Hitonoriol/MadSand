package hitonoriol.madsand.input;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.gui.stages.Overlay;
import hitonoriol.madsand.gui.textgenerator.CellInfoGenerator;
import hitonoriol.madsand.gui.textgenerator.NotificationGenerator;
import hitonoriol.madsand.gui.textgenerator.StaticTextGenerator;
import hitonoriol.madsand.gui.widgets.GameTooltip;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapEntity;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.pathfinding.Node;
import hitonoriol.madsand.pathfinding.Path;
import hitonoriol.madsand.screens.GameWorldRenderer;
import hitonoriol.madsand.util.Functional;

public class Mouse {
	public static int x = 0, y = 0; // Coords of mouse cursor on the screen
	public static Pair prevCoords = new Pair();
	public static int wx = 0, wy = 0; // Coords of the cell of map that mouse is currently pointing at
	public static Set<Integer> heldButtons = new HashSet<>();

	public static Vector3 mouseWorldCoords = new Vector3(0.0F, 0.0F, 0.0F);

	public static GameTooltip tooltipContainer;
	private static BiConsumer<Integer, Integer> clickAction = null;

	private static CellInfoGenerator cellInfo = new CellInfoGenerator();
	private static NotificationGenerator notifications = new NotificationGenerator();
	private static StaticTextGenerator clickActionText = new StaticTextGenerator(
			"[LMB] choose this tile" + Resources.LINEBREAK + "[RMB] cancel" + Resources.LINEBREAK);

	private static Path rangedPath;
	private static Path pathToCursor = new Path();
	private static int DEF_CUR_PATH_LEN = 8, maxCurPathLen = DEF_CUR_PATH_LEN;

	public static void initListener() {
		Overlay overlay = Gui.overlay;
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
					if (Gui.dialogActive)
						return;

					if (overlay.gameTooltip.isVisible()) {
						overlay.gameContextMenu.openGameContextMenu();
						Gui.gameUnfocused = true;
					} else
						overlay.gameContextMenu.closeGameContextMenu();
				}
			}

			public void clicked(InputEvent event, float x, float y) {
				if (skipClick())
					return;

				handleMouseClick();
			}

			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				ignoreClick = Gui.isGameUnfocused() || Gui.dialogActive;
				ignoreClick |= !Mouse.isInteractionPossible();

				if (ignoreClick)
					heldButtons.add(button);

				super.touchDown(event, x, y, pointer, button);
				return true;
			}
		});
	}

	private static void initTooltip() {
		notifications.setEnabled(false);
		clickActionText.setEnabled(false);
		Functional.with(GameTooltip.instance(), tooltip -> {
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

	public static void updCoords() {
		updScreenCoords();
		tooltipContainer.moveTo(x, y);

		wx = (int) Math.floor(mouseWorldCoords.x / MadSand.TILESIZE);
		wy = (int) Math.floor(mouseWorldCoords.y / MadSand.TILESIZE);

		if (Gui.gameUnfocused)
			return;

		if (prevCoords.equals(wx, wy))
			return;
		prevCoords.set(wx, wy);

		if (hasClickAction())
			refreshPathToCursor();

		refreshTooltip();
	}

	public static void refreshTooltip() {
		GameTooltip.instance().refresh(wx, wy);
		highlightRangedTarget();
	}

	private static void refreshPathToCursor() {
		pathToCursor.clear();
		MadSand.world().getCurLoc().searchPath(MadSand.player().x, MadSand.player().y, wx, wy, pathToCursor);

		if (!pathToCursor.isEmpty() && pathToCursor.getCount() > maxCurPathLen)
			pathToCursor.truncate(maxCurPathLen);
	}

	private static void highlightRangedTarget() {
		Player player = MadSand.player();
		GameWorldRenderer renderer = MadSand.getRenderer();

		if (!player.canPerformRangedAttack())
			return;

		int x = cellInfo.getX(), y = cellInfo.getY();
		if (!cellInfo.hasNpc()) {
			if (rangedPath != null)
				renderer.removePath(rangedPath);
		}

		else if (rangedPath == null
				|| (rangedPath != null && !rangedPath.getDestination().at(x, y))) {
			renderer.removePath(rangedPath);
			renderer.queuePath(rangedPath = Path.create(player.x, player.y, x, y));
		}

		if (rangedPath != null) {
			Node destNode = rangedPath.getDestination();
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
	}

	public static void setClickAction(BiConsumer<Integer, Integer> coordConsumer) {
		setClickAction(coordConsumer, DEF_CUR_PATH_LEN);
		MadSand.print("You think about your next move...");
	}

	public static void cancelClickAction() {
		clickAction = null;
		clickActionText.setEnabled(false);
		refreshTooltip();
	}

	public static void performClickAction() {
		if (pathToCursor.isEmpty())
			return;

		BiConsumer<Integer, Integer> action = clickAction;
		cancelClickAction();

		Node destination = pathToCursor.getDestination();
		action.accept(destination.x, destination.y);
	}

	public static boolean pointingAtObject() {
		return cellInfo.isCellOccupied();
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
		Loot loot = cellInfo.getLoot();
		AbstractNpc npc = cellInfo.getNpc();
		MapObject object = cellInfo.getObject();
		MapEntity target = object != Map.nullObject ? cellInfo.getObject() : npc;
		Player player = MadSand.player();

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

			if (npc.state == AbstractNpc.State.Hostile)
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

		if (heldButtons.contains(Buttons.MIDDLE))
			MadSand.player().lookAtMouse(wx, wy);

		else if (heldButtons.contains(Buttons.LEFT)) {
			MadSand.player().lookAtMouse(wx, wy);
			MadSand.player().walk(MadSand.player().stats.look);
		}
	}

}
