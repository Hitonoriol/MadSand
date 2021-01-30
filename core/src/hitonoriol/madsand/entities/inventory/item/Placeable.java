package hitonoriol.madsand.entities.inventory.item;

public class Placeable extends Item {

	public int altObject;
	
	public Placeable(Placeable protoItem) {
		super(protoItem);
		altObject = protoItem.altObject;
	}
	
	@Override
	public Placeable copy() {
		return new Placeable(this);
	}
	
	public static enum Type {
		Object, Tile
	}
}
