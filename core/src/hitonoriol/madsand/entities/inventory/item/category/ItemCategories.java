package hitonoriol.madsand.entities.inventory.item.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.util.Utils;

/*
 * Container for TradeItemList 
 * where for each TradeCategory there can be multiple TradeItemLists 
 */

public class ItemCategories extends HashMap<ItemCategory, ArrayList<ItemCategoryList>> {
	public static final int MAX_TIER = 10, MAX_ITEMS = 350;

	public ItemCategories() {}

	public List<Item> roll(ItemCategory category, int tier) {
		Utils.dbg("Rolling tradeList " + category + " tier: " + tier);
		List<Item> items = new ArrayList<>();
		final int rolledTier = tier;
		boolean all = category.equals(ItemCategory.All);
		ItemCategoryList list;

		if (category.isFlag())
			category = ItemCategory.random();

		list = getTradeItemList(category, tier);

		for (int i = tier; i >= 0; i--) {
			if (!all)
				getTradeItemList(category, tier).roll(items);
			else {
				for (int j = 0; j < list.rollTimes(); ++j) {
					list = getTradeItemList(category, tier);
					tier = rerollTier(tier);
					category = ItemCategory.random();
					addItem(items, list.rollItem());
				}
			}
			tier = rerollTier(tier);
		}
		Pair range = new Pair();
		capItemQuantity(items, item -> {
			range.x = (int) ((tierDistribution(MAX_TIER - rolledTier) / 100d) * MAX_ITEMS) / 10;
			range.y = Utils.rand(MAX_ITEMS / 20, MAX_ITEMS);
			/*Utils.dbg("Rerolling quantity [%s]", range);*/
			return range;
		});
		return items;
	}

	public List<Item> roll(ItemCategory category) {
		return roll(category, rollTier());
	}

	public static List<Item> capItemQuantity(List<Item> items, Function<Item, Pair> rangeSupplier) {
		items.stream()
				.filter(item -> {
					int max = rangeSupplier.apply(item).y;
					boolean overflow = item.quantity > max;
					if (overflow)
						item.quantity = max;
					return overflow;
				})
				.forEach(item -> {
					Pair range = rangeSupplier.apply(item);
					item.setQuantity(Utils.rand(Math.min(1, range.x), range.y));
				});
		return items;
	}

	static void addItem(List<Item> items, Item item) {
		int idx = items.indexOf(item);
		if (idx != -1)
			items.get(idx).quantity += item.quantity;
		else
			items.add(item);
	}

	@JsonIgnore
	public ItemCategoryList getTradeItemList(ItemCategory category, int tier) {
		List<ItemCategoryList> itemLists = super.get(category);

		for (ItemCategoryList list : itemLists)
			if (list.tier == tier)
				return list;

		return itemLists.get(0);
	}

	@JsonIgnore
	public ItemCategoryList getTradeItemList(ItemCategory category) {
		return getTradeItemList(category, 0);
	}

	public void put(ItemCategory category, ItemCategoryList itemList) {
		ArrayList<ItemCategoryList> valList = null;

		if (super.containsKey(category))
			valList = super.get(category);

		if (valList == null)
			valList = new ArrayList<>();

		valList.add(itemList);

		super.remove(category);
		super.put(category, valList);
	}

	public int rollId(ItemCategory category, int tier) {
		return roll(category, tier < 0 ? NpcProp.tradeLists.rollTier() : tier).get(0).id();
	}

	public int rollId(ItemCategory category) {
		return rollId(category, 0);
	}

	public static int rollItemQuantity(int tier) {
		return Utils.rand(1, (int) tierDistribution(tier));
	}

	/* Tier 0 has 100% probability, the higher the tier the lower this value gets */
	private static double tierDistribution(double tier) {
		return 0.03967804978399232 * Math.pow(tier, 2) - 10.171235342522081 * tier + 99.26496605636694;
	}

	public int rerollTier(int tier) {
		/* Higher tiers have higher chances of rerolling */
		return (Utils.percentRoll(100 - tierDistribution(tier)) ? rollTier() : tier);
	}

	public int rollTier() {
		double tierChance;
		for (float tier = MAX_TIER; tier >= 0; --tier) {
			tierChance = tierDistribution(tier);
			if (Utils.percentRoll(tierChance))
				return (int) tier;
		}
		return 0;
	}
}
