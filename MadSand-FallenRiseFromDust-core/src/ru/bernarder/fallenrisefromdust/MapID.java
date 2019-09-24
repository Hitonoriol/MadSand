package ru.bernarder.fallenrisefromdust;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class MapID {
	Pair worldxy;
	int layer, id;

	public MapID(Pair coords, int layer, int id) {
		this.worldxy = coords;
		this.layer = layer;
		this.id = id;
	}

	public MapID(Pair coords, int layer) {
		this.worldxy = coords;
		this.layer = layer;
		this.id = 0;
	}

	public MapID() {
		this(new Pair(0, 0), 0);
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
