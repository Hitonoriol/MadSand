package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.entities.EquipSlot;
import hitonoriol.madsand.entities.Stat;

public class Armor extends CombatEquipment {

	public Type type;

	public Armor(Armor protoItem) {
		super(protoItem);
		type = protoItem.type;
	}

	public Armor() {
		super();
	}

	@Override
	public Armor copy() {
		return new Armor(this);
	}

	@Override
	public Stat getMainStat() {
		return Stat.Defense;
	}

	@Override
	public EquipSlot getEquipSlot() {
		switch (type) {
		case HeadArmor:
			return EquipSlot.Head;

		case ChestArmor:
			return EquipSlot.Chest;

		case LegArmor:
			return EquipSlot.Legs;

		case FootArmor:
			return EquipSlot.Feet;

		case Shield:
			return EquipSlot.Offhand;
		}
		return null;
	}

	public static enum Type {
		HeadArmor, ChestArmor, LegArmor, FootArmor, Shield
	}
}
