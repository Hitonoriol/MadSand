package hitonoriol.madsand.enums;

public enum ItemType {
	None, PlaceableObject, PlaceableTile, Crop, HeadArmor, ChestArmor, Shield, Consumable,
	Axe, Shovel, Pickaxe, Hoe, MeleeWeapon, LegArmor, FootArmor, OffhandWeapon, GrabBag,
	FishingRod, FishingBait;

	// For non-equipment items: if player takes the item in hand, which hand does it go to:
	public EquipSlot handSlot() {
		return (this == FishingBait) ? EquipSlot.Offhand : EquipSlot.MainHand;
	}

	public boolean isConsumable() {
		return this == Consumable ||
				this == GrabBag;
	}

	public boolean isWeapon() {
		return this == MeleeWeapon ||
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
				this == Axe ||
				this == FishingRod;
	}

	public boolean isUnique() { //Unique = non-stackable
		return (isWeapon() || isTool() || isArmor());
	}
}
