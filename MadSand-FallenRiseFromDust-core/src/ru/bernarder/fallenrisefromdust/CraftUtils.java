package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.strings.InventoryNames;

public class CraftUtils {
	public static boolean craftItem(int id) {
		if (LootNode.removeLootFromInv(InventoryNames.recipe.get(id))) {
			InvUtils.putItem(id, 1, false);

		}
		return false;
	}
}
