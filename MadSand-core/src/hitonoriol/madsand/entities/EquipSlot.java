package hitonoriol.madsand.entities;

import hitonoriol.madsand.entities.inventory.ItemType;

public enum EquipSlot {
	Head(0, "Head"), Chest(1, "Chest"), Legs(2, "Legs"), Feet(3, "Feet"),
	Offhand(4, "Offhand"), MainHand(5, "Hand");

	public final int number;
	public final String text;

	EquipSlot(int val, String slotString) {
		this.number = val;
		this.text = slotString;
	}

	private static EquipSlot values[] = EquipSlot.values();

	public static EquipSlot getByNumber(int i) {
		return values[i];
	}

	public static EquipSlot slotByType(ItemType type) {
		switch (type) {
		case HeadArmor:
			return Head;

		case ChestArmor:
			return Chest;

		case LegArmor:
			return Legs;

		case FootArmor:
			return Feet;

		case Shield:
		case OffhandWeapon:
			return Offhand;

		default:
			break;
		}

		if (type.isWeapon())
			return MainHand;

		return null;
	}

	// Returns slot for any item (if item is not an equipment, one of the hand slots will be returned)
	public static EquipSlot slotByTypeAll(ItemType type) {
		EquipSlot slot = slotByType(type);
		if (slot == null)
			return type.handSlot();
		else
			return slot;
	}
}
