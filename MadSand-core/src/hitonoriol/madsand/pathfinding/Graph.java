package hitonoriol.madsand.pathfinding;

import com.badlogic.gdx.ai.pfa.indexed.DefaultIndexedGraph;

public class Graph extends DefaultIndexedGraph<Node> {
    
    public Graph() {
        super();
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public Node getNode(int index) {
        return nodes.get(index);
    }
    
    public void clear() {
    	nodes.clear();
    }
    
    public void remove(Node node) {
    	nodes.removeValue(node, true);
    }

}