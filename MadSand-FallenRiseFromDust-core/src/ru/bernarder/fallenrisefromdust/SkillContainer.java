package ru.bernarder.fallenrisefromdust;

import java.util.HashMap;

import ru.bernarder.fallenrisefromdust.enums.Skill;

public class SkillContainer extends HashMap<Skill, SkillValue> {
	static int LEVEL_REQ_EXP = 100;
	static double LEVEL_MULTIPLIER = 0.9;

	public SkillContainer() {
		Skill skill;
		SkillValue skillVal;
		for (int i = 0; i < Skill.len(); ++i) {
			skill = Skill.get(i);
			if (skill == Skill.Level)
				skillVal = new SkillValue(LEVEL_REQ_EXP, LEVEL_MULTIPLIER);
			else
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

	public String getExpString(Skill skill) {
		SkillValue skillv = get(skill);
		return skillv.exp + "/" + skillv.requiredExp;
	}

	public String getLvlString(Skill skill) {
		SkillValue skillv = get(skill);
		return skillv.lvl + " (" + getExpString(skill) + ")";

	}
}
