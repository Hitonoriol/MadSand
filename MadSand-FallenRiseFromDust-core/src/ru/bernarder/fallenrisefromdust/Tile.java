package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.properties.TileProp;

public class Tile {
	int id;
	String name;
	boolean foreground = false;

	public Tile(int id) {
		this.id = id;
		this.name = TileProp.name.get(id);

		if (TileProp.cover.getOrDefault(id, -1) != -1)
			foreground = true;
	}

	public Tile() {
		this(0);
	}
}