package hitonoriol.madsand.entities;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.util.Utils;

public class BaseStats extends HashMap<Stat, Integer> {
	public static final int MAX_LVL = 35;
	private static final int RAND_MAX = 8, RAND_MIN = 1;
	private final static int ROLLABLE_STATS = Stat.rollableStats.size();
	private static final int DEF_MAX_SUM = 15;

	public int maxStatSum = DEF_MAX_SUM;

	public BaseStats() {
		super();
	}

	public BaseStats(BaseStats stats) {
		set(stats);
	}

	public int get(Stat stat) {
		return super.getOrDefault(stat, 0);
	}

	public void set(Stat stat, int value) {
		super.put(stat, value);
	}

	public void set(BaseStats stats) {
		for (Stat stat : Stat.values())
			set(stat, stats.get(stat));
	}

	// Copy specified stat from stat container
	public void set(Stat stat, BaseStats stats) {
		set(stat, stats.get(stat));
	}

	public void add(BaseStats statBonus) {
		for (Stat stat : Stat.values())
			increase(stat, statBonus.get(stat));
	}

	public void sub(BaseStats statBonus) {
		for (Stat stat : Stat.values())
			increase(stat, -statBonus.get(stat));
	}

	public int increaseMaxSum(int by) {
		maxStatSum += by;
		return maxStatSum;
	}

	public int increaseMaxStatSum() {
		return increaseMaxSum(1);
	}

	public void increase(Stat stat, int by) {
		set(stat, get(stat) + by);
	}

	public void increase(Stat stat) {
		increase(stat, 1);
	}

	public void decrease(Stat stat) {
		increase(stat, -1);
	}

	@JsonIgnore
	public int getSum() {
		int sum = 0;
		for (Stat stat : Stat.values())
			if (!stat.excludeFromSum())
				sum += get(stat);
		return sum;
	}

	public int getMaxSum() {
		return maxStatSum;
	}

	public int getMinSum() {
		return maxStatSum - 1;
	}

	@JsonIgnore
	public int getFreePoints() {
		return maxStatSum - getSum();
	}

	private static double getEffectiveness(Stat stat, double lvl) {
		switch (stat) {
		case Luck:
			return 0.034459925419825316 * Math.pow(lvl, 2)
					+ 1.2751306598545928 * lvl
					+ 0.6652992420539262;

		case Accuracy:
			return 0.057563520795230305 * Math.pow(lvl, 2)
					+ 0.11036796858541652 * lvl
					+ 25.60873585146606;

		default:
			return (lvl / MAX_LVL) * 100;
		}
	}

	public double getEffectiveness(Stat stat) {
		return getEffectiveness(stat, Math.min(MAX_LVL, get(stat)));
	}

	public boolean roll(Stat stat) {
		return Utils.percentRoll(getEffectiveness(stat));
	}

	public void randomize(int lvl) {
		final int maxStatVal = RAND_MAX + lvl;
		maxStatSum += lvl * ROLLABLE_STATS;

		int sum = 0;
		while (sum != maxStatSum) {
			for (Stat stat : Stat.values())
				if (!stat.excludeFromSum())
					set(stat, Utils.rand(RAND_MIN, maxStatVal));
			sum = getSum();
		}
	}

	public void randomize() {
		randomize(0);
	}
}
