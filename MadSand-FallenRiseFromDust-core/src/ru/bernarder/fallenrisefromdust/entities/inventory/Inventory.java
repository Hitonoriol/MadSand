package ru.bernarder.fallenrisefromdust.entities.inventory;

import java.util.HashMap;
import java.util.Vector;
import java.util.Map.Entry;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.fasterxml.jackson.annotation.JsonIgnore;

import ru.bernarder.fallenrisefromdust.Utils;
import ru.bernarder.fallenrisefromdust.enums.Skill;
import ru.bernarder.fallenrisefromdust.map.Loot;

public class Inventory {
	public Vector<Item> items = new Vector<Item>();

	public double curWeight, maxWeight;

	private HashMap<Item, InventoryUICell> itemUI = new HashMap<Item, InventoryUICell>();

	@JsonIgnore
	public InventoryUI inventoryUI = new InventoryUI();

	public Inventory(float maxWeight) {
		setMaxWeight(maxWeight);
	}

	public Inventory() {
		curWeight = 0;
		maxWeight = 0;
	}

	public void setMaxWeight(float val) {
		maxWeight = val;
		inventoryUI.setMass(curWeight, maxWeight);
	}

	public void refreshContents() {
		Item item;
		int shp = -1;
		for (int i = 0; i < items.size(); ++i) {
			item = items.get(i);
			if (item.hp != -1)
				shp = item.hp;

			item.reinit();

			item.hp = shp;

			if (item.id == 0)
				refreshRemoveItem(item);
			else
				refreshItem(item);
		}
		refreshWeight();
	}

	void refreshWeight() {
		curWeight = 0;
		for (Item item : items) {
			curWeight += item.getWeight();
		}
	}

	void dump() {
		Utils.out("Inventory dump: ");
		Utils.out("Weight: " + curWeight + " / " + maxWeight);
		for (Item item : items) {
			Utils.out(item.quantity + " " + item.name);
		}
	}

	public int getSameCell(Item item) {
		return items.indexOf(item);
	}

	public int getSameCell(int id) {
		return items.indexOf(new Item(id));
	}

	public int getSameCell(int id, int q) { // find q or more items
		int i = getSameCell(id);
		if (i != -1) {
			if (items.get(i).quantity < q) {
				Utils.out("getSameCell " + id + " " + q + " not found");
				return -1;
			}
		}
		return i;
	}

	Item getItem(int cid) {
		return items.get(cid);
	}

	void clear() {
		items.clear();
		curWeight = 0;
	}

	private void refreshRemoveItem(Item item) {
		if (item.id == 0)
			return;
		if (itemUI.containsKey(item)) {
			inventoryUI.setMass(curWeight, maxWeight);
			Group rcell = itemUI.get(item).cell;
			Cell<Group> cell = inventoryUI.invTable.getCell(rcell);
			clearContextMenus();
			rcell.remove();
			// remove cell from table
			inventoryUI.invTable.getCells().removeValue(cell, true);
			inventoryUI.invTable.invalidate();
			// itemUI.get(item).cell.remove();
			itemUI.remove(item);
			inventoryUI.stacks -= 1;

			inventoryUI.refresh(itemUI.entrySet());
			Utils.out("Removed item stacks: " + inventoryUI.stacks);
		}
	}

	private void refreshItem(Item item) {
		if (item.id == 0)
			return;
		inventoryUI.setMass(curWeight, maxWeight);
		if (itemUI.containsKey(item)) {
			itemUI.get(item).setText(item.quantity + "");
			if (item.type.isTool())
				itemUI.get(item).setHp(item.hp);
		} else {
			InventoryUICell cell = new InventoryUICell(item);
			itemUI.put(item, cell);
			inventoryUI.putNewItem(cell.cell);
			inventoryUI.refresh(itemUI.entrySet());
		}
	}

	public void clearContextMenus() {
		for (Entry<Item, InventoryUICell> pair : itemUI.entrySet()) {
			InventoryUICell cell = pair.getValue();
			cell.hideContext();
		}
	}

	public boolean damageTool(Item item, Skill skill) {
		if (!item.damageTool(skill)) {
			refreshItem(item);
			return false;
		} else {
			refreshRemoveItem(item);
			return true;
		}
	}

	void damageTool(Item item) {
		damageTool(item, Skill.None);
	}

	public boolean putItem(Item item) { // don't use this directly, use Entity's addItem method
		Item updItem;
		double newWeight = item.getWeight() + curWeight;
		int existingIdx = getSameCell(item.id);
		if (item.type.isTool())
			existingIdx = -1;
		if (newWeight <= maxWeight) {
			if (existingIdx != -1) {
				items.get(existingIdx).quantity += item.quantity;
				updItem = items.get(existingIdx);
			} else {
				items.add(item);
				updItem = item;
			}
			refreshItem(updItem);
			curWeight = newWeight;

			return true;
		}
		return false;
	}

	public boolean putItem(int id) {
		return putItem(id, 1);
	}

	public boolean putItem(int id, int quantity) {
		Item item = new Item(id, quantity);
		return putItem(item);
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

	public boolean delItem(String query) {
		int i = 0;
		if (query.indexOf(":") == -1)
			query += ":";
		try {
			String[] block = query.split(":");
			String[] attr;
			while (i < block.length) {
				attr = block[i].split("/");
				if (!delItem(Integer.parseInt(attr[0]), Integer.parseInt(attr[1])))
					return false;
				i++;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean delItem(Item item) {
		for (int i = items.size() - 1; i >= 0; --i) {
			if (items.get(i).equals(item)) {
				refreshRemoveItem(item);
				items.remove(i);
				return true;
			}
		}
		return false;
	}

	public boolean delItem(int id) {
		return delItem(id, 1);
	}

	public boolean itemsExist(String sequence) {
		int i = 0;
		if (sequence.indexOf(":") == -1)
			sequence += ":";
		String[] block = sequence.split(":");
		String[] attr = new String[0];
		int id = 0;
		int q = 0;
		while (i < block.length) {
			attr = block[i].split("/");
			id = Integer.parseInt(attr[0]);
			q = Integer.parseInt(attr[1]);
			if (getSameCell(id, q) == -1)
				return false;
			i++;
		}
		return true;
	}

	public int putItem(String query) {
		return Loot.addLootQ(query, this, 0, 0, null); // Don't ask about this, this is a long story
	}
}
