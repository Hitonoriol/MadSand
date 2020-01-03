package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import ru.bernarder.fallenrisefromdust.containers.Line;
import ru.bernarder.fallenrisefromdust.entities.Npc;
import ru.bernarder.fallenrisefromdust.entities.Player;
import ru.bernarder.fallenrisefromdust.enums.GameState;
import ru.bernarder.fallenrisefromdust.map.Map;
import ru.bernarder.fallenrisefromdust.map.MapObject;
import ru.bernarder.fallenrisefromdust.map.Tile;
import ru.bernarder.fallenrisefromdust.properties.ObjectProp;
import ru.bernarder.fallenrisefromdust.properties.TileProp;
import ru.bernarder.fallenrisefromdust.world.World;

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

		info += ("Tile: " + TileProp.name.get(tile.id)) + Gui.LINEBREAK;

		if (object != Map.nullObject)
			info += ("Object: " + ObjectProp.name.get(object.id)) + Gui.LINEBREAK;

		if (npc != Map.nullNpc) {
			info += ("You look at " + " " + npc.stats.name) + Gui.LINEBREAK;

			if (World.player.knowsNpc(npc.id))
				info += npc.getInfoString();

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

		if (justClicked) {
			justClicked = false;

			World.player.lookAtMouse(wx, wy, diagonal);

			if (rest) {
				World.player.rest();
				return;
			}

			if (pointingAtObject && diagonal) {
				World.player.interact();
				return;
			}

		}

		if (Gdx.input.isButtonPressed(Buttons.LEFT) && !(diagonal && pointingAtObject) && !rest) {
			World.player.lookAtMouse(wx, wy);
			World.player.walk(World.player.stats.look);
		}
	}

}
