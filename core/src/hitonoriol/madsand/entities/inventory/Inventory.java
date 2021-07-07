package hitonoriol.madsand.entities.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.entities.inventory.item.AbstractEquipment;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.util.Utils;

public class Inventory {
	public static final double MAX_WEIGHT = Integer.MAX_VALUE;

	public ArrayList<Item> items = new ArrayList<Item>();

	public float curWeight, maxWeight;

	@JsonIgnore
	private InventoryUI inventoryUI;

	public Inventory(float maxWeight) {
		setMaxWeight(maxWeight);
	}

	public Inventory() {
		curWeight = 0;
		maxWeight = 0;
	}

	public void initUI() {
		inventoryUI = new InventoryUI(this);
	}

	@JsonIgnore
	public InventoryUI getUI() {
		return inventoryUI;
	}

	public boolean hasUI() {
		return inventoryUI != null;
	}

	private void updateUI(Consumer<InventoryUI> action) {
		if (hasUI())
			action.accept(inventoryUI);
	}

	public void setMaxWeight(float val) {
		maxWeight = val;
		refreshUITitle();
	}

	public void refreshUITitle() {
		updateUI(ui -> ui.refreshTitle());
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
		for (int i = 0; i < items.size(); ++i) {
			Item item = items.get(i);
			item.reinit();

			updateUI(ui -> {
				if (item.id == 0)
					ui.refreshRemoveItem(item);
				else
					ui.refreshItem(item);
			});
		}
		refreshWeight();
	}

	public void refreshWeight() {
		curWeight = 0;
		for (Item item : items)
			curWeight += item.getTotalWeight();
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

	public boolean damageEquipment(AbstractEquipment item, int dmg) {
		if (!item.damage(dmg)) {
			updateUI(ui -> ui.refreshItem(item));
			return false;
		} else {
			updateUI(ui -> ui.refreshRemoveItem(item));
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
		float newWeight = item.getTotalWeight() + curWeight;
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
			updateUI(ui -> ui.refreshItem(updItem));
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

		Item item = Item.create(id, quantity);
		if (item.isEquipment()) {
			item.setQuantity(1);
			putItem(id, quantity - 1);
		}

		return putItem(item);
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

		for (int i = items.size() - 1; i >= 0; --i) {
			Item foundItem = items.get(i);
			if (foundItem.equals(item)) {
				curWeight -= foundItem.weight * (float) quantity;
				foundItem.quantity -= quantity;

				if (foundItem.quantity < 1) {
					updateUI(ui -> ui.refreshRemoveItem(foundItem));
					items.remove(i);
				} else
					updateUI(ui -> ui.refreshItem(foundItem));

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
