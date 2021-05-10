package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.entities.Stat;
import hitonoriol.madsand.entities.equipment.EquipSlot;

public class Weapon extends CombatEquipment {

	public Type type;

	public Weapon(Weapon protoItem) {
		super(protoItem);
		type = protoItem.type;
		initEquipStats(protoItem.equipStats);
	}

	public Weapon() {
		super();
	}

	@Override
	public Weapon copy() {
		return new Weapon(this);
	}

	@Override
	public EquipSlot getEquipSlot() {
		if (type == Type.OffhandWeapon)
			return EquipSlot.Offhand;

		return EquipSlot.MainHand;
	}

	@Override
	public Stat getMainStat() {
		switch (type) {
		case MeleeWeapon:
			return Stat.Strength;
		case OffhandWeapon:
			return Stat.Dexterity;
		case RangedWeapon:
			return Stat.Accuracy;
		}

		return null;
	}

	public static enum Type {
		MeleeWeapon, OffhandWeapon, RangedWeapon
	}
}
