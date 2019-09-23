package ru.bernarder.fallenrisefromdust;

import java.util.ArrayList;
import java.util.Map.Entry;

import ru.bernarder.fallenrisefromdust.properties.ItemProp;
import ru.bernarder.fallenrisefromdust.properties.ObjectProp;

import java.util.StringTokenizer;

public class BuildScript {

	/* [condition] [token] [value || constant]; */
	final static char printID = '=';
	final static char standingOnID = '!'; // !1 player_give @gold 100
	final static char objInFrontID = '^';
	final static char inHandID = '#';
	final static char valueID = '$';
	final static char itemStringID = '@';
	final static String LINE_DELIMITER = ";";
	final static String COMMAND_DELIMITER = " ";

	public static class Value {
		final static String VALUE_PLAYERX = "$player_x", VALUE_PLAYERY = "$player_y",
				VALUE_PLAYER_LOOK_X = "$player_look_x", VALUE_PLAYER_LOOK_Y = "$player_look_y",
				VALUE_ALTITEM = "$altitem";
	}

	public static class Token {
		final static String PLACE_OBJECT = "place_object", REMOVE_OBJECT = "remove_object", PLACE_TILE = "place_tile",
				MAP_CLEAR = "map_clear", OBJECT_SQUARE = "object_square", TILE_SQUARE = "tile_square",
				OBJECT_LINE = "object_line", TILE_LINE = "tile_line", PLAYER_GIVE = "player_give",
				DAMAGE_OBJECT = "damage_object", PLAYER_HEAL = "player_heal", PLAYER_SATIATE = "player_satiate",
				PLAYER_REMOVE_ITEM = "player_remove_item", PLAYER_HAND_SET = "player_hand_set";
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
		Pair coords = new Pair(MadSand.player.x, MadSand.player.y);
		switch (arg) {
		case Value.VALUE_PLAYERX:
			return coords.x;
		case Value.VALUE_PLAYERY:
			return coords.y;
		case Value.VALUE_PLAYER_LOOK_X:
			return coords.addDirection(MadSand.player.look).x;
		case Value.VALUE_PLAYER_LOOK_Y:
			return coords.addDirection(MadSand.player.look).y;
		case Value.VALUE_ALTITEM:
			int id = MadSand.world.getCurLoc().getObject(MadSand.player.x, MadSand.player.y, MadSand.player.look).id;
			return MapObject.getAltItem(id);

		default:
			return 0;
		}
	}

	static int getItemID(String arg) {
		for (Entry<Integer, String> entry : ItemProp.name.entrySet()) {
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

	static boolean conditionFalse(char id, String cond) {
		int sid = Integer.parseInt(cond.split(COMMAND_DELIMITER)[0].substring(1));
		int x = MadSand.player.x;
		int y = MadSand.player.y;
		boolean condition = false;
		switch (id) {
		case standingOnID:
			condition = (MadSand.world.getCurLoc().getTile(x, y).id == sid);
			break;
		case objInFrontID:
			condition = (MadSand.world.getCurLoc().getObject(x, y, MadSand.player.look).id == sid);
			break;
		case inHandID:
			condition = (MadSand.player.hand == sid);
			break;
		}
		return !condition;
	}

	public static void execute(String command, StringTokenizer tokens) {
		ArrayList<Integer> arg = getAllArgs(tokens);
		int x, y, id, i, height, dir;
		char cid = command.charAt(0);
		if (!Character.isLetter(cid)) {
			if (conditionFalse(cid, command))
				return;
			else
				command = command.split(COMMAND_DELIMITER)[1];
		}
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

		case Token.PLAYER_HAND_SET:
			id = arg.get(0);
			MadSand.player.hand = id;
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
		char id;
		try {
			while (lineTokens.hasMoreTokens()) {
				commandTokens = new StringTokenizer(lineTokens.nextToken(), COMMAND_DELIMITER);
				command = commandTokens.nextToken().trim();
				id = command.charAt(0);
				if (id == printID) {
					MadSand.print(query.substring(command.length()));
					continue;
				}
				if (!Character.isLetter(id))
					command += COMMAND_DELIMITER + commandTokens.nextToken();
				execute(command, commandTokens);
			}
		} catch (Exception e) {
			MadSand.print("An error has occured in script: " + e.getMessage());
			e.printStackTrace();
		}
	}
}