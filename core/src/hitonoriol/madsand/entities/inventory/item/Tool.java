package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.equipment.EquipSlot;
import hitonoriol.madsand.entities.inventory.item.category.ItemCategory;
import hitonoriol.madsand.entities.skill.Skill;
import hitonoriol.madsand.util.Utils;

public class Tool extends AbstractEquipment {
	public static int MIN_SKILL_DMG = 1;

	public int dmg;
	public Type type;
	public Skill skill = Skill.None;

	public Tool(Tool protoItem) {
		super(protoItem);
		dmg = protoItem.dmg;
		type = protoItem.type;
		skill = protoItem.skill;
	}

	public Tool() {
		super();
	}

	@Override
	public Tool copy() {
		return new Tool(this);
	}

	@Override
	public void use(Player player) {
		super.use(player);
		player.useItem(this);
	}

	// Item skill damage - roll amount of damage to do to objects
	// Min: 1
	public int getSkillDamage(Skill skill) {
		if (type != Type.Hammer && skill != this.skill)
			return MIN_SKILL_DMG;
		else
			return Utils.rand(MIN_SKILL_DMG, (int) (dmg * Skill.SKILL_DMG_COEF));
	}

	public static enum Type {
		None, // -- Bare hands 
		Axe, Shovel, Pickaxe, Hoe, Hammer,
		FishingRod;

		private static final Type[] values = values();

		public static Type bySkill(Skill skill) {
			for (Type type : values)
				if (type.isSkillCompatible(skill))
					return type;
			return None;
		}

		public boolean isSkillCompatible(Skill skill) {
			switch (this) {
			case None:
				return skill == Skill.Gathering || skill == Skill.Farming;
			case Axe:
				return skill == Skill.Woodcutting;
			case FishingRod:
				return skill == Skill.Fishing;
			case Hoe:
				return skill == Skill.Farming;
			case Pickaxe:
				return skill == Skill.Mining;
			case Shovel:
				return skill == Skill.Digging;
			default:
				break;
			}
			return skill == Skill.Gathering;
		}
	}

	@Override
	public EquipSlot getEquipSlot() {
		return EquipSlot.MainHand;
	}
	
	@Override
	public void initCategory() {
		setCategory(ItemCategory.Tools);
	}
}
