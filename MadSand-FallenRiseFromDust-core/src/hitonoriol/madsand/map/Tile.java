package hitonoriol.madsand.map;

import java.util.HashMap;
import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.properties.TileProp;

public class Tile {
	@JsonIgnore
	public int id;

	public int damage;
	public String onInteract;
	public HashMap<Integer, Vector<Integer>> altItems;
	public String name;

	public boolean foreground = false;

	@JsonIgnore
	public boolean visited = false;
	@JsonIgnore
	public boolean visible = true;

	public Tile(int id) {
		final Tile tileProp = TileProp.getTileProp(id);

		this.id = id;
		this.name = tileProp.name;
		this.foreground = tileProp.foreground;
		this.damage = tileProp.damage;
	}

	public Tile() {
		this.id = 0;
		this.onInteract = Resources.emptyField;
	}
}