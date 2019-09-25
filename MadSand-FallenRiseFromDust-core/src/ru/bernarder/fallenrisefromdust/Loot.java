package ru.bernarder.fallenrisefromdust;

import java.util.Vector;

import ru.bernarder.fallenrisefromdust.properties.ItemProp;

public class Loot {
	Vector<Item> contents = new Vector<Item>();

	public Loot(Item cont) {
		contents.add(cont);
	}

	public Loot() {
		this(new Item());
	}

	String getContents() {
		String ret = "";
		for (Item item : contents) {
			ret += item.getString() + "/";
		}
		return ret;
	}

	void lootCollision() {
		if (MadSand.player.standingOnLoot()) {
			addLootToInv(MadSand.player.x, MadSand.player.y);
			MadSand.world.getCurLoc().removeLoot(MadSand.player.x, MadSand.player.y);
			MadSand.print("You picked some things up");
		}
	}

	public Loot add(int id, int q) {
		contents.add(new Item(id, q));
		return this;
	}

	public void addLootToInv(int x, int y) {
		for (Item item : contents) {
			if (MadSand.player.inventory.putItem(item.id, item.quantity))
				contents.remove(item);
		}
	}

	public static int addLootQToInv(String temp) {
		int i = 0;
		Utils.out("Loot Cell contents: " + temp);
		try {
			String[] block = temp.split(":");
			String[] attr = new String[0];
			if (!temp.equals("n")) {
				while (i < block.length) {
					attr = block[i].split("/");

					MadSand.player.inventory.putItem(Integer.parseInt(attr[0]), Integer.parseInt(attr[1]), false);
					i++;
				}
				return Integer.parseInt(attr[0]);
			}
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	public static boolean removeLootFromInv(String query) { // TODO --> to inventory
		int i = 0;
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
			e.printStackTrace();
		}
		return false;
	}

	public static boolean invExists(String sequence) {
		int i = 0;
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
