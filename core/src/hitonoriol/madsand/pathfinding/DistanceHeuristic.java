package hitonoriol.madsand.pathfinding;

import com.badlogic.gdx.ai.pfa.Heuristic;

public class DistanceHeuristic implements Heuristic<Node> {

	@Override
	public float estimate(Node node, Node endNode) {
		if (node == null || endNode == null)
			return Float.MAX_VALUE;

		return Math.abs(endNode.x - node.x) + Math.abs(endNode.y - node.y);
	}

}