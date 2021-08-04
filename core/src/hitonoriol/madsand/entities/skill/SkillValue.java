package hitonoriol.madsand.entities.skill;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class SkillValue {
	final static double DEFAULT_MULTIPLIER = 0.031;
	final static int DEFAULT_REQUIRED_EXP = 30;
	final static int DEFAULT_MAX_LEVEL = 100;

	public int lvl;
	public double exp, lvlStartExp, requiredExp;
	public double lvUpMultiplier;

	public SkillValue(int lvl, double exp, double requiredExp, double lvUpMultiplier) {
		this.lvl = lvl;
		this.exp = exp;
		this.requiredExp = requiredExp;
		this.lvUpMultiplier = lvUpMultiplier > 0 ? lvUpMultiplier : DEFAULT_MULTIPLIER;
	}

	public SkillValue(double requiredExp, double multiplier) {
		this(0, 0, requiredExp, multiplier);
	}

	public SkillValue(double requiredExp) {
		this(0, 0, requiredExp, DEFAULT_MULTIPLIER);
	}

	public SkillValue() {
		this(0, 0, DEFAULT_REQUIRED_EXP, DEFAULT_MULTIPLIER);
	}

	public double expToNextLvl() {
		return requiredExp - exp;
	}

	public boolean check() {
		if (exp >= requiredExp) {
			++lvl;
			double prevReqExp = requiredExp - lvlStartExp;
			lvlStartExp = requiredExp;
			requiredExp += prevReqExp * (1 + lvUpMultiplier);
			return true;
		}
		return false;
	}

	public float getProgress() {
		return (float) (((exp - lvlStartExp) / (requiredExp - lvlStartExp)) * 100);
	}

	public void addExp(double amt) {
		exp += amt;
	}

	@Override
	public String toString() {
		return String.format("Lvl. %d lvlStart=%f (%f/%f) [%f%%]", lvl, lvlStartExp, exp, requiredExp, getProgress());
	}
}
