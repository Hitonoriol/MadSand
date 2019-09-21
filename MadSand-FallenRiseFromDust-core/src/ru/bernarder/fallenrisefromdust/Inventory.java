package ru.bernarder.fallenrisefromdust;

import java.util.Vector;

public class Inventory {
	Vector<Item> items = new Vector<Item>();
	double curWeight, maxWeight;

	public Inventory(float maxWeight) {
		this.maxWeight = maxWeight;
	}

	void dump() {
		Utils.out("Inventory dump: ");
		Utils.out("Weight: " + curWeight + " / " + maxWeight);
		for (Item item : items) {
			Utils.out(item.quantity + " " + item.name);
		}
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
		Item item = new Item(id, quantity);
		double newWeight = item.getWeight() + curWeight;
		int existingIdx = getSameCell(id);
		if (newWeight <= maxWeight) {
			if (existingIdx != -1)
				items.get(existingIdx).quantity += quantity;
			else
				items.add(item);
			curWeight = newWeight;
			if (!silent)
				MadSand.print("You got " + Item.queryToName(id + "/" + quantity));
			Utils.out("ITEM PUT " + id + " " + quantity);
			dump();
			return true;
		}
		return false;
	}

	boolean putItem(int id, int q) {
		return putItem(id, q, false);
	}

	public boolean delItem(int id, int quantity) {
		int idx = getSameCell(id, quantity);
		if (idx == -1)
			return false;
		else {
			curWeight -= new Item(id, quantity).getWeight();
			if ((items.get(idx).quantity - quantity) <= 0)
				items.remove(idx);
			else
				items.get(idx).quantity -= quantity;
			dump();
			return true;
		}
	}
}
