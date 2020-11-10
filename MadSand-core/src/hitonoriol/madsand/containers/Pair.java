package hitonoriol.madsand.containers;

import java.io.IOException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.enums.Direction;

public class Pair {
	public static final Pair nullPair = new Pair(-1, -1);
	private static Pair instance = new Pair();

	public int x;
	public int y;

	public Pair(int x, int y) {
		set(x, y);
	}

	public Pair(Pair pair) {
		set(pair);
	}

	public Pair() {
		this(0, 0);
	}

	public Pair set(Pair pair) {
		set(pair.x, pair.y);
		return this;
	}

	public Pair set(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}

	Pair add(int x, int y) {
		return set(this.x + x, this.y + y);
	}

	public Pair add(Pair arg) {
		return add(arg.x, arg.y);
	}

	public Pair random(int xMax, int yMax) {
		return this.set(Utils.rand(xMax), Utils.rand(yMax));
	}

	public static Pair getInstance() {
		return instance;
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

	public static Direction getRelativeDirection(int originX, int originY, int cellX, int cellY, boolean fourWay) {
		Direction ret = null;
		int dx = cellX - originX;
		int dy = cellY - originY;

		if (dy > 0)
			ret = Direction.UP;
		else if (dy < 0)
			ret = Direction.DOWN;

		if (dx > 0)
			ret = Direction.RIGHT;
		else if (dx < 0)
			ret = Direction.LEFT;

		if (fourWay)
			return ret;

		if (dx < 0 && dy < 0)
			ret = Direction.DOWN_LEFT;
		else if (dx > 0 && dy < 0)
			ret = Direction.DOWN_RIGHT;
		else if (dx < 0 && dy > 0)
			ret = Direction.UP_LEFT;
		else if (dx > 0 && dy > 0)
			ret = Direction.UP_RIGHT;

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

	public boolean equals(int x, int y) {
		return (this.x == x && this.y == y);
	}

	final String PAIR_STRING_DELIMITER = ", ";

	@Override
	public String toString() {
		return x + PAIR_STRING_DELIMITER + y;
	}

	public static Pair make(int x, int y) {
		return new Pair(x, y);
	}

	public class PairKeyDeserializer extends KeyDeserializer {

		@Override
		public Pair deserializeKey(String key, DeserializationContext ctxt) throws IOException {
			String[] pair = key.split(PAIR_STRING_DELIMITER);
			return new Pair(Integer.parseInt(pair[0]), Integer.parseInt(pair[1]));
		}
	}
}