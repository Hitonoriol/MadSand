package hitonoriol.madsand.entities.inventory.item;

public abstract class LevelBoundItem extends Item {
	public int lvl;

	public LevelBoundItem(LevelBoundItem protoItem) {
		super(protoItem);
	}

	@Override
	public String getFullName() {
		return super.getFullName() + " [ORANGE][[Lvl " + lvl + "][]";
	}
}
