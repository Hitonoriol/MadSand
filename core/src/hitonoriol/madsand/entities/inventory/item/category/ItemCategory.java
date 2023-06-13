package hitonoriol.madsand.entities.inventory.item.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hitonoriol.madsand.util.Utils;

public enum ItemCategory {
	All, // Flag: Include items from all categories
	Random, // Flag: Roll a random category
	Trash, // Useless items

	Food,
	Potions,
	Farming,
	Tools,
	Weapons,
	Projectiles,
	Armor,
	Building,
	Materials,
	Scrolls,
	Pills,
	Recipes;

	public static List<ItemCategory> rollableCategories = new ArrayList<>(Arrays.asList(values()));
	static {
		rollableCategories.remove(All);
		rollableCategories.remove(Random);
		rollableCategories.remove(Trash);
	}

	public boolean isFlag() {
		return this == ItemCategory.Random ||
			this == ItemCategory.All;
	}

	public static ItemCategory random() {
		return Utils.randElement(rollableCategories);
	}

}
