package hitonoriol.madsand.entities.inventory.item;

import com.fasterxml.jackson.annotation.JsonSetter;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.item.category.ItemCategory;

public class PlaceableItem extends Item {
	private Type type;
	private int altObject; // id of tile/object to be placed when this item is used
	private boolean directional = false;

	public PlaceableItem(PlaceableItem protoItem) {
		super(protoItem);
		altObject = protoItem.altObject;
		type = protoItem.type;
		directional = protoItem.directional;
	}

	public PlaceableItem() {
		super();
	}

	@Override
	public PlaceableItem copy() {
		return new PlaceableItem(this);
	}

	@Override
	public void use(Player player) {
		player.useItem(this);
	}

	@Override
	public void leftClickAction() {
		toggleEquipped();
	}

	@JsonSetter("altObject")
	public void setAltObject(int value) {
		altObject = value;
	}

	public boolean isDirectional() {
		return directional;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getAltObject() {
		return altObject;
	}
	
	@Override
	public void initCategory() {
		setCategory(ItemCategory.Building);
	}

	public static enum Type {
		Object, Tile
	}
}
