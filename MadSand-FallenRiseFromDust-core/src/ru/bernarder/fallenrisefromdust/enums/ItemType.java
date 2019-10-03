package ru.bernarder.fallenrisefromdust.enums;

public enum ItemType {
	Item(0), PlaceableObject(1), PlaceableTile(2), Crop(3), HeadArmor(4), ChestArmor(5), Shield(6), Consumable(7),
	Axe(8), Shovel(9), Pickaxe(10), Hoe(11), Weapon(12);

	private final int val;

	private ItemType(int val) {
		this.val = val;
	}

	public int get() {
		return val;
	}

	private static ItemType[] values = ItemType.values();

	public static ItemType get(int i) {
		if (i < 0)
			i = 0;
		return values[i];
	}
}
