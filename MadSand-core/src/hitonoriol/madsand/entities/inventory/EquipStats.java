package hitonoriol.madsand.entities.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;

public class EquipStats {
	public int lvl;

	public int rollMax, rollMin;

	public int constitution;
	public int dexterity;
	public int strength;
	public int accuracy;
	public int intelligence;
	public int defense;

	private static float multiplier = 2.1f;

	public EquipStats(int lvl) {
		this.lvl = lvl;
		rollMax = (int) (lvl * multiplier + 1);
		rollMin = lvl / 2 + 1;

		roll();
	}

	public EquipStats(EquipStats eStats) {
		lvl = eStats.lvl;
		rollMax = eStats.rollMax;
		rollMin = eStats.rollMin;
		constitution = eStats.constitution;
		dexterity = eStats.dexterity;
		strength = eStats.strength;
		accuracy = eStats.accuracy;
		intelligence = eStats.intelligence;
		defense = eStats.defense;
	}

	public EquipStats() {
		this.lvl = 0;
	}

	@JsonIgnore
	private boolean isUnlucky() { // with each item lvl the chance to roll a debuff halves; idk how good is that, we'll see
		return Utils.rand(0, lvl) == lvl;
	}

	private int rollStat() {
		int ret = Utils.rand(rollMin, rollMax);
		float f = (isUnlucky()) ? -0.5f : 1;
		if (isUnlucky())
			f = 0;
		return (int) (ret * f);
	}

	public EquipStats roll() {
		constitution = rollStat();
		dexterity = rollStat();
		strength = rollStat();
		accuracy = rollStat();
		intelligence = rollStat();
		defense = rollStat();

		return this;
	}

	private String POSITIVE_STAT_COLOR = "[GREEN]";
	private String NEGATIVE_STAT_COLOR = "[RED]";

	private String asStatString(int stat) {
		return ((stat > 0) ? (POSITIVE_STAT_COLOR + "+" + stat) : (NEGATIVE_STAT_COLOR + Utils.str(stat))) + "[]";
	}

	@JsonIgnore
	public String getString() {
		String ret = "";
		if (defense != 0)
			ret += "Defense " + asStatString(defense) + Resources.LINEBREAK;
		if (constitution != 0)
			ret += "Constitution " + asStatString(constitution) + Resources.LINEBREAK;
		if (dexterity != 0)
			ret += "Dexterity " + asStatString(dexterity) + Resources.LINEBREAK;
		if (strength != 0)
			ret += "Strength " + asStatString(strength) + Resources.LINEBREAK;
		if (accuracy != 0)
			ret += "Accuracy " + asStatString(accuracy) + Resources.LINEBREAK;
		if (intelligence != 0)
			ret += "Intelligence " + asStatString(intelligence) + Resources.LINEBREAK;
		return ret;
	}

	@JsonIgnore
	public int getTotalBonus() {
		return defense + constitution + dexterity + strength + accuracy + intelligence;
	}
}
