package hitonoriol.madsand.entities.equipment;

public enum EquipSlot {
	Head(0, "Head"), Chest(1, "Chest"), Legs(2, "Legs"), Feet(3, "Feet"),
	Offhand(4, "Offhand"), MainHand(5, "Hand");

	public final int number;
	public final String text;

	EquipSlot(int val, String slotString) {
		number = val;
		text = slotString;
	}

	public final static EquipSlot values[] = EquipSlot.values();

	public static EquipSlot getByNumber(int i) {
		return values[i];
	}
}
