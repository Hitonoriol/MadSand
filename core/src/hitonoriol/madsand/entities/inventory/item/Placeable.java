package hitonoriol.madsand.entities.inventory.item;

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

	public static enum Type {
		Object, Tile
	}
}
