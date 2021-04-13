package hitonoriol.madsand.pathfinding;

import com.badlogic.gdx.ai.pfa.indexed.DefaultIndexedGraph;
import com.badlogic.gdx.utils.Array;

import hitonoriol.madsand.Utils;

public class Graph extends DefaultIndexedGraph<Node> {

	private int changedIdx;

	public Graph() {
		super();
		resetChangedIdx();
	}

	public void addNode(Node node) {
		nodes.add(node);
	}

	public Node getNode(int index) {
		return nodes.get(index);
	}

	public Array<Node> getNodes() {
		return nodes;
	}

	public void clear() {
		nodes.clear();
		resetChangedIdx();
	}
	
	private void resetChangedIdx() {
		changedIdx = Integer.MAX_VALUE;
	}
	
	private boolean changed() {
		return changedIdx != Integer.MAX_VALUE;
	}

	public void remove(Node node) {
		if (node == null)
			return;

		changedIdx = Math.min(changedIdx, nodes.indexOf(node, true) - 1);
		Utils.out("Node {%s} removed. changedIdx = %d", node, changedIdx);

		nodes.removeValue(node, true);
	}

	public void reIndex() {
		if (changed()) {
			Utils.out("Reindexing %d pathfinding graph nodes...", getNodeCount() - changedIdx - 1);
			
			Node node = nodes.get(changedIdx);
			int startIdx = changedIdx + 1;
			Node curNode;
			int prevIdx = node.getIndex(), curIdx;

			if (startIdx != 0)
				for (int i = startIdx; i < getNodeCount(); ++i) {
					curNode = nodes.get(i);
					curIdx = curNode.getIndex();
					curNode.setIndex(prevIdx);
					prevIdx = curIdx;
				}

			resetChangedIdx();
		}
	}
}