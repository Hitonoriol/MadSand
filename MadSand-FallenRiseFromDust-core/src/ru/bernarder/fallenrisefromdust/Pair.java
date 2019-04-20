package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.enums.Direction;

public class Pair {
	int x, y;

	public Pair(int x, int y) {
		set(x, y);
	}

	Pair set(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}

	Pair add(Pair arg) {
		this.x += arg.x;
		this.y += arg.y;
		return this;
	}

	Pair add(int x, int y) {
		return add(new Pair(x, y));
	}

	private Pair directionToCoord(Direction arg) {
		if (arg.equals(Direction.UP))
			return new Pair(0, 1);
		else if (arg.equals(Direction.DOWN))
			return new Pair(0, -1);
		else if (arg.equals(Direction.LEFT))
			return new Pair(-1, 0);
		else
			return new Pair(1, 0);
	}

	Pair addDirection(Direction dir) {
		return this.add(directionToCoord(dir));
	}
}