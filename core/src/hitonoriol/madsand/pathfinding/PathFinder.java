package hitonoriol.madsand.pathfinding;

import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;

public class PathFinder extends IndexedAStarPathFinder<Node> {
	public PathFinder(Graph graph, boolean calculateMetrics) {
		super(graph, calculateMetrics);
	}
}
