package ru.bernarder.fallenrisefromdust;

import java.util.Vector;

public class Loot {
	int nodes = 0;
	Vector<Item> contents = new Vector<Item>();

	public Loot(Item cont) {
		add(cont);
	}

	public Loot() {
		this(new Item());
	}

	String getContents() {
		String ret = "";
		for (Item item : contents) {
			ret += item.getString() + Item.BLOCK_DELIM;
		}
		if (ret == "")
			return Item.EMPTY_QUERY;
		return ret;
	}

	boolean isEmpty() {
		for (int i = 0; i < contents.size(); ++i)
			if (contents.get(i).id != 0)
				return false;
		return true;
	}

	public Loot remove(int idx) {
		contents.remove(idx);
		--nodes;
		return this;
	}

	public Loot add(int id, int q) {
		if (id == 0)
			return Map.nullLoot;
		++nodes;
		Utils.out("Loot add " + id + " " + q + " Nodes: " + nodes);
		contents.add(new Item(id, q));
		return this;
	}

	public Loot add(Item item) {
		return add(item.id, item.quantity);
	}

	private static int addLootQ(String temp, boolean inventory, int x, int y, Map map) { // Don't look here
		int i = 0;
		if (temp.equals("n"))
			return -1;
		Utils.out("Loot Cell contents: " + temp);
		try {
			String[] block = temp.split(":");
			String[] attr = new String[0];
			int id, q;
			if (!temp.equals("n")) {
				while (i < block.length) {
					attr = block[i].split("/");
					id = Integer.parseInt(attr[0]);
					q = Integer.parseInt(attr[1]);
					if (inventory)
						World.player.inventory.putItem(id, q, false);
					else
						map.putLoot(x, y, id, q);
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

	static int addLootQ(String query) { // This static abomination adds items by query to inventory
		return addLootQ(query, true, 0, 0, null);
	}

	static int addLootQ(String query, int x, int y, Map map) { // and this thing puts items to map by their query
		return addLootQ(query, false, x, y, map);
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
			if (World.player.inventory.getSameCell(id, q) == -1)
				return false;
			i++;
		}
		return true;
	}
}
