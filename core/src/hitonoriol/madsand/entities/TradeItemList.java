package hitonoriol.madsand.entities;

import java.util.ArrayList;
import java.util.Random;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.inventory.item.Item;

/*
 * List that contains ids of items for trader npcs
 * 
 * Used to roll items for trader inventories according to <trader tier>
 * ( and their <TradeCategory> -- in TradeListContainer ) 
 */

public class TradeItemList {

	public static int DEFAULT_MIN_ROLLS = 3;
	public static int DEFAULT_MAX_ROLLS = 10;

	public int tier = 0;
	public int minRolls, maxRolls;
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

	public int getRandomId(Random random) {
		return items.get(random.nextInt(items.size()));
	}

	public int getRandomId() {
		return getRandomId(Utils.random);
	}

	private void rollItemQuantity(Item item) {

		if (item.type.isUnique())
			item.quantity = 1;
		else
			item.quantity = Utils.rand(BASE_ITEM_QUANTITY) + tier * TIER_FACTOR;

	}

	public Item rollItem() {
		Item item = Item.create(getRandomId());
		rollItemQuantity(item);

		return item;
	}

	private static int TIER_MULTIPLIER = 3;

	public ArrayList<Item> roll(ArrayList<Item> items) {
		if (maxRolls < 1) {
			minRolls = DEFAULT_MIN_ROLLS;
			maxRolls = DEFAULT_MAX_ROLLS;
		}

		int rolls = rollTimes();

		for (int i = 0; i < rolls; ++i)
			items.add(rollItem());

		return items;
	}

	public int rollTimes() {
		return Utils.rand(minRolls + tier * (TIER_MULTIPLIER / 2), maxRolls + tier * TIER_MULTIPLIER);
	}

	public TradeItemList add(int... itemList) {

		for (int item : itemList)
			items.add(item);

		return this;
	}

}
