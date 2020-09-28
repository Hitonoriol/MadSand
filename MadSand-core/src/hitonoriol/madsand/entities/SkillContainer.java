package hitonoriol.madsand.entities;

import java.util.HashMap;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Tuple;
import hitonoriol.madsand.enums.Skill;
import hitonoriol.madsand.world.World;

public class SkillContainer extends HashMap<Skill, SkillValue> {
	final int BONUS_DENOMINATOR = 25; // skill lvl/this = bonus percent of something for some actions
	final int ITEM_BONUS_DENOMINATOR = 5; // level/this = quantity of bonus items from action
	public static HashMap<Skill, Tuple<Integer, Double>> reqList = new HashMap<Skill, Tuple<Integer, Double>>();

	public SkillContainer() {
		Skill skill;
		SkillValue skillVal;
		Tuple<Integer, Double> rs;
		Resources.loadSkillReqs();
		for (int i = 0; i < Skill.len(); ++i) {
			skill = Skill.get(i);
			skillVal = new SkillValue();

			if (reqList.containsKey(skill)) {
				rs = reqList.get(skill);
				skillVal = new SkillValue(rs.l, rs.r);
			} else
				skillVal = new SkillValue();

			super.put(skill, skillVal);
		}
	}

	public void check() {
		for (int i = 0; i < Skill.len(); ++i)
			check(Skill.get(i));
	}

	public boolean check(Skill skill, boolean verbose) {
		boolean lvup = get(skill).check();
		if (verbose && lvup) {
			MadSand.notice(skill.toString() + " increased!");

			if (skill.equals(Skill.Level)) {
				++World.player.stats.maxStatSum;
				Gui.overlay.levelUpDialog();
			}
		}
		return lvup;
	}

	public boolean check(Skill skill) {
		return check(skill, true);
	}

	public SkillValue put(Skill skill, SkillValue skillval) {
		if (containsKey(skill))
			remove(skill);
		super.put(skill, skillval);
		return skillval;
	}

	public boolean increaseSkill(Skill skill) {
		++get(skill).exp;
		return check(skill);
	}

	public boolean increaseSkill(Skill skill, int amt) {
		get(skill).exp += amt;
		return check(skill);
	}

	public int getLvl(Skill skill) {
		return get(skill).lvl;
	}

	public void setLvl(int lvl) {
		super.get(Skill.Level).lvl = lvl;
	}

	public int getLvl() {
		return getLvl(Skill.Level);
	}

	public int getExp(Skill skill) {
		return get(skill).exp;
	}

	public int getExp() {
		return getExp(Skill.Level);
	}

	public void setExp(Skill skill, int amt) {
		get(skill).exp = amt;
	}

	static final int REWARD_CHANCE = 40;

	// How many items to get from one roll
	public int getItemReward(Skill skill) {
		int rew = getLvl(skill) / ITEM_BONUS_DENOMINATOR;
		if (rew == 0)
			return 1;
		if (Utils.percentRoll(REWARD_CHANCE))
			return rew;
		else
			return 1;
	}

	// How many times to roll for drops
	public int getItemDropRolls(Skill skill) {
		if (Utils.percentRoll(getLvl(skill)))
			return Utils.rand(1, getLvl(skill));

		return 1;
	}

	// Is roll successful
	public boolean skillRoll(Skill skill) {
		int lvl = get(skill).lvl;

		if (lvl == 0)
			return false;

		return (Utils.rand(0, lvl) != lvl) && (Utils.rand(0, lvl) != lvl); // e.g. if lvl is 1, probability of
																			// successful roll is 1/4
	}

	// Currently used to determine amount of "skill-damage" done to objects on interaction 
	// Tools have "skill damage" too, this one and the ones of tools stack
	// Min: 0
	public int getBaseSkillDamage(Skill skill) {
		if (getLvl(skill) < 2)
			return 0;

		return skillRoll(skill) ? Utils.rand(1, getLvl(skill)) : 1;
	}

	public String getExpString(Skill skill) {
		SkillValue skillv = get(skill);
		return skillv.exp + "/" + skillv.requiredExp;
	}

	public String getLvlString(Skill skill) {
		SkillValue skillv = get(skill);
		return skillv.lvl + " (" + getExpString(skill) + ")";

	}
}
