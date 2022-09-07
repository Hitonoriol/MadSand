package hitonoriol.madsand.entities.skill;

import static hitonoriol.madsand.resources.Resources.loader;

import java.util.HashMap;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.dialogs.LevelupDialog;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;

public class SkillContainer extends HashMap<Skill, SkillValue> {
	private static HashMap<Skill, SkillValue> reqList = loader().loadMap(Resources.SKILL_FILE, Skill.class,
			SkillValue.class);
	public final static int MAX_SKILL_LVL = 100; // max effective lvl (possible to get higher lvls, but lvl bonuses won't increase further than this)
	private final static SkillContainer maxSkills = new SkillContainer();
	static {
		maxSkills.forEach((skill, skillValue) -> {
			while (skillValue.lvl < MAX_SKILL_LVL) {
				skillValue.addExp(skillValue.expToNextLvl());
				skillValue.check();
			}
			Utils.dbg("Max %s: [%s]", skill, skillValue);
		});
	}

	final float ITEM_BONUS_COEF = 0.35f; // level*this = quantity of bonus items from action

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
			MadSand.notice(skill + " increased!");

			if (skill.equals(Skill.Level)) {
				MadSand.player().stats.baseStats.increaseMaxStatSum();
				new LevelupDialog().show();
			} else
				Gui.drawOkDialog(skill + " up!", "Your " + skill + " skill is now level " + get(skill).lvl + "!");
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
		return increaseSkill(skill, 1);
	}

	public boolean increaseSkill(Skill skill, double amt) {
		if (skill == Skill.None)
			return false;

		get(skill).addExp(amt);
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
		return (int) get(skill).exp;
	}

	public int getExp() {
		return getExp(Skill.Level);
	}

	public void setExp(Skill skill, int amt) {
		get(skill).exp = amt;
	}

	// How many items to get from one roll
	public int getItemReward(Skill skill) {
		int reward = (int) (getLvl(skill) * ITEM_BONUS_COEF);

		if (reward == 0)
			return 1;

		if (Utils.percentRoll(getSkillEffect(skill)))
			return Utils.rand(1, reward);
		else
			return 1;
	}

	// How many times to roll for drops
	public int getItemDropRolls(Skill skill) {
		int lvl = getLvl(skill);

		if (lvl < 2)
			return 1;

		if (Utils.percentRoll(getSkillEffect(skill)))
			return Utils.rand(1, getLvl(skill));

		return 1;
	}

	// Is roll with probability getSkillRollPercent() successful
	public boolean skillRoll(Skill skill) {
		return Utils.percentRoll(getSkillEffect(skill));
	}

	// Skill effectiveness % [0; 1]
	public double getSkillEffectPercent(Skill skill) {
		return Math.min(1, getSkillEffect(skill) / maxSkillEffect(skill));
	}

	// Skill effectiveness in range [0, MAX_SKILL_EFFECT], depends on level 
	public double getSkillEffect(Skill skill) {
		return skillLvlEffect((double) getLvl(skill)) + calcSkillBonusEffect(skill);
	}

	public double calcSkillBonusEffect(Skill skill) {
		double bonusEffect = 0;
		if (skill == Skill.Survival)
			bonusEffect = 7;

		else if (skill == Skill.None)
			return 5;

		bonusEffect += Math.pow(getExp(skill) + SkillValue.DEFAULT_REQUIRED_EXP, 0.25) * 0.85;
		return bonusEffect;
	}

	// Currently used to determine amount of "skill-damage" done to objects on interaction 
	// Tools have "skill damage" too, this one and the ones of tools stack
	// Min: 0
	public int getBaseSkillDamage(Skill skill) {
		if (getLvl(skill) < 2)
			return 0;

		return skillRoll(skill) ? Utils.rand(1, getLvl(skill)) : 0;
	}

	public static double skillLvlEffect(double skillLvl) {
		if (skillLvl >= MAX_SKILL_LVL)
			skillLvl = MAX_SKILL_LVL;

		return Math.sqrt(calcSkillEffect(Math.pow(skillLvl + 0.75, 1.425)) * 6.5 + Math.sqrt(skillLvl + 2)) * 0.75;
	}

	private static double calcSkillEffect(double val) {
		return 29032780 + (3.617571 - 29032780) / (1 + Math.pow(val / 4810264, 1.12112));
	}

	public static double maxSkillEffect(Skill skill) {
		return maxSkills.getSkillEffect(skill);
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
