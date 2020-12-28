package hitonoriol.madsand.enums;

import hitonoriol.madsand.Utils;

public enum TradeCategory {
	All, // Include all categories
	Random, // Roll a random category
	Trash, // useless items

	Food,
	Potions,
	Farming,
	Tools,
	Weapons,
	Armor,
	Building,
	Materials;

	public boolean isFlag() {
		return this.equals(TradeCategory.Random) ||
				this.equals(TradeCategory.All);
	}

	private static TradeCategory categories[] = values();

	public static TradeCategory roll() {
		TradeCategory category = TradeCategory.All;
		while (category.isFlag())
			category = categories[Utils.rand(categories.length)];
		return category;
	}

}
