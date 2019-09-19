package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.strings.InventoryNames;

public class CraftUtils {
	public static boolean craftItem(int id) {
		if (Loot.removeLootFromInv(InventoryNames.recipe.get(id))) {
			MadSand.player.inventory.putItem(id, 1, false);
		}
		return false;
	}
}
