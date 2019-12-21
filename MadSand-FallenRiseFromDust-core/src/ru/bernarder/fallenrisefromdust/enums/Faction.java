package ru.bernarder.fallenrisefromdust.enums;

public enum Faction{
	None(0), Outlaws(1), Partisans(2), Monsters(3), Marauders(4);
	
	private final int val;

	private Faction(int val) {
		this.val = val;
	}

	public int get() {
		return val;
	}
	
	private static Faction[] values = Faction.values();
	
	public static Faction get(int i) {
	    return values[i - 1];
	}
}
