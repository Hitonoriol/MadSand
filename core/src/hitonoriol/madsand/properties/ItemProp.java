package hitonoriol.madsand.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hitonoriol.madsand.entities.inventory.item.CropSeeds;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.PlaceableItem;
import hitonoriol.madsand.entities.inventory.item.category.ItemCategories;
import hitonoriol.madsand.map.CropGrowthStageContainer;
import hitonoriol.madsand.resources.Resources;

public class ItemProp {
	public static Map<Integer, Item> items;
	public static Map<Integer, List<Integer>> craftStationRecipes = new HashMap<>();
	public static Map<Integer, List<Integer>> craftReq = new HashMap<>();
	public static Map<Integer, List<Integer>> buildReq = new HashMap<>();
	public static ItemCategories itemCategories = new ItemCategories();

	public static void addCraftStationRecipe(int id, int item) {
		List<Integer> items;
		if (!craftStationRecipes.containsKey(id))
			items = new ArrayList<>();
		else
			items = craftStationRecipes.remove(id);

		items.add(item);
		craftStationRecipes.put(id, items);

	}

	public static Item getItem(int id) {
		return items.get(id);
	}

	public static String getItemName(int id) {
		return items.get(id).name;
	}

	public static String getCraftRecipe(int id) {
		return items.get(id).recipe;
	}

	public static int getCraftQuantity(int id) {
		int quantity = items.get(id).craftQuantity;

		if (quantity < 1)
			quantity = 1;

		return quantity;
	}

	public static int getCost(int id) {
		return items.get(id).cost;
	}

	public static int getAltObject(int id) {
		return ((PlaceableItem)items.get(id)).getAltObject();
	}

	public static String getOnUseAction(int id) {
		String action = items.get(id).useAction;

		if (action == null)
			return Resources.emptyField;

		return action;
	}

	public static CropGrowthStageContainer getCropStages(int id) {
		return ((CropSeeds)items.get(id)).cropContainer.cropStages;
	}
	
	public static int getCropSoil(int id) {
		return ((CropSeeds)items.get(id)).cropContainer.soil;
	}
}