package hitonoriol.madsand.pathfinding;

import java.util.Iterator;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultConnection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedNode;
import com.badlogic.gdx.utils.Array;

public class Node implements IndexedNode<Node> {

	private int index;
	public final int x, y;

	Array<Connection<Node>> connections = new Array<Connection<Node>>();

	public Node(int x, int y, int index) {
		this.index = index;
		this.x = x;
		this.y = y;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public Array<Connection<Node>> getConnections() {
		return connections;
	}

	public void addNeighbor(Node node) {
		if (node != null)
			connections.add(new DefaultConnection<Node>(this, node));
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
		return String.format("Index:%d x:%d y:%d", index, x, y);
	}

}
