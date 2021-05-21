package hitonoriol.madsand.entities.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.entities.inventory.item.AbstractEquipment;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.util.Utils;

public class Inventory {
	public static final double MAX_WEIGHT = Integer.MAX_VALUE;

	public ArrayList<Item> items = new ArrayList<Item>();

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
			inventoryUI.setHeader(getWeightString());
	}

	@JsonIgnore
	public String getWeightString() {
		return "[" + Utils.round(curWeight) + "/" + maxWeight + "] kg";
	}

	@JsonIgnore
	public float getTotalWeight() {
		return curWeight;
	}

	public void refreshContents() {
		Item item;
		for (int i = 0; i < items.size(); ++i) {
			item = items.get(i);
			item.reinit();

			if (item.id == 0)
				refreshRemoveItem(item);
			else
				refreshItem(item);
		}
		refreshWeight();
	}

	public void refreshWeight() {
		curWeight = 0;
		for (Item item : items)
			curWeight += item.getWeight();
		refreshUITitle();
	}

	public void dump() {
		Utils.dbg("Inventory dump: ");
		Utils.dbg("Weight: " + curWeight + " / " + maxWeight);
		items.forEach(item -> Utils.dbg(item.toString()));
	}

	public int getIndex(Item item) {
		return items.indexOf(item);
	}

	public boolean itemExists(Item item) {
		return items.contains(item);
	}

	public <T extends Item> Optional<T> getItem(Class<T> itemType) {
		return items.stream()
				.filter(item -> item.is(itemType))
				.findFirst()
				.map(item -> item.as(itemType))
				.orElse(Optional.empty());
	}

	public <T extends Item> boolean hasItem(Class<T> itemType) {
		return getItem(itemType).isPresent();
	}

	public boolean hasItem(int id, int quantity) {
		return getSameCell(id, quantity) != -1;
	}

	public boolean hasItem(int id) {
		return hasItem(id, 1);
	}

	public boolean containsAll(List<Item> items) {
		for (Item item : items) {
			Item invItem = getItem(item.id);
			if (invItem == Item.nullItem || invItem.quantity < item.quantity)
				return false;
		}
		return true;
	}

	public boolean containsNone(List<Item> items) {
		for (Item item : items)
			if (hasItem(item.id))
				return false;
		return true;
	}

	public int getIndex(int id) {
		int pos = 0;
		for (Item item : items) {
			if (item.id == id)
				return pos;
			++pos;
		}
		return -1;
	}

	public int getSameCell(int id, int q) { // find q or more items
		int i = getIndex(id);
		if (i != -1)
			if (items.get(i).quantity < q)
				return -1;
		return i;
	}

	public Item getItem(Item referenceItem) {
		int itemIdx = getIndex(referenceItem);
		if (itemIdx == -1)
			return Item.nullItem;

		return items.get(itemIdx);
	}

	public Item getItem(int id) { // get item by its id, not the index
		Item ret = Item.nullItem;
		int pos = getIndex(id);

		if (pos != -1)
			ret = items.get(pos);

		return ret;
	}

	public Item getItemByIndex(int idx) {
		if (idx < 0 || idx >= items.size())
			return Item.nullItem;

		return items.get(idx);
	}

	public void clear() {
		items.clear();
		curWeight = 0;
	}

	private void refreshRemoveItem(Item item) {
		if (itemUI == null)
			return;
		if (item.id == 0)
			return;
		if (itemUI.containsKey(item)) {
			refreshUITitle();
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
		}
	}

	public void refreshItem(Item item) {
		if (itemUI == null)
			return;
		if (item.id == 0)
			return;
		refreshUITitle();
		if (itemUI.containsKey(item)) {
			itemUI.get(item).setText(item.quantity + "");
			if (item instanceof AbstractEquipment)
				itemUI.get(item).refreshHp();
			itemUI.get(item).refreshEquippedStatus();
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
			if (cell.contextActive())
				cell.hideContext();
		}
	}

	public boolean damageEquipment(AbstractEquipment item, int dmg) {
		if (!item.damage(dmg)) {
			refreshItem(item);
			return false;
		} else {
			refreshRemoveItem(item);
			return true;
		}
	}

	/*
	 * This method discards "overflowing" items,
	 * use <Entity>.addItem(...) for extra items to drop
	 */
	public boolean putItem(Item item) {
		if (item.equals(Item.nullItem))
			return false;

		if (item.quantity < 1)
			return true;

		Item updItem;
		float newWeight = item.getWeight() + curWeight;
		int existingIdx = getIndex(item);

		if (!item.equals(getItemByIndex(existingIdx)))
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

	public boolean putItem(List<Item> items) {
		for (Item item : items)
			putItem(item);
		return true;
	}

	public boolean putItem(int id) {
		return putItem(id, 1);
	}

	public boolean putItem(int id, int quantity) {
		if (id == Item.NULL_ITEM)
			return true;

		return putItem(Item.create(id, quantity));
	}

	public boolean putItem(String query) {
		return putItem(Item.parseItemString(query));
	}

	public boolean delItem(int id, int quantity) {
		if (!hasItem(id))
			return false;

		return delItem(getItem(id), quantity);
	}

	public boolean delItem(String query) {
		return delItem(Item.parseItemString(query));
	}

	public boolean delItem(List<Item> items) {
		for (Item item : items)
			delItem(item, item.quantity);
		return true;
	}

	public boolean delItem(Item item, int quantity) {
		if (quantity > item.quantity)
			return false;

		Item foundItem;
		for (int i = items.size() - 1; i >= 0; --i) {
			foundItem = items.get(i);
			if (foundItem.equals(item)) {
				curWeight -= foundItem.weight * (float) quantity;
				foundItem.quantity -= quantity;

				if (foundItem.quantity < 1) {
					refreshRemoveItem(foundItem);
					items.remove(i);
				} else
					refreshItem(foundItem);

				refreshUITitle();
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

	public boolean isEmpty() {
		return items.isEmpty();
	}

	public boolean itemsExist(String sequence) {
		MutableBoolean itemsExist = new MutableBoolean(true);
		Item.parseListString(sequence, (id, quantity) -> {
			if (!itemsExist.booleanValue())
				return;

			if (!hasItem(id, quantity))
				itemsExist.setFalse();
		});
		return itemsExist.booleanValue();
	}
}
