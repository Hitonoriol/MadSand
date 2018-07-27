package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.strings.InventoryNames;
import values.PlayerStats;

public class CraftUtils {
	public static boolean craftItem(int id) {
		if (LootLayer.removeLootFromInv(InventoryNames.recipe.get(id))) {
			PlayerActions.craftingup();
			InvUtils.putItem(id, PlayerStats.craftingskill[0], false);

		}
		return false;
	}
}
