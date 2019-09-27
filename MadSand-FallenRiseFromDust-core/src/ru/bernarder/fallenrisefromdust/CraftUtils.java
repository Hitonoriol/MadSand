package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.properties.ItemProp;

public class CraftUtils {
	public static boolean craftItem(int id) {
		if (Loot.removeLootFromInv(ItemProp.recipe.get(id))) {
			World.player.inventory.putItem(id, 1, false);
		}
		return false;
	}
}
