package ru.bernarder.fallenrisefromdust;

import java.util.Vector;

import ru.bernarder.fallenrisefromdust.strings.InventoryNames;

public class Loot {
	Vector<String> contents = new Vector<String>();

	public Loot(String cont) {
		contents.add(cont);
	}

	static void lootCollision() {
		if (Player.standingOnLoot(MadSand.player.x, MadSand.player.y)) {
			addLootToInv(MadSand.player.x, MadSand.player.y);
			MadSand.world.getCurLoc().removeLoot(MadSand.player.x, MadSand.player.y);
			MadSand.print("You picked some things up");
		}
	}

	public Loot add(int id, int q) {

		return this;
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

					MadSand.player.inventory.putItem(Integer.parseInt(attr[0]), Integer.parseInt(attr[1]), false);
					i++;
				}
				return Integer.parseInt(attr[0]);
			}
			return 0;
		} catch (Exception localException) {
		}

		return 0;
	}

	public static boolean removeLootFromInv(String query) { // TODO --> to inventory
		int i = 1;
		String temp = query;
		try {
			String[] block = temp.split(":");
			String[] attr = new String[0];
			while (i < block.length) {
				attr = block[i].split("/");
				if (MadSand.player.inventory.getSameCell(Integer.parseInt(attr[0]), Integer.parseInt(attr[1])) == -1) {
					return false;
				}
				i++;
			}
			i = 1;
			while (i < block.length) {
				attr = block[i].split("/");
				MadSand.player.inventory.delItem(Integer.parseInt(attr[0]), Integer.parseInt(attr[1]));
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
			if (MadSand.player.inventory.getSameCell(id, q) == -1) {
				return false;
			}
			i++;
		}
		return true;
	}
}
