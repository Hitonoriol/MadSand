package ru.bernarder.fallenrisefromdust.enums;

public enum ItemType {
	PlaceableObject(1), PlaceableTile(2), Crop(3), HeadArmor(4), ChestArmor(5), Shield(6), Consumable(9);
	
	
	private final int val;

	private ItemType(int val) {
		this.val = val;
	}

	public int get() {
		return val;
	}
}
