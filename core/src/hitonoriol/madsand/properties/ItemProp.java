package hitonoriol.madsand.properties;

import java.util.ArrayList;
import java.util.HashMap;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.inventory.item.CropSeeds;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.Placeable;
import hitonoriol.madsand.map.CropGrowthStageContainer;

public class ItemProp {
	public static HashMap<Integer, ? extends Item> items = new HashMap<>();
	public static HashMap<Integer, ArrayList<Integer>> craftStationRecipes = new HashMap<>();
	public static HashMap<Integer, ArrayList<Integer>> craftReq = new HashMap<>();
	public static HashMap<Integer, ArrayList<Integer>> buildReq = new HashMap<>();

	public static void addCraftStationRecipe(int id, int item) {
		ArrayList<Integer> items;
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
		return ((Placeable)items.get(id)).getAltObject();
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