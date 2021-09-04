package hitonoriol.madsand.entities.inventory.item.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.util.Utils;

/*
 * List that contains ids of items for trader npcs
 * 
 * Used to roll items for trader inventories according to <trader tier>
 * ( and their <TradeCategory> -- in TradeListContainer ) 
 */

public class ItemCategoryList {
	public static int MIN_ROLLS = 10, MAX_ROLLS = 15;

	public int tier = 0;
	public int minRolls, maxRolls;
	public ArrayList<Integer> items;

	public static int BASE_TOOL_QUANTITY = 2;
	public static int BASE_ITEM_QUANTITY = 50;
	public static int TIER_FACTOR = 5;

	public ItemCategoryList(int tier) {
		items = new ArrayList<>();
		this.tier = tier;
		minRolls = MIN_ROLLS - tier;
		maxRolls = MAX_ROLLS - tier;
	}

	public ItemCategoryList() {
		this(0);
	}

	public int getRandomId(Random random) {
		return items.get(random.nextInt(items.size()));
	}

	public int getRandomId() {
		return getRandomId(Utils.random);
	}

	private void rollItemQuantity(Item item) {
		item.setQuantity(ItemCategories.rollItemQuantity(tier));
	}

	public Item rollItem() {
		Item item = Item.create(getRandomId());
		rollItemQuantity(item);

		return item;
	}

	public List<Item> roll(List<Item> items) {
		int rolls = rollTimes();

		for (int i = 0; i < rolls; ++i)
			ItemCategories.addItem(items, rollItem());

		return items;
	}

	public int rollTimes() {
		return Utils.rand(minRolls, maxRolls);
	}

	public ItemCategoryList add(int... items) {
		for (int item : items)
			this.items.add(item);

		return this;
	}

}
