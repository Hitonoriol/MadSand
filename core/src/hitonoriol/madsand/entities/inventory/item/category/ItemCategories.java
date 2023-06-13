package hitonoriol.madsand.entities.inventory.item.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.util.Utils;

/*
 * Container for TradeItemList
 * where for each TradeCategory there can be multiple TradeItemLists
 */

public class ItemCategories {
	public static final int MAX_TIER = 10, MAX_ITEMS = 350;
	private Map<ItemCategory, List<ItemCategoryList>> categories = new HashMap<>();

	private static final ItemCategories instance = new ItemCategories();

	private ItemCategories() {}

	public void addItem(int id, ItemCategory category, int tier) {
		Utils.dbg("ItemCategory [id=%d] ==> %s T%d", id, category, tier);
		getItemList(category, tier, true).add(id);
	}

	public List<Item> roll(ItemCategory category, int tier) {
		Utils.dbg("Rolling from itemlist " + category + " T" + tier);
		List<Item> items = new ArrayList<>();
		final int rolledTier = tier;
		boolean all = category.equals(ItemCategory.All);
		ItemCategoryList list;

		if (category.isFlag())
			category = ItemCategory.random();

		list = getItemList(category, tier);

		for (int i = tier; i >= 0; i--) {
			if (!all)
				getItemList(category, tier).roll(items);
			else {
				for (int j = 0; j < list.rollTimes(); ++j) {
					list = getItemList(category, tier);
					tier = rerollTier(tier);
					category = ItemCategory.random();
					addItem(items, list.rollItem());
				}
			}
			tier = rerollTier(tier);
		}
		var range = new Pair();
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
				var range = rangeSupplier.apply(item);
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

	private List<ItemCategoryList> getCategoryList(ItemCategory category) {
		var valList = categories.get(category);
		if (valList == null) {
			valList = new ArrayList<>();
			categories.put(category, valList);
		}
		return valList;
	}

	public ItemCategoryList getItemList(ItemCategory category, int tier, boolean createIfNull) {
		var itemLists = getCategoryList(category);
		tier = clampTier(tier);

		for (ItemCategoryList list : itemLists)
			if (list.tier == tier)
				return list;

		var list = createIfNull ? new ItemCategoryList(tier) : itemLists.get(0);
		if (createIfNull)
			itemLists.add(list);

		return list;
	}

	public ItemCategoryList getItemList(ItemCategory category, int tier) {
		return getItemList(category, tier, false);
	}

	public ItemCategoryList getItemList(ItemCategory category) {
		return getItemList(category, 0);
	}

	public int rollId(ItemCategory category, int tier) {
		return roll(category, tier < 0 ? ItemCategories.rollTier() : tier).get(0).id();
	}

	public int rollId(ItemCategory category) {
		return rollId(category, 0);
	}

	public static int rollItemQuantity(int tier) {
		return Utils.rand(1, (int) tierDistribution(tier));
	}

	/* Tier 0 has 100% probability, the higher the tier the lower this value gets */
	private static double tierDistribution(double tier) {
		return (0.03967804978399232 * Math.pow(tier, 2) - 10.171235342522081 * tier + 99.26496605636694) * 0.225;
	}

	static {
		Utils.out("Item tier distribution [T0 if all rolls failed]:");
		for (int i = 1; i <= MAX_TIER; ++i)
			Utils.out("T%d = %f%%", i, tierDistribution(i));
	}

	public int rerollTier(int tier) {
		/* Higher tiers have higher chances of rerolling
		 * + tiers can only be rerolled "down" */
		int newTier = (Utils.percentRoll(100 - tierDistribution(tier)) ? rollTier() : tier);
		return Math.min(tier, newTier);
	}

	public static int rollTier() {
		double tierChance, rollResult = Utils.randPercent();
		for (float tier = MAX_TIER; tier >= 0; --tier) {
			tierChance = tierDistribution(tier);
			if (Utils.percentRoll(rollResult, tierChance))
				return (int) tier;
		}
		return 0;
	}

	public static int clampTier(int tier) {
		return Math.max(0, Math.min(tier, MAX_TIER));
	}

	public static ItemCategories get() {
		return instance;
	}
}
