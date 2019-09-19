package ru.bernarder.fallenrisefromdust;

import java.util.Vector;

public class Inventory {
	Vector<Item> items = new Vector<Item>();
	Item item;
	float curWeight, maxWeight;

	public Inventory(float maxWeight) {
		this.maxWeight = maxWeight;
	}

	int getSameCell(int id) {
		return items.indexOf(new Item(id));
	}

	int getSameCell(int id, int q) { // find q or more items
		int i = getSameCell(id);
		if (i != -1) {
			if (items.get(i).quantity < q)
				return -1;
		}
		return i;
	}

	void clear() {
		items.clear();
		curWeight = 0;
	}

	boolean putItem(int id, int quantity, boolean silent) {
		Utils.out("ITEM PUT " + quantity + " " + id);
		double newWeight = item.reinit(id, quantity).getMass() + curWeight;
		if (getSameCell(id) != -1 && newWeight <= maxWeight) {
			items.add(item);
			if (!silent)
				MadSand.print("You got " + Item.queryToName(":" + id + "/" + quantity));
			return true;
		}
		return false;
	}

	public boolean delItem(int id, int quantity) {
		boolean r = false;
		// TODO
		return r;
	}
}
