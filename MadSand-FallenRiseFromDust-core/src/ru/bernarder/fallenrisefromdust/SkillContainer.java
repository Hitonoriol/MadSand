package ru.bernarder.fallenrisefromdust;

import java.util.HashMap;

import ru.bernarder.fallenrisefromdust.enums.Skill;

public class SkillContainer extends HashMap<Skill, SkillValue> {
	final int BONUS_DENOMINATOR = 25; // skill lvl/this = bonus percent of something for some actions
	final int ITEM_BONUS_DENOMINATOR = 5; // level/this = quantity of bonus items from action
	static HashMap<Skill, Tuple<Integer, Double>> reqList = new HashMap<Skill, Tuple<Integer, Double>>();

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
		if (verbose && lvup)
			MadSand.print(skill.toString() + " increased!");
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

	public int getLvl(Skill skill) {
		return get(skill).lvl;
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

	static final int REWARD_CHANCE = 10;

	public int getItemReward(Skill skill) {
		int rew = get(skill).lvl / ITEM_BONUS_DENOMINATOR;
		if (rew == 0)
			return 1;
		if (Utils.random.nextInt(REWARD_CHANCE) == Utils.random.nextInt(REWARD_CHANCE))
			return rew;
		else
			return 1;
	}

	public int getLvlReward(Skill skill, int baseVal) {
		return (get(skill).lvl / BONUS_DENOMINATOR) * baseVal;
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
