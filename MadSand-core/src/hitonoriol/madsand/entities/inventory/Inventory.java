package hitonoriol.madsand.entities.inventory;

import java.util.HashMap;
import java.util.Vector;
import java.util.Map.Entry;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.enums.Skill;
import hitonoriol.madsand.map.Loot;

public class Inventory {
	public static final double MAX_WEIGHT = Integer.MAX_VALUE;

	public Vector<Item> items = new Vector<Item>();

	public float curWeight, maxWeight;

	private HashMap<Item, InventoryUICell> itemUI;

	@JsonIgnore
	public InventoryUI inventoryUI;

	public Inventory(float maxWeight) {
		setMaxWeight(maxWeight);
	}

	public Inventory() {
		curWeight = 0;
		maxWeight = 0;
	}

	public void initUI() {
		inventoryUI = new InventoryUI();
		itemUI = new HashMap<Item, InventoryUICell>();
	}

	public void setMaxWeight(float val) {
		maxWeight = val;
		refreshUITitle();
	}

	public void refreshUITitle() {
		if (itemUI != null)
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
		refreshUITitle();
	}

	public void dump() {
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
		int pos = 0;
		for (Item item : items) {
			if (item.id == id)
				return pos;
			++pos;
		}
		return -1;
	}

	public int getSameCell(int id, int q) { // find q or more items
		int i = getSameCell(id);
		if (i != -1) {
			if (items.get(i).quantity < q)
				return -1;

		}
		return i;
	}

	public Item getItem(int id) { // get item by its id, not the index
		Item ret = Item.nullItem;
		int pos = getSameCell(id);

		if (pos != -1)
			ret = items.get(pos);

		return ret;
	}

	void clear() {
		items.clear();
		curWeight = 0;
	}

	private void refreshRemoveItem(Item item) {
		if (itemUI == null)
			return;
		if (item.id == 0)
			return;
		if (itemUI.containsKey(item)) {
			inventoryUI.setMass(curWeight, maxWeight);
			Group rcell = itemUI.get(item);
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
		if (itemUI == null)
			return;
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
			inventoryUI.putNewItem(cell);
			inventoryUI.refresh(itemUI.entrySet());
		}
	}

	public void clearContextMenus() {
		if (itemUI == null)
			return;
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

	/*
	 * This method discards "overflowing" items,
	 * use <Entity>.addItem(...) for extra items to drop
	 */
	public boolean putItem(Item item) {
		Item updItem;

		float newWeight = item.getWeight() + curWeight;
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
			curWeight = newWeight;
			refreshItem(updItem);

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
			return true;
		}
	}

	public boolean delItem(String query) {
		if (query.equals(Item.EMPTY_ITEM_STRING))
			return false;

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

	public boolean delItem(Item item, int quantity) {
		if (quantity > item.quantity)
			return false;

		for (int i = items.size() - 1; i >= 0; --i) {

			if (items.get(i).equals(item)) {
				item.quantity -= quantity;

				if (item.quantity < 1) {
					refreshRemoveItem(item);
					items.remove(i);
				} else
					refreshItem(item);

				return true;
			}
		}

		return false;
	}

	public boolean delItem(Item item) {
		return delItem(item, item.quantity);
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

		if (query.equals(Item.EMPTY_ITEM_STRING))
			return -1;

		return Loot.addLootQ(query, this, 0, 0, null); // Don't ask about this, this is a long story
	}
}
