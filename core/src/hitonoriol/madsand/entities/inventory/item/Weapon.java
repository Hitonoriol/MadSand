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
	public EquipSlot getEquipSlot() {
		if (type == Type.OffhandWeapon)
			return EquipSlot.Offhand;

		return EquipSlot.MainHand;
	}

	public static enum Type {
		MeleeWeapon, OffhandWeapon, RangedWeapon
	}
}
