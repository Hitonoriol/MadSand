package ru.bernarder.fallenrisefromdust;

import java.util.HashMap;

import ru.bernarder.fallenrisefromdust.enums.Skill;

public class SkillContainer extends HashMap<Skill, SkillValue> {
	final int BONUS_DENOMINATOR = 100;
	static HashMap<Skill, Tuple<Integer, Double>> reqList = new HashMap<Skill, Tuple<Integer, Double>>();

	public SkillContainer() {
		Skill skill;
		SkillValue skillVal;
		Tuple<Integer, Double> rs;
		Utils.loadSkillReqs();
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
