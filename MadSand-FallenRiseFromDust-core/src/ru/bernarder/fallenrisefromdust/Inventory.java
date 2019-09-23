package ru.bernarder.fallenrisefromdust;

import java.util.HashMap;
import java.util.Vector;

public class Inventory {
	Vector<Item> items = new Vector<Item>();
	HashMap<Item, InventoryUICell> itemUI = new HashMap<Item, InventoryUICell>();
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

	private void refreshRemoveItem(Item item) {
		if (itemUI.containsKey(item)) {
			itemUI.get(item).cell.remove();
			itemUI.remove(item);
			Gui.invTable.pack();
		}
	}

	private void refreshItem(Item item) {
		if (itemUI.containsKey(item))
			itemUI.get(item).setText(item.quantity + "");
		else {
			InventoryUICell cell = new InventoryUICell(item);
			itemUI.put(item, cell);
			Gui.invTable.add(cell.cell);
		}
	}

	boolean putItem(int id, int quantity, boolean silent) {
		Item item = new Item(id, quantity);
		Item updItem;
		double newWeight = item.getWeight() + curWeight;
		int existingIdx = getSameCell(id);
		if (newWeight <= maxWeight) {
			if (existingIdx != -1) {
				items.get(existingIdx).quantity += quantity;
				updItem = items.get(existingIdx);
			} else {
				items.add(item);
				updItem = item;
			}
			refreshItem(updItem);
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
			Item item = new Item(id, quantity);
			curWeight -= item.getWeight();
			if ((items.get(idx).quantity - quantity) <= 0) {
				items.remove(idx);
				refreshRemoveItem(item);
			} else {
				items.get(idx).quantity -= quantity;
				refreshItem(items.get(idx));
			}
			dump();
			return true;
		}
	}
}
