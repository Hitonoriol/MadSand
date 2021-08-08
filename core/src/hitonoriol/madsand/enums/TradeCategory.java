package hitonoriol.madsand.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hitonoriol.madsand.util.Utils;

public enum TradeCategory {
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

	public static List<TradeCategory> rollableCategories = new ArrayList<>(Arrays.asList(values()));
	static {
		rollableCategories.remove(All);
		rollableCategories.remove(Random);
		rollableCategories.remove(Trash);
	}

	public boolean isFlag() {
		return this.equals(TradeCategory.Random) ||
				this.equals(TradeCategory.All);
	}

	public static TradeCategory roll() {
		return Utils.randElement(rollableCategories);
	}

}
