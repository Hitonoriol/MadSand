package hitonoriol.madsand.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.text.WordUtils;

import com.badlogic.gdx.Input.Keys;

import hitonoriol.madsand.util.Utils;

public enum Direction {
	RIGHT, UP_RIGHT, UP, UP_LEFT, LEFT, DOWN_LEFT, DOWN, DOWN_RIGHT;

	private static final Direction[] values = Direction.values();
	public static final Direction[] baseValues = { RIGHT, UP, LEFT, DOWN };
	public static final List<Direction> directions = Arrays.asList(values);
	public static final int BASE_DIRECTIONS = baseValues.length, TOTAL_DIRECTIONS = values.length;

	public static List<Direction> asList() {
		return new ArrayList<>(directions);
	}

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

	public boolean isPositive() {
		return !isDown() && !isLeft();
	}

	public boolean isNegative() {
		return !isPositive();
	}

	public boolean isLeft() {
		return this == LEFT || this == UP_LEFT || this == DOWN_LEFT;
	}

	public boolean isRight() {
		return this == RIGHT || this == UP_RIGHT || this == DOWN_RIGHT;
	}

	public boolean isDown() {
		return this == DOWN || this == DOWN_LEFT || this == DOWN_RIGHT;
	}

	public boolean isUp() {
		return this == UP || this == UP_LEFT || this == UP_RIGHT;
	}

	public boolean isVertical() {
		return this == UP || this == DOWN;
	}

	public boolean isHorizontal() {
		return this == LEFT || this == RIGHT;
	}

	public boolean isBase() {
		return !isDiagonal();
	}

	public boolean isDiagonal() {
		return (this == UP_LEFT || this == UP_RIGHT || this == DOWN_LEFT || this == DOWN_RIGHT);
	}

	public Direction rotateCounterClockwise() {
		return rotate(1);
	}

	public Direction rotateClockwise() {
		return rotate(-1);
	}

	private Direction rotate(int dir) {
		return values[Math.floorMod(ordinal() + dir, TOTAL_DIRECTIONS)];
	}

	/* Rotation in degrees where RIGHT is 0 */
	public float getRotation() {
		return ordinal() * 45f;
	}

	public int baseOrdinal() {
		return ordinal() / 2;
	}

	public Direction getBase(int idx) {
		return baseValues[idx];
	}

	public int toKey() {
		return Keys.valueOf(toString());
	}

	public static void forEachBase(Consumer<Direction> action) {
		for (Direction dir : baseValues)
			action.accept(dir);
	}

	public static Direction random() {
		return Utils.randElement(baseValues);
	}

	@Override
	public String toString() {
		return WordUtils.capitalizeFully(name());
	}
}
