package hitonoriol.madsand.pathfinding;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;

import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.util.Utils;

public class PathfinfingEngine {
	private Pair coords = new Pair();
	private IndexedAStarPathFinder<Node> pathFinder;
	private Graph graph;
	private DistanceHeuristic heuristic;
	private NodeMap nodeMap;
	private Map map;

	public PathfinfingEngine(Map map) {
		this.map = map;
	}

	public void init() {
		graph = new Graph();
		nodeMap = new NodeMap(graph, map.getWidth(), map.getHeight());
		heuristic = new DistanceHeuristic();
	}

	public Graph getGraph() {
		return graph;
	}

	public void refreshGraph() {
		Utils.out("Rebuilding pathfinding graph...");
		MapObject object;
		graph.clear();
		nodeMap.clear();
		for (int y = 0; y < map.getHeight(); ++y) {
			for (int x = 0; x < map.getWidth(); ++x) {
				object = map.getObject(x, y);
				if (object.equals(Map.nullObject) || object.nocollide)
					graph.addNode(nodeMap.putNew(x, y));
			}
		}

		for (int y = 0; y < map.getHeight(); ++y) {
			for (int x = 0; x < map.getWidth(); ++x)
				linkToNeighbors(x, y);
		}
		refreshPathFinder();
		Utils.out("Created " + graph.getNodeCount() + " nodes");
	}

	private void linkToNeighbors(int x, int y) {
		Node node = getNode(x, y);
		if (node == null)
			return;

		Pair nCoords = new Pair();
		for (Direction dir : Direction.baseValues) {
			nCoords.set(x, y).addDirection(dir);

			if (nodeMap.nodeExists(nCoords.x, nCoords.y)) {
				addNodeNeighbor(node, nCoords.x, nCoords.y);
				addNodeNeighbor(nodeMap.get(nCoords), x, y);
			}
		}
	}

	// Unlink node at x, y from all its neighbors
	private void unlinkFromNeighbors(int x, int y) {
		Node node = nodeMap.get(x, y);

		if (node == null)
			return;

		for (Direction dir : Direction.baseValues) {
			removeNodeNeighbor(nodeMap.get(coords.set(x, y).addDirection(dir)), x, y);
			removeNodeNeighbor(nodeMap.get(x, y), coords.x, coords.y);
		}
	}

	private void refreshPathFinder() {
		pathFinder = new IndexedAStarPathFinder<Node>(graph, true);
	}

	private void removeNodeNeighbor(Node node, int x, int y) {
		if (!map.validCoords(x, y) || node == null)
			return;

		node.removeNeighbor(nodeMap.get(x, y));
	}

	private void addNodeNeighbor(Node aNode, int x, int y) {
		if (!map.validCoords(x, y))
			return;

		aNode.addNeighbor(nodeMap.get(x, y));

	}

	public Node getNode(int x, int y) {
		return nodeMap.get(x, y);
	}

	public boolean searchPath(int startX, int startY, int endX, int endY, DefaultGraphPath<Node> path) {
		Node start = nodeMap.get(startX, startY), end = nodeMap.get(endX, endY);

		if (start == null || end == null)
			return false;

		return pathFinder.searchNodePath(start, end, heuristic, path);
	}

	public void objectRemoved(int x, int y) {
		if (graph.getNodeCount() == 0)
			return;

		graph.addNode(nodeMap.putNew(x, y));
		linkToNeighbors(x, y);
		refreshPathFinder();
	}

	public void objectAdded(MapObject object, int x, int y) {
		if (graph.getNodeCount() == 0)
			return;

		if (!object.nocollide) {
			unlinkFromNeighbors(x, y);
			graph.remove(nodeMap.remove(x, y));
		} else {
			graph.addNode(nodeMap.putNew(x, y));
			linkToNeighbors(x, y);
		}

		refreshPathFinder();
	}

}
