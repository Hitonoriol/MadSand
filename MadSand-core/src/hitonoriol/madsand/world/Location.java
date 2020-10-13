package hitonoriol.madsand.world;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.map.Map;

/*
 * {MapObjects, Tiles, NPCs, ...} --> Map --> Location --> WorldMap --> World
 */
public class Location {
	public static final int LAYER_OVERWORLD = 0;
	public static final int LAYER_BASE_DUNGEON = 1;
	public static final int LAYER_BASE_CAVE = 501;
	
	private HashMap<Integer, Map> layers = new HashMap<>();
	
	public void putLayer(int layer, Map map) {
		layers.put(layer, map);
	}
	
	public boolean hasLayer(int layer) {
		return layers.containsKey(layer);
	}
	
	@JsonIgnore
	public Map getLayer(int layer) {
		return layers.get(layer);
	}
	
	@JsonIgnore
	public int getLayerCount() {
		return layers.size();
	}
	
	@JsonIgnore
	public HashMap<Integer, Map> getLayers() {
		return layers;
	}
	
	@JsonIgnore
	public void setLayers(HashMap<Integer, Map> layers) {
		this.layers = layers;
	}
	
}
