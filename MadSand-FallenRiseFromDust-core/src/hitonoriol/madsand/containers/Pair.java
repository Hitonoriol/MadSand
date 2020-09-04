package hitonoriol.madsand.containers;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import hitonoriol.madsand.enums.Direction;

public class Pair {
	public static final Pair nullPair = new Pair(-1, -1);

	public int x;
	public int y;

	public Pair(int x, int y) {
		set(x, y);
	}

	public Pair(Pair pair) {
		set(pair.x, pair.y);
	}

	public Pair() {
		this(0, 0);
	}

	public Pair set(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Pair add(Pair arg) {
		this.x += arg.x;
		this.y += arg.y;
		return this;
	}

	Pair add(int x, int y) {
		return add(new Pair(x, y));
	}

	public static Pair directionToCoord(Direction arg) {
		Pair ret = new Pair(0, 0);
		switch (arg) {
		case UP:
			ret = new Pair(0, 1);
			break;
		case DOWN:
			ret = new Pair(0, -1);
			break;
		case LEFT:
			ret = new Pair(-1, 0);
			break;
		case RIGHT:
			ret = new Pair(1, 0);
			break;
		case UP_LEFT:
			ret = new Pair(-1, 1);
			break;
		case UP_RIGHT:
			ret = new Pair(1, 1);
			break;
		case DOWN_LEFT:
			ret = new Pair(-1, -1);
			break;
		case DOWN_RIGHT:
			ret = new Pair(1, -1);
			break;
		}
		return ret;
	}

	public Direction toDirection() {
		if (x == 0 && y == 1)
			return Direction.UP;
		else if (x == 0 && y == -1)
			return Direction.DOWN;
		else if (x == -1 && y == 0)
			return Direction.LEFT;
		else if (x == 1 && y == 0)
			return Direction.RIGHT;
		else
			return null;
	}

	public static Direction getRelativeDirection(int originX, int originY, int cellX, int cellY) {
		Direction ret = null;
		if (originX > cellX)
			ret = Direction.RIGHT;
		else if (originX < cellX)
			ret = Direction.LEFT;
		else if (originY > cellY)
			ret = Direction.UP;
		else
			ret = Direction.DOWN;
		return ret;
	}

	public Pair addDirection(Direction dir) {
		return this.add(directionToCoord(dir));
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(211, 233).append(x).append(y).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Pair))
			return false;
		if (obj == this)
			return true;

		Pair rhs = (Pair) obj;
		return new EqualsBuilder().append(x, rhs.x).append(y, rhs.y).isEquals();
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}