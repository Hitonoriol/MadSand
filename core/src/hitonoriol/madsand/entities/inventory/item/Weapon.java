package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.entities.Stat;

public class Weapon extends CombatEquipment {

	Type type;

	public Weapon(Weapon protoItem) {
		super(protoItem);
		type = protoItem.type;
		if (lvl == protoItem.lvl)
			equipStats.stats.set(Stat.Strength, protoItem.equipStats.stats);
	}

	public static enum Type {
		MeleeWeapon, OffhandWeapon, RangedWeapon
	}
}
