package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.properties.TileProp;

public class Tile {
	int id;
	String name;

	public Tile(int id) {
		this.id = id;
		this.name = TileProp.name.get(id);
	}
}