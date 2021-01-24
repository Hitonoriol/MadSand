package hitonoriol.madsand.entities.inventory;

import hitonoriol.madsand.entities.EquipSlot;
import hitonoriol.madsand.entities.Skill;

public enum ItemType {
	None,
	PlaceableObject, PlaceableTile,
	Crop,
	Consumable,
	Axe, Shovel, Pickaxe, Hoe, Hammer,
	MeleeWeapon, OffhandWeapon, RangedWeapon,
	Projectile,
	HeadArmor, ChestArmor, LegArmor, FootArmor, Shield,
	GrabBag,
	FishingRod, FishingBait;

	// For non-equipment items: if player takes the item in hand, which hand does it go to:
	public EquipSlot handSlot() {
		return (this == FishingBait || this == Projectile)
				? EquipSlot.Offhand
				: EquipSlot.MainHand;
	}

	public boolean isPlaceable() {
		return this == PlaceableObject ||
				this == PlaceableTile;
	}

	public boolean isConsumable() {
		return this == Consumable ||
				this == GrabBag;
	}

	public boolean isWeapon() {
		return this == MeleeWeapon ||
				this == OffhandWeapon ||
				this == RangedWeapon;
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
				this == FishingRod ||
				this == Hammer;
	}

	public boolean isUnique() { // Unique = non-stackable
		return (isWeapon() || isTool() || isArmor());
	}

	public static ItemType bySkill(Skill skill) {
		switch (skill) {

		case Farming:
			return Hoe;

		case Digging:
			return Shovel;

		case Mining:
			return Pickaxe;

		case Woodcutting:
			return Axe;

		default:
			return None;

		}
	}
}