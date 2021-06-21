package hitonoriol.madsand.enums;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import hitonoriol.madsand.util.Utils;

public enum Direction {
	UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT;

	public static List<Direction> directions = Arrays.asList(Direction.values());
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

	public Direction counterClockwise() {
		return baseValues[(ArrayUtils.indexOf(baseValues, this) + 1) % baseValues.length];
	}

	/* Rotation in degrees where RIGHT is 0 */
	public float getRotation() {
		return ArrayUtils.indexOf(baseValues, this) * 90f;
	}

	public static Direction random() {
		return Utils.randElement(baseValues);
	}
}
