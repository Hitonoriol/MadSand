package hitonoriol.madsand.world;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.map.Map;

public class WorldMap {

	private Pair coords = new Pair();

	int xsz, ysz;
	public int curLayer = Location.LAYER_OVERWORLD;
	public Pair curWorldPos = new Pair();

	HashMap<Pair, Location> locations = new HashMap<>();

	public WorldMap(int sz) {
		xsz = ysz = sz;
		setCurWPos(xsz / 2, ysz / 2);
	}

	public WorldMap() {

	}

	public Location addMap(Pair coords, int layer, Map map) {
		Location location;

		if (!locations.containsKey(coords))
			location = new Location();
		else
			location = locations.remove(coords);

		location.putLayer(layer, map);

		locations.put(coords, location);

		return location;
	}

	public int wx() {
		return curWorldPos.x;
	}

	public int wy() {
		return curWorldPos.y;
	}

	@JsonIgnore
	public void setCurWPos(int wx, int wy) {
		curWorldPos.set(wx, wy);
	}

	public int getLayerCount(int wx, int wy) {
		return locations.get(coords.set(wx, wy)).getLayerCount();
	}

	public Location get(Pair coords) {
		return locations.get(coords);
	}

	public Location remove(Pair coords) {
		return locations.remove(coords);
	}

	public Location remove() {
		return remove(curWorldPos);
	}

	public void jumpToLocation(int x, int y, int layer) {
		setCurWPos(x, y);
		curLayer = layer;
	}

	public boolean hasLocation(Pair coords) {
		return locations.containsKey(coords);
	}
}
