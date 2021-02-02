package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.entities.EquipSlot;
import hitonoriol.madsand.entities.Stat;

public class Weapon extends CombatEquipment {

	public Type type;

	public Weapon(Weapon protoItem) {
		super(protoItem);
		type = protoItem.type;
		if (lvl == protoItem.lvl)
			equipStats.stats.set(Stat.Strength, protoItem.equipStats.stats);
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
