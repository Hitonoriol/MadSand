package hitonoriol.madsand.world;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import hitonoriol.madsand.containers.Pair;

public class MapID {
	public Pair worldxy = new Pair();
	public int layer, id;

	public MapID(Pair coords, int layer, int id, boolean copyPair) {
		if (copyPair) {
			this.worldxy.x = coords.x;
			this.worldxy.y = coords.y;
		} else
			this.worldxy = coords;
		
		this.layer = layer;
		this.id = id;
	}
	
	public MapID(Pair coords, int layer, int id) {
		this(coords, layer, id, false);
	}

	public MapID(Pair coords, int layer) {
		this(coords, layer, 0);
	}

	public MapID(Pair coords) {
		this(coords, 0);
	}

	public MapID(MapID mapId) {
		this(mapId.worldxy, mapId.layer, mapId.id);
	}

	public MapID() {
		this(Pair.nullPair);
	}

	public MapID setLayer(int layer) {
		this.layer = layer;
		return this;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(67, 139).append(worldxy.x).append(worldxy.y).append(layer).append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MapID))
			return false;
		if (obj == this)
			return true;

		MapID rhs = (MapID) obj;
		return new EqualsBuilder().append(worldxy.x, rhs.worldxy.x).append(worldxy.y, rhs.worldxy.y)
				.append(layer, rhs.layer).append(id, rhs.id).isEquals();
	}
}
