package hitonoriol.madsand;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector3;

import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.gui.widgets.GameTooltip;
import hitonoriol.madsand.map.CellInfo;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.pathfinding.Node;
import hitonoriol.madsand.pathfinding.Path;
import hitonoriol.madsand.screens.GameWorldRenderer;
import hitonoriol.madsand.world.World;

public class Mouse {
	public static int x = 0, y = 0; // Coords of mouse cursor on the screen
	public static Pair prevCoords = new Pair();
	public static int wx = 0, wy = 0; // Coords of the cell of map that mouse is currently pointing at
	public static Set<Integer> heldButtons = new HashSet<>();

	public static Vector3 mouseWorldCoords = new Vector3(0.0F, 0.0F, 0.0F);

	public static GameTooltip tooltipContainer;

	private static boolean pointingAtObject = false;
	private static BiConsumer<Integer, Integer> clickAction = null;
	private static CellInfo cellInfo = new CellInfo();

	private static Path rangedPath;
	private static Path pathToCursor = new Path();
	private static int DEF_CUR_PATH_LEN = 8, maxCurPathLen = DEF_CUR_PATH_LEN;

	public static void updCoords() {
		x = Gdx.input.getX();
		y = Gdx.graphics.getHeight() - Gdx.input.getY();
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

		cellInfo.set(wx, wy);
		pointingAtObject = cellInfo.isCellOccupied();
		highlightRangedTarget();
		Gui.overlay.getTooltip().setText(cellInfo.getInfo());
	}

	private static void refreshPathToCursor() {
		pathToCursor.clear();
		MadSand.world.getCurLoc().searchPath(World.player.x, World.player.y, wx, wy, pathToCursor);

		if (!pathToCursor.isEmpty() && pathToCursor.getCount() > maxCurPathLen)
			pathToCursor.truncate(maxCurPathLen);
	}

	private static void highlightRangedTarget() {
		Player player = World.player;
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
			if (!MadSand.world.getCurLoc().npcExists(destNode.x, destNode.y))
				renderer.removePath(rangedPath);

			if (!renderer.isPathQueued(rangedPath))
				rangedPath = null;
		}
	}

	public static Path getPathToCursor() {
		return pathToCursor;
	}

	public static CellInfo pointingAt() {
		return cellInfo;
	}

	public static boolean hasClickAction() {
		return clickAction != null;
	}

	public static void setClickAction(BiConsumer<Integer, Integer> coordConsumer, int maxPathLen) {
		maxCurPathLen = maxPathLen;
		clickAction = coordConsumer;
		refreshPathToCursor();
	}

	public static void setClickAction(BiConsumer<Integer, Integer> coordConsumer) {
		setClickAction(coordConsumer, DEF_CUR_PATH_LEN);
		MadSand.print("You think about your next move...");
	}

	public static void cancelClickAction() {
		clickAction = null;
	}

	public static void performClickAction() {
		if (pathToCursor.isEmpty())
			return;

		Node destination = pathToCursor.getDestination();
		clickAction.accept(destination.x, destination.y);
		cancelClickAction();
	}

	private static final int CLICK_CUR_TILE = 0, CLICK_ADJ_TILE = 1;

	public static int getClickDistance() {
		return (int) Line.calcDistance(World.player.x, World.player.y, wx, wy);
	}

	public static boolean isClickActionPossible() {
		int clickDst = getClickDistance();
		return pointingAtObject ||
				clickDst == CLICK_CUR_TILE;
	}

	public static void mouseClickAction() {
		int clickDst = getClickDistance();
		boolean adjacentTileClicked = (clickDst == CLICK_ADJ_TILE);
		boolean currentTileClicked = (clickDst == CLICK_CUR_TILE);
		Loot loot = cellInfo.getLoot();
		AbstractNpc npc = cellInfo.getNpc();
		Player player = World.player;

		if ((player.isStepping()) || Gui.isGameUnfocused() || !pointingAtObject)
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

		else if (clickDst > 1 && npc != Map.nullNpc)
			player.rangedAttack(npc);
	}

	public static void pollMouseMovement() {
		if (Keyboard.inputIgnored())
			return;

		if (heldButtons.contains(Buttons.LEFT)) {
			World.player.lookAtMouse(wx, wy);
			World.player.walk(World.player.stats.look);
		}
	}

}
