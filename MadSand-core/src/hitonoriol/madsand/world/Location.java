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
	public static final int LAYER_MAX_DUNGEON = 50;
	public static final int LAYER_BASE_CAVE = 50; // Cave base value ( layer <n> of cave is BASE + <n> )
	public static final int LAYER_MAX_CAVE = 100;
	public static final int LAYER_MAX = 65535; // Layer number is saved as u16-bit int

	public int biome = -1;
	public boolean hasDungeon = false;

	HashMap<Integer, Map> layers = new HashMap<>();

	public void putLayer(int layer, Map map) {
		if (layers.containsKey(layer))
			layers.remove(layer);

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
