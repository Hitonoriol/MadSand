package hitonoriol.madsand.entities.inventory.item;

import com.fasterxml.jackson.annotation.JsonSetter;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.Player;

public class Placeable extends Item {

	public Type type;
	public int altObject;

	public Placeable(Placeable protoItem) {
		super(protoItem);
		altObject = protoItem.altObject;
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
	
	@JsonSetter("altObject")
	public void setAltObject(String value) {
		altObject = Utils.val(value);
		// Set drop-on-destruction item for placeable Objects (?)
	}

	public static enum Type {
		Object, Tile
	}
}
