package hitonoriol.madsand.containers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

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
		x0 = x;
		y0 = y;
	}

	public void setDestination(int x, int y) {
		x1 = x;
		y1 = y;
	}

	void build() {
		points = new ArrayList<>();
		forEachPoint(x0, y0, x1, y1, (x, y) -> points.add(new Pair(x, y)));
	}

	@Override
	public Iterator<Pair> iterator() {
		return points.iterator();
	}

	// Applies <pointAction> to each point in line while it returns true
	public static void rayCast(int x0, int y0, int x1, int y1, BiPredicate<Integer, Integer> pointAction) {
		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);

		int sx = x0 < x1 ? 1 : -1;
		int sy = y0 < y1 ? 1 : -1;
		int err = dx - dy;

		while (pointAction.test(x0, y0)) {
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

	public static void rayCast(Pair a, Pair b, BiPredicate<Integer, Integer> pointAction) {
		rayCast(a.x, a.y, b.x, b.y, pointAction);
	}

	public static void forEachPoint(int x0, int y0, int x1, int y1, BiConsumer<Integer, Integer> pointConsumer) {
		rayCast(x0, y0, x1, y1, (x, y) -> {
			pointConsumer.accept(x, y);
			return true;
		});
	}

	public static void forEachPoint(Pair a, Pair b, BiConsumer<Integer, Integer> pointConsumer) {
		forEachPoint(a.x, a.y, b.x, b.y, pointConsumer);
	}

	public static double calcDistance(float x0, float y0, float x1, float y1) {
		return Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
	}
}