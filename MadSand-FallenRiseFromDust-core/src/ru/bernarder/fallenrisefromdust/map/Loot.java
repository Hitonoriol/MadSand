package ru.bernarder.fallenrisefromdust.map;

import java.util.Vector;

import ru.bernarder.fallenrisefromdust.Utils;
import ru.bernarder.fallenrisefromdust.entities.inventory.Inventory;
import ru.bernarder.fallenrisefromdust.entities.inventory.Item;

public class Loot {
	int nodes = 0;
	public Vector<Item> contents = new Vector<Item>();

	public Loot(Item cont) {
		add(cont);
	}

	public Loot() {
		this(new Item());
	}

	public String getContents() {
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

	public static int addLootQ(String temp, Inventory inventory, int x, int y, Map map) { // Don't look here
		int i = 0;
		if (temp.equals("n"))
			return -1;
		if (temp.indexOf(":") == -1)
			temp += ":";
		Utils.out("Loot Cell contents: " + temp);
		try {
			String[] block = temp.split(":");
			String[] attr = new String[0];
			int id, q;

			while (i < block.length) {
				attr = block[i].split("/");
				id = Integer.parseInt(attr[0]);
				q = Integer.parseInt(attr[1]);
				if (inventory != null)
					inventory.putItem(id, q);
				else
					map.putLoot(x, y, id, q);
				i++;
			}
			
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	public static int addLootQ(String query, int x, int y, Map map) { // and this thing puts items to map by their query
		return addLootQ(query, null, x, y, map);
	}
}