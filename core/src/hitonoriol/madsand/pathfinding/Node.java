package hitonoriol.madsand.pathfinding;

import java.util.Iterator;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultConnection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedNode;
import com.badlogic.gdx.utils.Array;

public class Node implements IndexedNode<Node>, Comparable<Node> {

	private int index;
	public final int x, y;

	Array<Connection<Node>> connections = new Array<Connection<Node>>();

	public Node(int x, int y, int index) {
		this.index = index;
		this.x = x;
		this.y = y;
	}

	// this will break everything if you try to pathfind over it
	public Node(int x, int y) {
		this(x, y, 0);
	}

	@Override
	public int getIndex() {
		return index;
	}

	void setIndex(int idx) {
		index = idx;
	}

	public boolean at(int x, int y) {
		return this.x == x && this.y == y;
	}

	@Override
	public Array<Connection<Node>> getConnections() {
		return connections;
	}

	public void addNeighbor(Node node) {
		if (node == null)
			return;

		if (!hasNeighbor(node))
			connections.add(new DefaultConnection<Node>(this, node));
	}

	public boolean hasNeighbor(Node node) {
		if (connections.isEmpty())
			return false;

		Connection<Node> connection;
		for (int i = 0; i < connections.size; ++i) {
			connection = connections.get(i);
			if (connection.getToNode() == node)
				return true;
		}

		return false;
	}

	public void removeNeighbor(Node node) {
		if (node == null)
			return;

		Iterator<Connection<Node>> it = connections.iterator();
		Connection<Node> connection;
		while (it.hasNext()) {
			connection = it.next();
			if (connection.getToNode() == node)
				it.remove();
		}

	}

	public String toString() {
		return String.format("#%d (%d, %d) [%d neighbors]", index, x, y, connections.size);
	}

	@Override
	public int compareTo(Node o) {
		return Integer.compare(index, o.index);
	}

}
