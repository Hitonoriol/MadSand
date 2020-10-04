package hitonoriol.madsand.enums;

public enum ItemType {
	Item(0), PlaceableObject(1), PlaceableTile(2), Crop(3), HeadArmor(4), ChestArmor(5), Shield(6), Consumable(7),
	Axe(8), Shovel(9), Pickaxe(10), Hoe(11), Weapon(12), LegArmor(13), FootArmor(14), OffhandWeapon(15), GrabBag(100);

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

	public boolean isConsumable() {
		return this == Consumable ||
				this == GrabBag;
	}

	public boolean isWeapon() {
		return this == Weapon ||
				this == OffhandWeapon;
	}

	public boolean isArmor() {
		return this == HeadArmor ||
				this == ChestArmor ||
				this == Shield ||
				this == LegArmor ||
				this == FootArmor;
	}

	public boolean isEquipment() {
		return isWeapon() || isArmor();
	}

	public boolean isTool() {
		return this == Hoe ||
				this == Pickaxe ||
				this == Shovel ||
				this == Axe;
	}

	public boolean isUnique() { //Unique = non-stackable
		return (isWeapon() || isTool() || isArmor());
	}
}
