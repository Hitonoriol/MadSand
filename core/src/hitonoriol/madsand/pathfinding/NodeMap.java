package hitonoriol.madsand.pathfinding;

import java.util.HashMap;
import java.util.Map;

import hitonoriol.madsand.containers.Pair;

public class NodeMap {
	private Map<Pair, Node> map = new HashMap<>();
	private Graph graph;
	private Pair coords = new Pair();
	public int width, height;

	public NodeMap(Graph graph, int width, int height) {
		this.graph = graph;
		this.width = width;
		this.height = height;
	}

	public Node putNew(int x, int y) {
		Node node = new Node(x, y, graph.getNodeCount());
		map.put(new Pair(x, y), node);
		return node;
	}

	public Node remove(int x, int y) {
		return map.remove(coords.set(x, y));
	}

	public Node get(int x, int y) {
		return get(coords.set(x, y));
	}

	public Node get(Pair coords) {
		return map.get(coords);
	}

	public boolean nodeExists(int x, int y) {
		return map.containsKey(coords.set(x, y));
	}

	public void clear() {
		map.clear();
	}
}
