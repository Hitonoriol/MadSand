package hitonoriol.madsand.entities;

import java.util.ArrayList;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.inventory.Item;

/*
 * List that contains ids of items for trader npcs
 * 
 * Used to roll items for trader inventories according to <trader tier>
 * ( and their <TradeCategory> -- in TradeListContainer ) 
 */

public class TradeItemList {

	public int tier = 0;
	public ArrayList<Integer> items;

	public static int BASE_TOOL_QUANTITY = 2;
	public static int BASE_ITEM_QUANTITY = 50;
	public static int TIER_FACTOR = 5;

	public TradeItemList(int tier) {
		items = new ArrayList<>();
		this.tier = tier;
	}

	public TradeItemList() {
		this(0);
	}

	private int getRandomId() {
		return items.get(Utils.random.nextInt(items.size()));
	}

	private void rollItemQuantity(Item item) {

		if (item.type.isUnique())
			item.quantity = Utils.rand(BASE_TOOL_QUANTITY) + tier;
		else
			item.quantity = Utils.rand(BASE_ITEM_QUANTITY) + tier * TIER_FACTOR;

	}

	public Item rollItem() {
		Item item = new Item(getRandomId());
		rollItemQuantity(item);

		return item;
	}

	public TradeItemList add(int... itemList) {

		for (int item : itemList)
			items.add(item);

		return this;
	}

}
