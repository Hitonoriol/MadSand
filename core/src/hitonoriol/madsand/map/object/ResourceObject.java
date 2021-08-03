package hitonoriol.madsand.map.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.PlayerStats;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.Tool;
import hitonoriol.madsand.entities.skill.Skill;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;
import me.xdrop.jrand.JRand;
import me.xdrop.jrand.generators.basics.FloatGenerator;

public class ResourceObject extends MapObject {
	public static float MIN_HP = 0.55f, MAX_HP = 1.75f;
	private static FloatGenerator hpRangeGen = JRand.flt().range(MIN_HP, MAX_HP);

	public Skill skill = Skill.None;
	public HashMap<Tool.Type, ArrayList<Integer>> altItems;

	public ResourceObject(ResourceObject protoObject) {
		super(protoObject);
		skill = protoObject.skill;
		altItems = protoObject.altItems;
		rollHp();
	}

	@Override
	public ResourceObject copy() {
		return new ResourceObject(this);
	}

	public ResourceObject() {}

	@Override
	public void interact(Player player) {
		Skill skill = getInteractionSkill();
		int curLvl = player.stats.skills.getLvl(skill);

		if (curLvl < lvl)
			MadSand.notice("You are not experienced enough." + Resources.LINEBREAK
					+ skill + " level required: " + lvl + Resources.LINEBREAK
					+ "Your " + skill + ": " + curLvl);
		else
			super.interact(player, () -> player.interact(this));
	}

	private void rollResources(Player player, int hpDelta) {
		PlayerStats stats = player.stats();
		Tool.Type heldTool = stats.getEquippedToolType();
		Skill skill = heldTool.isSkillCompatible(this.skill) ? this.skill : Skill.Gathering;
		Item objLoot;
		int rolls;

		for (int x = 0; x < hpDelta; ++x) {
			rolls = stats.skills.getItemDropRolls(skill);
			if (!stats.luckRoll() && !stats.skills.skillRoll(skill))
				rolls = 1;

			for (int i = 0; i < rolls; ++i) {
				objLoot = Item.create(rollDrop(heldTool), stats.skills.getItemReward(skill));
				player.addItem(objLoot);
			}

			player.increaseSkill(skill, Math.max(1, lvl));
		}
	}

	private float BASE_RES_FAIL = 40; // Base resource gathering fail probability

	@Override
	public int simulateHit(Player player) {
		if (Utils.percentRoll(BASE_RES_FAIL) && !player.stats.skills.skillRoll(skill) && !player.stats.luckRoll())
			return -1;

		return super.simulateHit(player, skill);
	}

	@Override
	public int acceptHit(Player player, Supplier<Integer> dmg) {
		PlayerStats stats = player.stats();
		int preHitHp = hp;
		int damage = super.acceptHit(player, dmg);

		if (damage == -1)
			MadSand.print("You fail to interact with " + name);

		else if (rollDrop(stats.getEquippedToolType()) != -1 && preHitHp != hp)
			rollResources(player, Math.abs(preHitHp - hp));

		return damage;
	}

	@Override
	public int acceptHit(Player player) {
		return super.acceptHit(player, () -> simulateHit(player));
	}

	private void rollHp() {
		hp = maxHp = (int) Math.max(maxHp * hpRangeGen.gen(), 1f);
	}

	public int rollDrop(Tool.Type heldItemType) {
		if (heldItemType == Tool.Type.Hammer)
			return -1;

		return rollResource(id, heldItemType, altItems);
	}

}
