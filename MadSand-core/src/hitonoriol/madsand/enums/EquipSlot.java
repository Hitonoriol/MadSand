package hitonoriol.madsand.enums;

public enum EquipSlot {
	Head(0, "Head"), Chest(1, "Chest"), Legs(2, "Legs"), Feet(3, "Feet"),
	Offhand(4, "Offhand"), MainHand(5, "Hand");

	public final int number;
	public final String text;

	EquipSlot(int val, String slotString) {
		this.number = val;
		this.text = slotString;
	}
}
