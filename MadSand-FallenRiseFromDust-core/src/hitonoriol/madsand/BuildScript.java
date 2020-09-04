package hitonoriol.madsand;

import java.util.ArrayList;
import java.util.Map.Entry;

import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.Tutorial;
import hitonoriol.madsand.world.World;

import java.util.StringTokenizer;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class BuildScript {

	/*
	 * Syntax:
	 * 
	 * [condition] [command] [variable || constant];
	 * 
	 */

	final static String LINE_DELIMITER = ";";
	final static String COMMAND_DELIMITER = " ";

	public static class Prefix {
		final static char VARIABLE = '$';
		final static char ITEM_STRING = '@';
		final static char MODE = '*';
	}

	public static class Condition {
		final static char PRINT = '=';
		final static char STANDING_ON = '!'; // !1 player_give @gold 100
		final static char NO_CONDITION = '-';
		final static char OBJECT_IN_FRONT = '^';
		final static char IN_HAND = '#';
	}

	public static class Mode {
		final static String NONSTRICT = "nonstrict", STRICT = "strict"; // strict = stop execution after the first
																		// false condition
	}

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
				PLAYER_REMOVE_ITEM = "player_remove_item", PLAYER_HAND_SET = "player_hand_set",
				PLAYER_KILL = "player_kill", NPC_SPAWN = "npc_spawn", CHAIN_DIALOG = "chain_dialog",
				DUNGEON_DESCEND = "dungeon_descend", DUNGEON_ASCEND = "dungeon_ascend", TUTORIAL_SHOW = "tutorial_show";
	}

	public static void bLine(int x, int y, int dir, int id, int len, int head) {
		int ii = 0;
		while (ii < len) {
			if (head == 0)
				MadSand.world.getCurLoc().addObject(x + ii * dir, y, id);
			else
				MadSand.world.getCurLoc().addObject(x, y + ii * dir, id);
			ii++;
		}
	}

	public static void tLine(int x, int y, int dir, int id, int len, int head) {
		int ii = 0;
		while (ii < len) {
			if (head == 0)
				MadSand.world.getCurLoc().addTile(x + ii * dir, y, id, true);
			else
				MadSand.world.getCurLoc().addTile(x, y + ii * dir, id, true);
			ii++;
		}
	}

	static int getValue(String arg) {
		Pair coords = new Pair(World.player.x, World.player.y);
		switch (arg) {

		case Value.VALUE_PLAYERX:
			return coords.x;

		case Value.VALUE_PLAYERY:
			return coords.y;

		case Value.VALUE_PLAYER_LOOK_X:
			return coords.addDirection(World.player.stats.look).x;

		case Value.VALUE_PLAYER_LOOK_Y:
			return coords.addDirection(World.player.stats.look).y;

		case Value.VALUE_ALTITEM:
			int id = MadSand.world.getCurLoc().getObject(World.player.x, World.player.y, World.player.stats.look).id;
			return MapObject.getAltItem(id, World.player.stats.hand.id);

		default:
			return 0;
		}
	}

	static int getItemID(String arg) {
		for (Entry<Integer, String> entry : ItemProp.name.entrySet()) {
			if (entry.getValue().equalsIgnoreCase(arg))
				return entry.getKey();
		}
		return -1;
	}

	static int val(String arg) {
		char id = arg.charAt(0);

		if (id == Prefix.VARIABLE)
			return getValue(arg);

		if (id == Prefix.ITEM_STRING)
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
		int x = World.player.x;
		int y = World.player.y;
		boolean condition = false;
		switch (id) {

		case Condition.NO_CONDITION:
			condition = true;
			break;

		case Condition.STANDING_ON:
			condition = (MadSand.world.getCurLoc().getTile(x, y).id == sid);
			break;

		case Condition.OBJECT_IN_FRONT:
			condition = (MadSand.world.getCurLoc().getObject(x, y, World.player.stats.look).id == sid);
			break;

		case Condition.IN_HAND:
			condition = (World.player.stats.hand.id == sid);
			break;

		}

		if (!condition && strict)
			stop = true;
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
		case Token.DUNGEON_DESCEND:
			MadSand.world.descend();
			break;
		case Token.DUNGEON_ASCEND:
			MadSand.world.ascend();
			break;
		case Token.NPC_SPAWN:
			x = arg.get(0);
			y = arg.get(1);
			id = arg.get(2);

			MadSand.world.getCurLoc().putNpc(new Npc(id, x, y), x, y);
			break;
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
			MadSand.world.getCurLoc().dmgObjInDir(World.player.x, World.player.y, World.player.stats.look);
			break;

		case Token.PLAYER_HEAL:
			int amount = arg.get(0);

			World.player.heal(amount);
			break;

		case Token.PLAYER_GIVE:
			id = arg.get(0);
			int q = arg.get(1);

			World.player.addItem(id, q);
			break;

		case Token.PLAYER_REMOVE_ITEM:
			id = arg.get(0);
			q = arg.get(1);

			World.player.inventory.delItem(id, q);
			break;

		case Token.PLAYER_KILL:
			World.player.damage(World.player.stats.hp);
			break;
		}
	}

	static boolean stop = false;

	static boolean strict = false;

	public static void execute(String query) {
		query.replaceAll("\n", "");
		query = query.trim();
		if (query.length() == 0)
			return;

		Utils.out("Executing: {" + query + "}");
		StringTokenizer lineTokens = new StringTokenizer(query, LINE_DELIMITER);
		StringTokenizer commandTokens;
		String command;
		String printCond;
		String line;
		char id;
		try {
			while (lineTokens.hasMoreTokens() && !stop) {
				line = lineTokens.nextToken().trim();
				commandTokens = new StringTokenizer(line, COMMAND_DELIMITER);
				command = commandTokens.nextToken().trim();
				id = command.charAt(0);
				switch (id) {

				case Condition.PRINT:
					printCond = commandTokens.nextToken();
					if (!conditionFalse(printCond.charAt(0), printCond + COMMAND_DELIMITER))
						MadSand.print(line.substring(printCond.length() + command.length() + 2).trim());
					continue;

				case Prefix.MODE:
					switch (command.substring(1)) {

					case Mode.NONSTRICT:
						strict = false;
						break;

					case Mode.STRICT:
						strict = true;
						break;

					}
					break;

				}

				if (command == Token.CHAIN_DIALOG) {
					String text = line.substring(command.length());
					GameDialog.generateDialogChain(text, Gui.overlay).show();
					continue;
				} else if (command == Token.TUTORIAL_SHOW) {
					String name = line.substring(command.length()).trim();
					GameDialog.generateDialogChain(Tutorial.strings.get(name), Gui.overlay).show();
					continue;
				}

				if (!Character.isLetter(id))
					command += COMMAND_DELIMITER + commandTokens.nextToken();

				execute(command, commandTokens);
			}

			if (stop)
				stop = false;

		} catch (Exception e) {
			MadSand.print("An error occured in script: " + ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		}
	}
}