package hitonoriol.madsand.pathfinding;

import java.util.HashMap;
import java.util.Map;

import hitonoriol.madsand.containers.Pair;

public class NodeMap {
	private Map<Pair, Node> map = new HashMap<>();
	private Pair coords = new Pair();
	private int idx = 0;
	public int width, height;

	public NodeMap(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public Node putNew(int x, int y) {
		Node node = new Node(x, y, idx++);
		map.put(coords.set(x, y), node);
		return node;
	}

	public Node remove(int x, int y) {
		return map.remove(coords.set(x, y));
	}

	public Node get(int x, int y) {
		return map.get(coords.set(x, y));
	}

	public Node get(Pair coords) {
		return get(coords.x, coords.y);
	}
	
	public void clear() {
		idx = 0;
		map.clear();
	}
}
