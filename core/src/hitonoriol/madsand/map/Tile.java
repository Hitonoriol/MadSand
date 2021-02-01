package hitonoriol.madsand.map;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.inventory.item.Tool;
import hitonoriol.madsand.properties.TileProp;

public class Tile {
	@JsonIgnore
	public int id;

	public int damage;
	public String onInteract;
	public HashMap<Tool.Type, ArrayList<Integer>> altItems;
	public String name;
	public FishingSpot fishingSpot;

	public boolean foreground = false;

	@JsonIgnore
	public boolean visited = false; // has tile been seen before
	@JsonIgnore
	public boolean visible = true; // can tile currently be seen

	public Tile(int id) {
		final Tile tileProp = TileProp.getTileProp(id);

		this.id = id;
		this.name = tileProp.name;
		this.foreground = tileProp.foreground;
		this.damage = tileProp.damage;
		this.fishingSpot = tileProp.fishingSpot;
	}

	public Tile() {
		this.id = 0;
		this.onInteract = Resources.emptyField;
	}

	public int rollDrop(Tool.Type heldItem) {
		return MapObject.rollTileResource(id, heldItem);
	}

	public boolean hasFishingSpot() {
		return fishingSpot != null;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Tile))
			return false;
		if (obj == this)
			return true;

		Tile rhs = (Tile) obj;
		return new EqualsBuilder().append(id, rhs.id).isEquals();
	}
}