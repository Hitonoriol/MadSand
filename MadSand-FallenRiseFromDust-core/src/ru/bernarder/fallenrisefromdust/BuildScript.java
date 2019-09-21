package ru.bernarder.fallenrisefromdust;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import ru.bernarder.fallenrisefromdust.strings.InventoryNames;

public class BuildScript {

	/* [token] [value || constant]; */
	final static char valueID = '$';
	final static char itemStringID = '@';
	final static String LINE_DELIMITER = ";";
	final static String COMMAND_DELIMITER = " ";

	public static class Value {
		final static String VALUE_PLAYERX = "$player_x", VALUE_PLAYERY = "$player_y",
				VALUE_PLAYER_LOOK_X = "$player_look_x", VALUE_PLAYER_LOOK_Y = "$player_look_y";
	}

	public static class Token {
		final static String PLACE_OBJECT = "place_object", REMOVE_OBJECT = "remove_object", PLACE_TILE = "place_tile",
				MAP_CLEAR = "map_clear", OBJECT_SQUARE = "object_square", TILE_SQUARE = "tile_square",
				OBJECT_LINE = "object_line", TILE_LINE = "tile_line", PLAYER_GIVE = "player_give",
				DAMAGE_OBJECT = "damage_object", PLAYER_HEAL = "player_heal", PLAYER_SATIATE = "player_satiate",
				PLAYER_REMOVE_ITEM = "player_remove_item";
	}

	public static void bLine(int x, int y, int dir, int id, int len, int head) {
		int ii = 0;
		while (ii < len) {
			if (head == 0) {
				MadSand.world.getCurLoc().addObject(x + ii * dir, y, id);
			} else
				MadSand.world.getCurLoc().addObject(x, y + ii * dir, id);
			ii++;
		}
	}

	public static void tLine(int x, int y, int dir, int id, int len, int head) {
		int ii = 0;
		while (ii < len) {
			if (head == 0) {
				MadSand.world.getCurLoc().addTile(x + ii * dir, y, id);
			} else
				MadSand.world.getCurLoc().addTile(x, y + ii * dir, id);
			ii++;
		}
	}

	static int getValue(String arg) {
		switch (arg) {
		case Value.VALUE_PLAYERX:
			return MadSand.player.x;
		case Value.VALUE_PLAYERY:
			return MadSand.player.y;
		default:
			return 0;
		}
	}

	static int getItemID(String arg) {
		for (Entry<Integer, String> entry : InventoryNames.name.entrySet()) {
			if (entry.getValue().equalsIgnoreCase(arg)) {
				return entry.getKey();
			}
		}
		return -1;
	}

	static int val(String arg) {
		char id = arg.charAt(0);
		if (id == valueID)
			return getValue(arg);
		if (id == itemStringID)
			return getItemID(arg.substring(1));
		return Integer.parseInt(arg);
	}

	static ArrayList<Integer> getAllArgs(StringTokenizer tokens) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		while (tokens.hasMoreTokens())
			ret.add(val(tokens.nextToken()));
		return ret;
	}

	public static void execute(String command, StringTokenizer tokens) {
		ArrayList<Integer> arg = getAllArgs(tokens);
		int x, y, id, i, height, dir;
		switch (command) {
		case Token.PLACE_OBJECT:
			x = arg.get(0);
			y = arg.get(1);
			id = arg.get(2);
			MadSand.world.getCurLoc().addObject(x, y, id);
			break;

		case Token.PLACE_TILE:
			x = arg.get(0);
			y = arg.get(1);
			id = arg.get(2);
			MadSand.world.putMapTile(x, y, id);
			break;

		case Token.MAP_CLEAR:
			MadSand.world.clearCurLoc();
			break;

		case Token.OBJECT_SQUARE:
			i = 0;
			height = arg.get(3);
			id = arg.get(2);
			x = arg.get(0);
			y = arg.get(1);
			while (i < height) {
				bLine(x, y, 1, id, height, 1);
				x++;
				i++;
			}
			break;

		case Token.OBJECT_LINE:
			x = arg.get(0);
			y = arg.get(1);
			dir = arg.get(2);
			id = arg.get(3);
			int len = arg.get(4);
			int isVertical = arg.get(5);
			bLine(x, y, dir, id, len, isVertical);
			break;

		case Token.TILE_SQUARE:
			i = 0;
			height = arg.get(3);
			id = arg.get(2);
			x = arg.get(0);
			y = arg.get(1);
			while (i < height) {
				tLine(x, y, 1, id, height, 1);
				x++;
				i++;
			}
			break;

		case Token.TILE_LINE:
			x = arg.get(0);
			y = arg.get(1);
			dir = arg.get(2);
			id = arg.get(3);
			len = arg.get(4);
			isVertical = arg.get(5);
			bLine(x, y, dir, id, len, isVertical);
			break;

		case Token.REMOVE_OBJECT:
			x = arg.get(0);
			y = arg.get(1);
			MadSand.world.getCurLoc().delObject(x, y);
			break;

		case Token.DAMAGE_OBJECT:
			MadSand.world.getCurLoc().dmgObjInDir(MadSand.player.x, MadSand.player.y, MadSand.player.look);
			break;

		case Token.PLAYER_HEAL:
			int amount = arg.get(0);
			MadSand.player.heal(amount);
			break;

		case Token.PLAYER_GIVE:
			id = arg.get(0);
			int q = arg.get(1);
			MadSand.player.inventory.putItem(id, q);
			break;
			
		case Token.PLAYER_REMOVE_ITEM:
			id = arg.get(0);
			q = arg.get(1);
			MadSand.player.inventory.delItem(id, q);
			break;
		}
	}

	public static void execute(String query) {
		Utils.out(query);
		query.replaceAll("\n", "");
		StringTokenizer lineTokens = new StringTokenizer(query, LINE_DELIMITER);
		StringTokenizer commandTokens;
		String command;
		try {
			while (lineTokens.hasMoreTokens()) {
				commandTokens = new StringTokenizer(lineTokens.nextToken(), COMMAND_DELIMITER);
				command = commandTokens.nextToken();
				execute(command, commandTokens);
			}
		} catch (Exception e) {
			MadSand.print("An error has occured in script: " + e.getMessage());
		}
	}
}