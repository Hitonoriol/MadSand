package hitonoriol.madsand.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import hitonoriol.madsand.util.Utils;

public enum Direction {
	RIGHT, UP_RIGHT, UP, UP_LEFT, LEFT, DOWN_LEFT, DOWN, DOWN_RIGHT;

	private static final Direction[] values = Direction.values();
	public static List<Direction> directions = new ArrayList<>(Arrays.asList(values));
	public static Direction[] baseValues = { RIGHT, UP, LEFT, DOWN };

	public Direction opposite() {
		switch (this) {
		case DOWN:
			return UP;
		case DOWN_LEFT:
			return UP_RIGHT;
		case DOWN_RIGHT:
			return UP_LEFT;
		case LEFT:
			return RIGHT;
		case RIGHT:
			return LEFT;
		case UP:
			return DOWN;
		case UP_LEFT:
			return DOWN_RIGHT;
		case UP_RIGHT:
			return DOWN_LEFT;
		default:
			return null;
		}
	}

	public boolean isBase() {
		return !isDiagonal();
	}

	public boolean isDiagonal() {
		return (this == UP_LEFT || this == UP_RIGHT || this == DOWN_LEFT || this == DOWN_RIGHT);
	}

	private Direction rotate(int dir) {
		return values[Math.floorMod(ArrayUtils.indexOf(values, this) + dir, values.length)];
	}

	public Direction rotateCounterClockwise() {
		return rotate(1);
	}

	public Direction rotateClockwise() {
		return rotate(-1);
	}

	/* Rotation in degrees where RIGHT is 0 */
	public float getRotation() {
		return ArrayUtils.indexOf(values, this) * 45f;
	}

	public static Direction random() {
		return Utils.randElement(baseValues);
	}
}
