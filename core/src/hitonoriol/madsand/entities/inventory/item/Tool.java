package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.EquipSlot;
import hitonoriol.madsand.entities.Skill;

public class Tool extends AbstractEquipment {
	public int dmg;
	public Skill skill = Skill.None;

	public Tool(Tool protoItem) {
		super(protoItem);
		dmg = protoItem.dmg;
		skill = protoItem.skill;
	}
	
	public Tool() {
		super();
	}

	// Item skill damage - roll amount of damage to do to objects
	// Min: 1
	public int getSkillDamage(Skill skill) {
		if (skill != this.skill)
			return 1;
		else
			return Utils.rand(1, dmg / Skill.SKILL_DMG_DENOMINATOR);
	}

	public boolean damageTool(Skill skill) {
		return damage();
	}

	public boolean damageTool() {
		return damageTool(Skill.None);
	}

	public static enum Type {
		Axe, Shovel, Pickaxe, Hoe, Hammer
	}

	@Override
	public EquipSlot getEquipSlot() {
		return EquipSlot.MainHand;
	}
}
