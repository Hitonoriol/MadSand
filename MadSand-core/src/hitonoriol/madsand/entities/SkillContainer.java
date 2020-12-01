package hitonoriol.madsand.entities;

import java.util.HashMap;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.enums.Skill;
import hitonoriol.madsand.world.World;

public class SkillContainer extends HashMap<Skill, SkillValue> {
	public final static int MAX_SKILL_LVL = 100; // max effective lvl (possible to get higher lvls, but lvl bonuses won't increase further than this)
	public final static double MAX_SKILL_ROLL_PERCENT = magicFormula(MAX_SKILL_LVL);

	final int BONUS_DENOMINATOR = 25; // skill lvl/this = bonus percent of something for some actions
	final int ITEM_BONUS_DENOMINATOR = 5; // level/this = quantity of bonus items from action
	public static HashMap<Skill, SkillValue> reqList = new HashMap<Skill, SkillValue>();

	public SkillContainer() {
		Skill skill;
		SkillValue skillVal;
		SkillValue rs;
		for (int i = 0; i < Skill.values().length; ++i) {
			skill = Skill.values()[i];
			skillVal = new SkillValue();

			if (reqList.containsKey(skill)) {
				rs = reqList.get(skill);
				skillVal = new SkillValue(rs.requiredExp, rs.lvUpMultiplier);
			} else
				skillVal = new SkillValue();

			super.put(skill, skillVal);
		}
	}

	public void check() {
		for (Skill skill : Skill.values())
			check(skill);
	}

	public boolean check(Skill skill, boolean verbose) {
		boolean lvup = get(skill).check();
		if (verbose && lvup) {
			MadSand.notice(skill.toString() + " increased!");

			if (skill.equals(Skill.Level)) {
				World.player.stats.baseStats.increaseMaxStatSum();
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

	// How many items to get from one roll
	public int getItemReward(Skill skill) {
		int rew = getLvl(skill) / ITEM_BONUS_DENOMINATOR;

		if (rew == 0)
			return 1;

		if (Utils.percentRoll(getSkillRollPercent(skill)))
			return rew;
		else
			return 1;
	}

	// How many times to roll for drops
	public int getItemDropRolls(Skill skill) {
		int lvl = getLvl(skill);

		if (lvl < 2)
			return 1;

		if (Utils.percentRoll(getSkillRollPercent(skill)))
			return Utils.rand(1, getLvl(skill));

		return 1;
	}

	// Is roll with probability getSkillRollPercent() successful
	public boolean skillRoll(Skill skill) {
		return Utils.percentRoll(getSkillRollPercent(skill));
	}

	// Get roll percent for Skill
	public double getSkillRollPercent(Skill skill) {
		double additionalPercent = 0;
		if (skill == Skill.Survival)
			additionalPercent = 6.5f;
		else if (skill.isResourceSkill())
			additionalPercent = 35;
		else if (skill == Skill.None)
			return 100;
		return magicFormula((double) getLvl(skill)) + additionalPercent;
	}

	// Currently used to determine amount of "skill-damage" done to objects on interaction 
	// Tools have "skill damage" too, this one and the ones of tools stack
	// Min: 0
	public int getBaseSkillDamage(Skill skill) {
		if (getLvl(skill) < 2)
			return 0;

		return skillRoll(skill) ? Utils.rand(1, getLvl(skill)) : 0;
	}

	public static double magicFormula(double skillLvl) {
		if (skillLvl < 1)
			return 1;

		if (skillLvl < 2)
			skillLvl += 0.4;

		if (skillLvl >= MAX_SKILL_LVL)
			return skillLvl = MAX_SKILL_LVL;

		return Math.log(Math.pow(skillLvl + 0.05, 22.0)) * 0.6; // Is capped at ~64%(MAX_SKILL_ROLL_PERCENT) when lvl == 100, & increases smoothly
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
