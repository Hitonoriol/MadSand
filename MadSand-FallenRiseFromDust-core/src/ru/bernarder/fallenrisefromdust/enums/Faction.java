package ru.bernarder.fallenrisefromdust.enums;

public enum Faction{
	NONE(0), OUTLAW(1), PARTISAN(2);
	
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
