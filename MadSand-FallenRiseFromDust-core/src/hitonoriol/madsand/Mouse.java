package hitonoriol.madsand;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.enums.GameState;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.world.World;

public class Mouse {
	public static int wx = 0; // Coords of the cell of map that mouse is currently pointing at
	public static int wy = 0;

	public static int x = 0; // Coords of mouse cursor on the screen
	public static int y = 0;

	static Vector3 mouseinworld = new Vector3(0.0F, 0.0F, 0.0F);

	static int wclickx = 0; // Coords of the cell of map where the mouse click occurred
	static int wclicky = 0;

	public static Map loc;
	public static Tile tile;
	public static MapObject object;
	public static Npc npc;
	public static Loot loot;

	public static boolean pointingAtObject = false; // flag that shows if mouse is pointing at object or npc or not
	public static boolean justClicked = false;

	public static void updCoords() {
		x = Gdx.input.getX();
		y = Gdx.graphics.getHeight() - Gdx.input.getY();

		wx = (int) Math.floor(Mouse.mouseinworld.x / MadSand.TILESIZE);
		wy = (int) Math.floor(Mouse.mouseinworld.y / MadSand.TILESIZE);

		if (Gui.gameUnfocused)
			return;

		loc = MadSand.world.getCurLoc();
		npc = loc.getNpc(wx, wy);
		tile = loc.getTile(wx, wy);
		object = loc.getObject(wx, wy);
		loot = loc.getLoot(wx, wy);

		pointingAtObject = (npc != Map.nullNpc) || (object != Map.nullObject);

		Gui.mousemenu.addAction(Actions.moveTo(x + 65, y - 70, 0.1F));

		Gui.mouselabel.setText(getCurrentCellInfo());
	}

	public static String getCurrentCellInfo() {
		Player player = World.player;
		String info = "";

		info += ("Looking at (" + wx + ", " + wy + ")") + Gui.LINEBREAK;

		if (wx == player.x && wy == player.y) {
			info += "You look at yourself" + Gui.LINEBREAK;
			info += player.getInfoString();
		}

		if (!tile.visible) {
			info += "You can't see anything there" + Gui.LINEBREAK;
			return info;
		}

		info += ("Tile: " + TileProp.getName(tile.id)) + Gui.LINEBREAK;

		if (loot != Map.nullLoot) {
			info += "On the ground: ";
			info += loot.getInfo() + Gui.LINEBREAK;
		}

		if (object != Map.nullObject)
			info += ("Object: " + object.name) + Gui.LINEBREAK;

		if (npc != Map.nullNpc) {
			info += ("You look at " + " " + npc.stats.name) + Gui.LINEBREAK;

			if (World.player.knowsNpc(npc.id))
				info += npc.getInfoString();
			
			if (!npc.friendly)
				info += npc.spottedMsg();

		}

		return info;
	}

	private static final int CLICK_ACTION_REST = 0;
	private static final int CLICK_ACTION_DIAGONAL = 1;

	static void mouseClickAction() {
		int dst = (int) Line.calcDistance(World.player.x, World.player.y, wx, wy);
		boolean diagonal = (dst == CLICK_ACTION_DIAGONAL);
		boolean rest = (dst == CLICK_ACTION_REST);

		if (MadSand.state != GameState.GAME)
			return;

		if ((World.player.isStepping()) || (Gui.gameUnfocused))
			return;

		if (justClicked && ((pointingAtObject && diagonal) || rest)) {
			justClicked = false;

			World.player.lookAtMouse(wx, wy, diagonal);

			if (rest && loot != Map.nullLoot) {
				World.player.pickUpLoot();
				return;
			} else if (rest) {
				World.player.rest();
				return;
			}

			if (pointingAtObject && diagonal) {
				World.player.interact();
				return;
			}

		}
		justClicked = false;

		if (Gdx.input.isButtonPressed(Buttons.LEFT) && !(diagonal && pointingAtObject) && !rest) {
			World.player.lookAtMouse(wx, wy);
			World.player.walk(World.player.stats.look);
		}
	}

}
