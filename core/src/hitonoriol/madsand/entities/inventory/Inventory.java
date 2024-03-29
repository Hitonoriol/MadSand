package hitonoriol.madsand.entities.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.entities.inventory.item.AbstractEquipment;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.util.Utils;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Inventory {
	public static final double MAX_WEIGHT = Integer.MAX_VALUE;

	private List<Item> items = new ArrayList<>();
	private float curWeight, maxWeight;

	public Inventory(float maxWeight) {
		setMaxWeight(maxWeight);
	}

	public Inventory() {
		curWeight = 0;
		maxWeight = 0;
	}

	public void setMaxWeight(float val) {
		maxWeight = val;
	}

	@JsonIgnore
	public String getWeightString() {
		return "[" + Utils.round(curWeight) + "/" + maxWeight + "] kg";
	}

	@JsonIgnore
	public float getTotalWeight() {
		return curWeight;
	}

	public float getMaxWeight() {
		return maxWeight;
	}

	public void refreshContents() {
		for (Item item : items) {
			item.reinit();
		}
		refreshWeight();
	}

	public void refreshWeight() {
		curWeight = 0;
		for (Item item : items)
			curWeight += item.getTotalWeight();
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
			var invItem = getItem(item.id());
			if (invItem == Item.nullItem || invItem.quantity < item.quantity)
				return false;
		}
		return true;
	}

	public boolean containsNone(List<Item> items) {
		for (Item item : items)
			if (hasItem(item.id()))
				return false;
		return true;
	}

	/* In case of unique items (same id but different hashes) this will count all of them */
	public int countItems(int id) {
		return items.stream()
			.filter(item -> item.equals(id))
			.mapToInt(item -> item.quantity)
			.sum();
	}

	public int getIndex(int id) {
		int pos = 0;
		for (Item item : items) {
			if (item.id() == id)
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
		var ret = Item.nullItem;
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
		return item.damage(dmg);
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

		float newWeight = item.getTotalWeight() + curWeight;
		int existingIdx = getIndex(item);

		if (!item.equals(getItemByIndex(existingIdx)))
			existingIdx = -1;

		if (newWeight <= maxWeight) {
			if (existingIdx != -1)
				items.get(existingIdx).quantity += item.quantity;
			else
				items.add(item);
			curWeight = newWeight;
			return true;
		}

		return false;
	}

	public boolean putItem(List<Item> items) {
		for (Item item : items)
			putItem(item);
		return true;
	}

	public Item putItem(int id) {
		return putItem(id, 1);
	}

	/*
	 * Returns empty Item on success, or newly created Item with specified id & quantity otherwise
	 */
	public Item putItem(int id, int quantity) {
		if (id == Item.NULL_ITEM)
			return Item.nullItem;

		var item = Item.create(id, quantity);
		if (item.isEquipment() && quantity > 1)
			putItem(id, quantity - 1);

		return putItem(item) ? Item.nullItem : item;
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
			var foundItem = items.get(i);
			if (foundItem.equals(item)) {
				curWeight -= foundItem.weight * quantity;
				foundItem.quantity -= quantity;
				if (foundItem.quantity < 1)
					items.remove(i);
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

	public List<Item> getItems() {
		return items;
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

	public boolean itemsExist(String sequence) {
		var itemsExist = new MutableBoolean(true);
		Item.parseListString(sequence, (id, quantity) -> {
			if (!itemsExist.booleanValue())
				return;

			if (!hasItem(id, quantity))
				itemsExist.setFalse();
		});
		return itemsExist.booleanValue();
	}

	@Override
	public String toString() {
		return String.format(
			"Inventory: {%s}",
			items.stream()
				.map(Item::toString)
				.collect(Collectors.joining(", "))
		);
	}
}
