package hitonoriol.madsand.entities.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.enums.ItemType;
import hitonoriol.madsand.enums.Stat;

public class EquipStats {
	public int lvl;

	public int rollMax, rollMin;

	public int constitution;
	public int dexterity;
	public int strength;
	public int accuracy;
	public int intelligence;
	public int defense;
	
	public ItemType type;
	public Stat mainStat;

	private static float multiplier = 2.1f;

	public EquipStats(int lvl, ItemType type) {
		this.lvl = lvl;
		
		rollMax = (int) (lvl * multiplier + 1);
		rollMin = lvl / 2 + 1;
		
		this.type = type;
		mainStat = getMainStat();

		rollMainStat();
		rollBonusStats();
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

	private int rollStatValue() {
		int ret = Utils.rand(rollMin, rollMax);
		float f = (isUnlucky()) ? -0.5f : 1;
		if (isUnlucky())
			f = 0;
		return (int) (ret * f);
	}

	private void setStat(Stat stat, int value) {
		switch (stat) {
		case Accuracy:
			accuracy = value;
			break;
		case Constitution:
			constitution = value;
			break;
		case Defense:
			defense = value;
			break;
		case Dexterity:
			dexterity = value;
			break;
		case Intelligence:
			intelligence = value;
			break;
		case Strength:
			strength = value;
			break;
		default:
			break;
		}
	}

	private int rollRandomStat() {
		Stat stats[] = Stat.values();
		int value = rollStatValue();
		Stat stat = null;
		
		do
			stat = stats[Utils.rand(stats.length)];
		while(stat.equals(mainStat));
		
		setStat(stat, value);
		return value;
	}

	@JsonIgnore
	private Stat getMainStat() {
		if (type.isWeapon())
			return Stat.Strength;
		else if (type.isArmor())
			return Stat.Defense;
		else
			return null;
	}

	private void rollMainStat() {
		int value = 0;

		do
			value = rollStatValue();
		while (value == 0);

		setStat(mainStat, value);
	}

	public EquipStats rollBonusStats() {
		int absSum = 0;
		for (int i = 1; i < lvl && absSum == 0; ++i)
			absSum += Math.abs(rollRandomStat());

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
