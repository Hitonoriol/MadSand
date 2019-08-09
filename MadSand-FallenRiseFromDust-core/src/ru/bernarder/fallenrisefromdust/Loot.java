package ru.bernarder.fallenrisefromdust;

import java.util.Vector;

import ru.bernarder.fallenrisefromdust.strings.InventoryNames;

public class Loot {
	Vector<String> contents = new Vector<String>();

	public Loot(String cont) {	// LootNode(Vector of Loot objs) <-- Loot
		contents.add(cont);
	}
	

	static void lootCollision() {
		if (Player.standingOnLoot(MadSand.x, MadSand.y)) {
			addLootToInv(MadSand.x, MadSand.y);
			removeLoot(MadSand.x, MadSand.y);
			MadSand.print("You picked some things up");
		}
	}

	static void removeLoot(int x, int y) {

	}

	public static void putLoot(int x, int y, int id, int q) {

	}

	public static void addLootToInv(int x, int y) {

	}
	
	public static int addLootQToInv(String temp) {
		int i = 1;
		Utils.out("Loot Cell contents: " + temp);
		try {
			String[] block = temp.split(":");
			String[] attr = new String[0];
			if (!temp.equals("nn")) {
				while (i < block.length) {
					attr = block[i].split("/");

					InvUtils.putItem(Integer.parseInt(attr[0]), Integer.parseInt(attr[1]), false);
					i++;
				}
				return Integer.parseInt(attr[0]);
			}
			return 0;
		} catch (Exception localException) {
		}

		return 0;
	}

	public static String getTextQuery(String query) {
		int i = 1;
		String temp = query;
		String ret = "";
		try {
			String[] block = temp.split(":");
			String[] attr = new String[0];
			while (i < block.length) {
				attr = block[i].split("/");
				ret = ret + attr[1] + " " + InventoryNames.name.get(Integer.parseInt(attr[0])) + " ";
				i++;
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace(Resource.eps);
		}
		return "";
	}

	public static boolean removeLootFromInv(String query) {
		int i = 1;
		String temp = query;
		try {
			String[] block = temp.split(":");
			String[] attr = new String[0];
			while (i < block.length) {
				attr = block[i].split("/");
				if (InvUtils.getSameCell(Integer.parseInt(attr[0]), Integer.parseInt(attr[1])) == -1) {
					return false;
				}
				i++;
			}
			i = 1;
			while (i < block.length) {
				attr = block[i].split("/");
				InvUtils.delItem(Integer.parseInt(attr[0]), Integer.parseInt(attr[1]));
				i++;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace(Resource.eps);
		}
		return false;
	}

	public static boolean invExists(String sequence) {
		int i = 1;
		String temp = sequence;
		String[] block = temp.split(":");
		String[] attr = new String[0];
		int id = 0;
		int q = 0;
		while (i < block.length) {
			attr = block[i].split("/");
			id = Integer.parseInt(attr[0]);
			q = Integer.parseInt(attr[1]);
			if (InvUtils.getSameCell(id, q) == -1) {
				return false;
			}
			i++;
		}
		return true;
	}

	public static String getCell(int x, int y) {
		String t = "n";
		if (x < MadSand.MAPSIZE && y < MadSand.MAPSIZE && x >= 0 && y >= 0)
			t = "todo";
		return t;
	}
}
