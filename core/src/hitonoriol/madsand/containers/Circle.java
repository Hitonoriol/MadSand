package hitonoriol.madsand.containers;

import java.util.function.BiConsumer;

public class Circle {
	public static void forEachPoint(int ox, int oy, int r, BiConsumer<Integer, Integer> pointConsumer) {
		for (int x = -r; x < r; ++x) {
			for (int y = -r; y < r; ++y) {
				if (x * x + y * y > r * r)
					continue;
				pointConsumer.accept(x, y);
			}
		}
	}
}
