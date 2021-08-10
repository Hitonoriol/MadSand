package hitonoriol.madsand.entities.inventory.item.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.util.Utils;

/*
 * Container for TradeItemList 
 * where for each TradeCategory there can be multiple TradeItemLists 
 */

public class ItemCategories extends HashMap<ItemCategory, ArrayList<ItemCategoryList>> {

	public static int MAX_TIER = 10;

	public ItemCategories() {
		super();
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

	public List<Item> roll(ItemCategory category, int tier) {
		List<Item> items = new ArrayList<>();
		boolean all = category.equals(ItemCategory.All);
		ItemCategoryList list;

		if (category.isFlag())
			category = ItemCategory.random();

		list = getTradeItemList(category, tier);

		for (int i = tier; i >= 0; i--) {
			Utils.dbg("Rolling tradeList " + category + " tier: " + tier);

			if (!all)
				getTradeItemList(category, tier).roll(items);
			else {
				for (int j = 0; j < list.rollTimes(); ++j) {
					list = getTradeItemList(category, tier);
					tier = rerollTier(tier);
					category = ItemCategory.random();
					items.add(list.rollItem());
				}
			}
			tier = rerollTier(tier);
		}

		return items;
	}

	public List<Item> roll(ItemCategory category) {
		return roll(category, rollTier());
	}

	public int rollId(ItemCategory category, int tier) {
		return roll(category, tier < 0 ? NpcProp.tradeLists.rollTier() : tier).get(0).id();
	}

	public int rollId(ItemCategory category) {
		return rollId(category, 0);
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
