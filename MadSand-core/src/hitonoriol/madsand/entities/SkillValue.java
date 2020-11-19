package hitonoriol.madsand.entities;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class SkillValue {
	final static double DEFAULT_MULTIPLIER = 0.3;
	final static int DEFAULT_REQUIRED_EXP = 30;
	final static int DEFAULT_MAX_LEVEL = 100;

	public int lvl, exp, requiredExp;
	public double lvUpMultiplier;

	public SkillValue(int lvl, int exp, int requiredExp, double lvUpMultiplier) {
		this.lvl = lvl;
		this.exp = exp;
		this.requiredExp = requiredExp;
		this.lvUpMultiplier = lvUpMultiplier;
	}

	public SkillValue(int requiredExp, double multiplier) {
		this(0, 0, requiredExp, multiplier);
	}

	public SkillValue(int requiredExp) {
		this(0, 0, requiredExp, DEFAULT_MULTIPLIER);
	}

	public SkillValue() {
		this(0, 0, DEFAULT_REQUIRED_EXP, DEFAULT_MULTIPLIER);
	}

	public boolean check() {
		if (exp >= requiredExp) {
			++lvl;
			requiredExp += requiredExp * lvUpMultiplier;
			exp = 0;
			return true;
		}
		return false;
	}
}
