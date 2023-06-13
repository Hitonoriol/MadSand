package hitonoriol.madsand.world;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.util.Utils;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class WorldMap {
	@JsonIgnore
	private Pair coords = new Pair();

	int xsz, ysz;
	public int curLayer = Location.LAYER_OVERWORLD;
	public Pair curWorldPos = new Pair();

	@JsonIgnore
	private HashMap<Pair, Location> locations = new HashMap<>();

	public WorldMap(int sz) {
		xsz = ysz = sz;
		setCurWPos(xsz / 2, ysz / 2);
	}

	public WorldMap() {}

	public boolean validCoords(Pair coords) {
		return coords.x >= 1 && coords.y >= 1 && coords.x <= xsz && coords.y <= ysz;
	}

	public void addLocation(Pair coords, Location location) {
		locations.put(coords, location);
	}

	public Location createLocation(int wx, int wy) {
		var location = new Location();
		addLocation(new Pair(wx, wy), location);
		return location;
	}

	public Location addMap(Pair coords, int layer, Map map) {
		if (!locations.containsKey(coords)) {
			Utils.dbg("Didn't find location @ %s, creating a new one...", coords);
			addLocation(coords, new Location());
		}

		var location = getLocation(coords);
		location.putLayer(layer, map);
		Utils.dbg(
			"{%X} Adding map {%X} to Location {%X} @ %s", hashCode(), map.hashCode(), location.hashCode(),
			coords
		);
		return location;
	}

	@JsonIgnore
	/* This is entirely cosmetic, real layer number is <curLayer> */
	public int getCurLocationLayer() { // Returns layer with layer type's offset (currently only for Caves)
		if (curLayer > Location.LAYER_BASE_CAVE)
			return curLayer - Location.LAYER_BASE_CAVE;

		return curLayer;
	}

	public Location.Layer getLayerType() {
		return Location.Layer.byNumber(curLayer);
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

	public Location getLocation(Pair coords) {
		return locations.get(coords);
	}

	@JsonIgnore
	public Location getLocation() {
		return getLocation(curWorldPos);
	}

	public Location remove(Pair coords) {
		return locations.remove(coords);
	}

	public Location remove() {
		return remove(curWorldPos);
	}

	public void jumpToLocation(int x, int y, int layer) {
		setCurWPos(x, y);

		if (layer != Location.LAYER_OVERWORLD) {
			if (!getLocation(coords.set(x, y)).hasDungeon) {
				if (curLayer >= Location.LAYER_BASE_CAVE && layer < Location.LAYER_BASE_CAVE)
					layer = Location.LAYER_OVERWORLD;
				else
					layer = Location.LAYER_BASE_CAVE + layer;
			}
		}

		curLayer = layer;
	}

	public boolean hasLocation(Pair coords) {
		return locations.containsKey(coords);
	}
}
