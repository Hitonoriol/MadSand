package ru.bernarder.fallenrisefromdust.map;

import ru.bernarder.fallenrisefromdust.properties.TileProp;

public class Tile {
	public int id;
	String name;
	
	public boolean foreground = false;
	
	public boolean known = false;
	public boolean visible = true;

	public Tile(int id) {
		this.id = id;
		this.name = TileProp.name.get(id);
		this.foreground = (TileProp.cover.getOrDefault(id, -1) != -1);
	}

	public Tile() {
		this(0);
	}
}