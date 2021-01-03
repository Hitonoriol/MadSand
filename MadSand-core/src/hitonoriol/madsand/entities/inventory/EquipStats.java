package hitonoriol.madsand.entities.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.Stat;
import hitonoriol.madsand.entities.StatContainer;

public class EquipStats {
	public int lvl;

	public int rollMax, rollMin;

	public StatContainer stats = new StatContainer();
	public ItemType type;
	public Stat mainStat;

	private static float multiplier = 1.7f;

	public EquipStats(int lvl, ItemType type) {
		this.lvl = lvl;

		rollMax = (int) (lvl * multiplier);
		rollMin = lvl / 3;

		if (rollMin == 0)
			rollMin = 1;

		this.type = type;
		mainStat = getMainStat();

		rollMainStat();
		rollBonusStats();
	}

	public EquipStats(EquipStats eStats) {
		lvl = eStats.lvl;
		rollMax = eStats.rollMax;
		rollMin = eStats.rollMin;
		stats.set(eStats.stats);
	}

	public EquipStats() {
		this.lvl = 0;
	}

	@JsonIgnore
	private boolean isUnlucky() {
		return Utils.rand(0, lvl * 2) == lvl;
	}

	private int rollStatValue() {
		int ret = Utils.rand(rollMin, rollMax);
		float f = (isUnlucky()) ? -0.5f : 1;
		if (isUnlucky())
			f = 0;
		return (int) (ret * f);
	}

	private int rollRandomStat() {
		Stat stats[] = Stat.values();
		int value = rollStatValue();
		Stat stat = null;

		do
			stat = stats[Utils.rand(stats.length)];
		while (stat.equals(mainStat));

		this.stats.set(stat, value);
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

		stats.set(mainStat, value);
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
		int statValue;
		for (Stat stat : Stat.values())
			if ((statValue = stats.get(stat)) != 0)
				ret += stat.name() + " " + asStatString(statValue) + Resources.LINEBREAK;
		return ret;
	}

	@JsonIgnore
	public int getTotalBonus() {
		return stats.getSum();
	}
}
