package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.strings.Tiles;

public class Tile {
	int id;
	String name;

	public Tile(int id) {
		this.id = id;
		this.name = Tiles.name.get(id);
	}
}