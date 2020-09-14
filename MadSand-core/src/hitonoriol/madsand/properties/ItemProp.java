package hitonoriol.madsand.properties;

import java.util.ArrayList;
import java.util.HashMap;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.ItemType;
import hitonoriol.madsand.map.CropGrowthStageContainer;

public class ItemProp {
	public static HashMap<Integer, Item> items = new HashMap<>();
	public static HashMap<Integer, ArrayList<Integer>> craftReq = new HashMap<>();

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

	public static ItemType getType(int id) {
		return items.get(id).type;
	}

	public static int getAltObject(int id) {
		return items.get(id).altObject;
	}

	public static String getOnUseAction(int id) {
		String action = items.get(id).useAction;

		if (action == null)
			return Resources.emptyField;

		return action;
	}

	public static CropGrowthStageContainer getCropStages(int id) {
		return items.get(id).cropStages;
	}
}