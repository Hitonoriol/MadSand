package hitonoriol.madsand.enums;

public enum Stat {
	Constitution, Dexterity, Strength, Accuracy, Intelligence, Luck, Defense;

	public boolean excludeFromSum() {
		return (this == Defense);
	}
}
