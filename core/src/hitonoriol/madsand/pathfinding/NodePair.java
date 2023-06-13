package hitonoriol.madsand.pathfinding;

import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.containers.Tuple;
import hitonoriol.madsand.enums.Direction;

public class NodePair extends Tuple<Node, Node> {
	public NodePair() {}

	public NodePair(Node l, Node r) {
		set(l, r);
	}

	public boolean isEmpty() {
		return l == null || r == null;
	}

	public Direction relativeDirection() {
		return Pair.getRelativeDirection(l.x, l.y, r.x, r.y, true);
	}
}
