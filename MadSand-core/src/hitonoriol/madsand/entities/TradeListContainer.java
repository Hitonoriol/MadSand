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
		TradeCategory[] categoryList = TradeCategory.values();

		while (category.equals(TradeCategory.Random) || category.equals(TradeCategory.All))
			category = categoryList[Utils.rand(categoryList.length)];

		for (int i = tier; i >= 0; i--) {
			Utils.out("Rolling tradeList " + category + " tier: " + tier);
			getTradeItemList(category, tier).roll(items);
		}

		return items;
	}
}
