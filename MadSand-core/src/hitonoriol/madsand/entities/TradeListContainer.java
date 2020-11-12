package hitonoriol.madsand.entities;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.TradeCategory;

/*
 * Container for TradeItemList 
 * where for each TradeCategory there can be multiple TradeItemLists 
 */

public class TradeListContainer extends HashMap<TradeCategory, ArrayList<TradeItemList>> {

	public static int MAX_TIER = 10;

	public TradeListContainer() {
		super();
	}

	@JsonIgnore
	public TradeItemList getTradeItemList(TradeCategory category, int tier) {
		ArrayList<TradeItemList> itemLists = super.get(category);

		for (TradeItemList list : itemLists)
			if (list.tier == tier)
				return list;

		return itemLists.get(0);
	}

	@JsonIgnore
	public TradeItemList getTradeItemList(TradeCategory category) {
		return getTradeItemList(category, 0);
	}

	public void put(TradeCategory category, TradeItemList itemList) {
		ArrayList<TradeItemList> valList = null;

		if (super.containsKey(category))
			valList = super.get(category);

		if (valList == null)
			valList = new ArrayList<>();

		valList.add(itemList);

		super.remove(category);
		super.put(category, valList);
	}

	
	public ArrayList<Item> roll(TradeCategory category, int tier) {
		ArrayList<Item> items = new ArrayList<>();
		boolean all = category.equals(TradeCategory.All);
		TradeItemList list;

		if (category.isFlag())
			category = TradeCategory.roll();

		list = getTradeItemList(category, tier);

		for (int i = tier; i >= 0; i--) {
			Utils.out("Rolling tradeList " + category + " tier: " + tier);

			if (!all)
				getTradeItemList(category, tier).roll(items);
			else {
				for (int j = 0; j < list.rollTimes(); ++j) {
					list = getTradeItemList(category, tier);
					category = TradeCategory.roll();
					items.add(list.rollItem());
				}
			}
		}

		return items;
	}

	public int rollTier() {
		double baseChance;
		for (int i = 0; i <= MAX_TIER; ++i) {
			baseChance = (1 / (i + 1));
			if (Utils.percentRoll(baseChance * 0.85))
				return i;
		}
		return 0;
	}
}
