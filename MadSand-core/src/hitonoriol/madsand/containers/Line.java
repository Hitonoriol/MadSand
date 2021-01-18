package hitonoriol.madsand.containers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Line implements Iterable<Pair> {
	private int x0, y0;
	private int x1, y1;
	private List<Pair> points;

	public List<Pair> getPoints() {
		return points;
	}

	public Line(int x0, int y0, int x1, int y1) {
		setOrigin(x0, y0);
		setDestination(x1, y1);
		build();
	}

	public Line(Pair a, Pair b) {
		this(a.x, a.y, b.x, b.y);
	}

	public void setOrigin(int x, int y) {
		this.x0 = x;
		this.y0 = y;
	}

	public void setDestination(int x, int y) {
		this.x1 = x;
		this.y1 = y;
	}

	void build() {
		points = new ArrayList<Pair>();

		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);

		int sx = x0 < x1 ? 1 : -1;
		int sy = y0 < y1 ? 1 : -1;
		int err = dx - dy;

		while (true) {
			points.add(new Pair(x0, y0));

			if (x0 == x1 && y0 == y1)
				break;

			int e2 = err * 2;
			if (e2 > -dx) {
				err -= dy;
				x0 += sx;
			}
			if (e2 < dx) {
				err += dx;
				y0 += sy;
			}
		}
	}

	public Iterator<Pair> iterator() {
		return points.iterator();
	}

	public static double calcDistance(int x1, int y1, int x2, int y2) {
		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}
}