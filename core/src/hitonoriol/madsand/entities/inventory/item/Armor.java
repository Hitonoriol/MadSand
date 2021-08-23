package hitonoriol.madsand.entities.inventory.item;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.entities.Stat;
import hitonoriol.madsand.entities.equipment.EquipSlot;
import hitonoriol.madsand.entities.inventory.item.category.ItemCategory;

public class Armor extends CombatEquipment {

	public Type type;

	public Armor(Armor protoItem) {
		super(protoItem);
		type = protoItem.type;
		initEquipStats(protoItem.equipStats);
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
	@JsonIgnore
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
	
	@Override
	protected ItemCategory combatCategory() {
		return ItemCategory.Armor;
	}

	public static enum Type {
		HeadArmor, ChestArmor, LegArmor, FootArmor, Shield
	}
}
