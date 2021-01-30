package hitonoriol.madsand.entities.inventory.item;

public class Armor extends CombatEquipment {

	public Type type;

	public Armor(Armor protoItem) {
		super(protoItem);
		type = protoItem.type;
	}

	@Override
	public Armor copy() {
		return new Armor(this);
	}

	public static enum Type {
		HeadArmor, ChestArmor, LegArmor, FootArmor, Shield
	}
}
