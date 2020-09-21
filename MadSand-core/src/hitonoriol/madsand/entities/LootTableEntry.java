package hitonoriol.madsand.entities;

import java.util.ArrayList;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.inventory.Item;

public class LootTableEntry {
	public boolean allowEmptyRoll = false;
	public ArrayList<LootItemEntry> items = new ArrayList<>();

	public Item rollItem(boolean allowEmptyRoll) {
		int minQuantity = allowEmptyRoll ? 0 : 1;
		LootItemEntry itemPair = items.get(Utils.rand(items.size())); // x -- item id | y -- item quantity

		itemPair.maxQuantity = Utils.rand(minQuantity, itemPair.maxQuantity);

		return new Item(itemPair.id, itemPair.maxQuantity);
	}

	public Item rollItem() {
		return rollItem(allowEmptyRoll);
	}
}
