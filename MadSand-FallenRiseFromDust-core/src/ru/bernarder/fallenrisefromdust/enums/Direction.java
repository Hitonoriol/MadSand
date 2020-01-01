package ru.bernarder.fallenrisefromdust.enums;

import ru.bernarder.fallenrisefromdust.Utils;

public enum Direction {
	UP(1), DOWN(2), LEFT(3), RIGHT(4), UP_LEFT(5), UP_RIGHT(6), DOWN_LEFT(7), DOWN_RIGHT(8);

	private final int val;

	private Direction(int val) {
		this.val = val;
	}

	public int get() {
		return val;
	}

	private static Direction[] values = Direction.values();

	public static Direction get(int i) {
		return values[i];
	}

	public static Direction random() {
		return get(Utils.rand(0, 3));
	}
}
