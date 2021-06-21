package hitonoriol.madsand.entities.inventory.item;

import com.fasterxml.jackson.annotation.JsonSetter;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.util.Utils;

public class Placeable extends Item {
	private Type type;
	private int altObject; // id of tile/object to be placed when this item is used
	private boolean directional = false;

	public Placeable(Placeable protoItem) {
		super(protoItem);
		altObject = protoItem.altObject;
		type = protoItem.type;
		directional = protoItem.directional;
	}

	public Placeable() {
		super();
	}

	@Override
	public Placeable copy() {
		return new Placeable(this);
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
	public void setAltObject(String value) {
		altObject = Utils.val(value);
		if (type == Type.Object)
			ObjectProp.getObject(altObject).setDropOnDestruction(id);
	}

	public boolean isDirectional() {
		return directional;
	}

	public Type getType() {
		return type;
	}

	public int getAltObject() {
		return altObject;
	}

	public static enum Type {
		Object, Tile
	}
}
