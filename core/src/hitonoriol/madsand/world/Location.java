package hitonoriol.madsand.world;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Faction;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.Settlement.Status;

/*
 * {MapObjects, Tiles, NPCs, ...} --> Map --> Location --> WorldMap --> World
 */

public class Location {
	public static final int LAYER_MAX = 65535; // Layer number is saved as u16-bit int

	public static final int LAYER_OVERWORLD = Layer.Overworld.base();
	public static final int LAYER_BASE_DUNGEON = Layer.Dungeon.base(), LAYER_MAX_DUNGEON = Layer.Dungeon.max();
	public static final int LAYER_BASE_CAVE = Layer.Cave.base(), LAYER_MAX_CAVE = Layer.Cave.max();

	public String name = "Wilderness";
	public int biome = -1;
	public boolean hasDungeon = false;
	public Faction faction = Faction.None;
	public Settlement settlement;

	private java.util.Map<Integer, Map> layers = new HashMap<>();

	public void putLayer(int layer, Map map) {
		if (layers.containsKey(layer))
			layers.remove(layer);

		layers.put(layer, map);
	}

	@JsonGetter("settlement")
	public Settlement getSettlement() {
		if (settlement != null)
			if (settlement.getLocation() == null) {
				settlement.setLocation(this);
				settlement.warehouse.refreshWeight();
			}

		return settlement;
	}

	@JsonSetter("settlement")
	public void setSettlement(Settlement settlement) {
		this.settlement = settlement;
	}

	public Settlement createSettlement(String name) {
		setName(name);
		settlement = new Settlement();
		return getSettlement();
	}

	public Settlement createSettlement() {
		return createSettlement(Utils.randWord());
	}

	public boolean hasLayer(int layer) {
		return layers.containsKey(layer);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@JsonIgnore
	public Map getLayer(int layer) {
		return layers.get(layer);
	}

	@JsonIgnore
	public Map getOverworld() {
		return layers.get(LAYER_OVERWORLD);
	}

	@JsonIgnore
	public int getLayerCount() {
		return layers.size();
	}

	@JsonIgnore
	public java.util.Map<Integer, Map> getLayers() {
		return layers;
	}

	@JsonIgnore
	public void setLayers(HashMap<Integer, Map> layers) {
		this.layers = layers;
	}

	@JsonIgnore
	public boolean isSettlement() {
		return settlement != null;
	}

	@JsonIgnore
	public boolean isPlayerOwnedSettlement() {
		if (!isSettlement())
			return false;
		return settlement.playerOwned;
	}

	public static Settlement.Status isSettlement(int wx, int wy) {
		var world = MadSand.world();
		var worldMap = world.getWorldMap();
		Location tmpLoc;
		var coords = new Pair(wx, wy);
		boolean locLoaded = false;
		Settlement.Status status;
		try {
			if (world.locExists(coords))
				tmpLoc = worldMap.getLocation(coords);
			else {
				tmpLoc = world.getMapSaver().loadLocationInfo(wx, wy);
				locLoaded = true;
			}

			if (tmpLoc.isPlayerOwnedSettlement())
				status = Status.PlayerOwned;
			else if (tmpLoc.isSettlement())
				status = Status.NpcOwned;
			else
				status = Status.DoesNotExist;

			if (locLoaded)
				worldMap.remove(coords);
			return status;
		} catch (Exception e) {
			e.printStackTrace();
			return Status.Unknown;
		}
	}

	@Override
	public String toString() {
		return String.format(
			"Location {%s} Settlement: %b",
			name, isSettlement()
		);
	}

	public enum Layer {
		Overworld(0), Dungeon(0, 30), Cave(30, LAYER_MAX);

		private final static Layer values[] = values();

		/* <base> is exclusive, end is inclusive */
		private int base, max;

		Layer(int base, int max) {
			this.base = base;
			this.max = max;
		}

		Layer(int base) {
			this(base, base);
		}

		public int base() {
			return base;
		}

		public int max() {
			return max;
		}

		public int count() {
			return max - base;
		}

		public static Layer byNumber(int layer) {
			for (Layer type : values)
				if (layer > type.base && layer <= type.max)
					return type;
			return null;
		}
	}
}
