package ru.bernarder.fallenrisefromdust;

public class MapID {
	Pair worldxy;
	int layer, id;

	public MapID(Pair coords, int layer, int id) {
		this.worldxy = coords;
		this.layer = layer;
		this.id = id;
	}
}
