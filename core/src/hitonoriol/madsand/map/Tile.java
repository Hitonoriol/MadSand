package hitonoriol.madsand.map;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.entities.inventory.ItemUI;
import hitonoriol.madsand.entities.inventory.item.PlaceableItem;
import hitonoriol.madsand.entities.inventory.item.Tool;
import hitonoriol.madsand.gamecontent.Textures;
import hitonoriol.madsand.gamecontent.Tiles;
import hitonoriol.madsand.gfx.TextureProcessor;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.resources.Resources;

public class Tile implements Placeable {
	@JsonIgnore
	private int id;

	public int damage;
	public String onInteract;
	public HashMap<Tool.Type, ArrayList<Integer>> altItems;
	public String name;
	public FishingSpot fishingSpot;

	public boolean foreground = false;

	@JsonIgnore
	public boolean visited = false; // has tile been seen before

	/*
	 * [-inf; 0] -- fully visible
	 * [1; +inf] -- darkened using LightMap in GameWorldRenderer, functionally not visible
	 */
	private int lightLevel = 0;

	public Tile(int id) {
		final var tileProp = Tiles.all().get(id);

		this.id = id;
		name = tileProp.name;
		foreground = tileProp.foreground;
		damage = tileProp.damage;
		fishingSpot = tileProp.fishingSpot;
	}

	public Tile() {
		id = 0;
		onInteract = Resources.emptyField;
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String name() {
		return name;
	}

	public int rollDrop(Tool.Type heldItem) {
		return MapObject.rollTileResource(id, heldItem);
	}

	public boolean hasFishingSpot() {
		return fishingSpot != null;
	}

	public boolean visible() {
		return lightLevel <= 0;
	}

	public void setVisible() {
		lightLevel = 0;
	}

	public void setLightLevel(int lightLevel) {
		this.lightLevel = lightLevel;
	}

	public void addLight(int lightLevel) {
		this.lightLevel += lightLevel;
	}

	public int getLightLevel() {
		return lightLevel;
	}

	@Override
	public void createPlaceable(PlaceableItem item) {
		createPlaceable(item, () -> {
			item.name = name;
			item.setType(PlaceableItem.Type.Tile);
		});
	}

	@Override
	public TextureRegion createPlaceableTexture() {
		return new TextureRegion(
			new Texture(
				TextureProcessor.extractPixmap(Textures.getTile(id), ItemUI.SIZE)
			)
		);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Tile))
			return false;
		if (obj == this)
			return true;

		var rhs = (Tile) obj;
		return new EqualsBuilder().append(id, rhs.id).isEquals();
	}
}