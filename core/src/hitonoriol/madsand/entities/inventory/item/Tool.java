package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.entities.EquipSlot;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.Skill;
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

	public boolean damageTool(Skill skill) {
		return damage();
	}

	public boolean damageTool() {
		return damageTool(Skill.None);
	}

	public static enum Type {
		None, // -- Bare hands 
		Axe, Shovel, Pickaxe, Hoe, Hammer,
		FishingRod;

		public static Type bySkill(Skill skill) {
			switch (skill) {

			case Farming:
				return Hoe;

			case Digging:
				return Shovel;

			case Mining:
				return Pickaxe;

			case Woodcutting:
				return Axe;

			default:
				return None;

			}
		}
	}

	@Override
	public EquipSlot getEquipSlot() {
		return EquipSlot.MainHand;
	}
}
