package hitonoriol.madsand.pathfinding;

import java.util.function.Consumer;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.map.Map;

public class Path extends DefaultGraphPath<Node> {
	public Path(int capacity) {
		super(capacity);
	}

	public Path() {
		super();
	}

	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	public Node getDestination() {
		return super.get(super.getCount() - 1);
	}

	public void truncate(int newLength) {
		nodes.truncate(newLength);
	}

	public void forEachDirection(Consumer<Direction> dirAction) {
		if (getCount() < 2)
			return;

		Node prevNode = nodes.get(0);
		for (Node node : nodes) {
			if (node == prevNode)
				continue;

			dirAction.accept(Pair.getRelativeDirection(prevNode.x, prevNode.y, node.x, node.y, true));
			prevNode = node;
		}
	}

	public static Path create(int x1, int y1, int x2, int y2) {
		Map map = MadSand.world().getCurLoc();
		Path path = new Path();
		Line.forEachPoint(x1, y1, x2, y2, (x, y) -> {
			Node node = map.getNode(x, y);
			if (node == null)
				node = new Node(x, y);
			path.add(node);
		});
		return path;
	}

	public static Path create(Pair a, Pair b) {
		return create(a.x, a.y, b.x, b.y);
	}
}
