package hitonoriol.madsand.enums;

import java.util.Arrays;
import java.util.List;

import hitonoriol.madsand.Utils;

public enum Direction {
	UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT;

	public static List<Direction> directions = Arrays.asList(Direction.values());
	public static Direction[] baseValues = { UP, DOWN, LEFT, RIGHT };

	public boolean isBase() {
		return !isDiagonal();
	}

	public boolean isDiagonal() {
		return (this == UP_LEFT || this == UP_RIGHT || this == DOWN_LEFT || this == DOWN_RIGHT);
	}

	public static Direction random() {
		int num = Utils.rand(0, 3);
		switch (num) {
		case 0:
			return UP;
		case 1:
			return DOWN;
		case 2:
			return LEFT;
		default:
			return RIGHT;
		}
	}
}
