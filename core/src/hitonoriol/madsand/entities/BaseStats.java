package hitonoriol.madsand.entities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.util.Utils;

public class BaseStats extends HashMap<Stat, Integer> {
	private final static int ROLLABLE_STATS = Stat.rollableStats.size();
	public static final int MAX_LVL = 35, MAX_SUM = MAX_LVL * ROLLABLE_STATS;
	private static final int RAND_MAX = 8, RAND_MIN = 1;
	private static final int DEF_MAX_SUM = 15;

	public int maxStatSum = DEF_MAX_SUM;

	public BaseStats() {}

	public BaseStats(BaseStats stats) {
		set(stats);
	}

	public double getOverallProgress() {
		return (double) getSum() / (double) MAX_SUM;
	}

	public BaseStats prepareLivingCreature() {
		Stat.rollableStats.forEach(stat -> set(stat, 1));
		return this;
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

	/* Stat effectiveness rating in %: [0; 100] */
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

	public double maxEffectiveness(Stat stat) {
		return getEffectiveness(stat, MAX_LVL);
	}

	private double toMax(Stat stat) {
		return getEffectiveness(stat) / maxEffectiveness(stat);
	}

	private double toMaxInverse(Stat stat) {
		double max = maxEffectiveness(stat);
		return (max - getEffectiveness(stat)) / max;
	}

	/* Equivalent to (roll() && roll() && ...) */
	public boolean rollAnd(Stat stat, int times) {
		if (times < 2)
			return roll(stat);

		double combinedProb = Math.pow(getEffectiveness(stat) / 100, times) * 100;
		Utils.dbg("Rolling %s[%d] &&%d times = %.5f%%", stat, get(stat), times, combinedProb);
		return Utils.percentRoll(combinedProb);
	}

	/* Equivalent to (roll() || roll() || ...) */
	public boolean rollOr(Stat stat, int times) {
		if (times < 2)
			return roll(stat);

		for (int i = 0; i < times; ++i)
			if (roll(stat))
				return true;
		return false;
	}

	/* !roll() equivalent, but with max probability capped at <probCap>% */
	public boolean rollInverse(Stat stat, double probCap) {
		return Utils.percentRoll(toMaxInverse(stat) * probCap);
	}

	public boolean roll(Stat stat, double probCap) {
		return Utils.percentRoll(toMax(stat) * probCap);
	}

	public boolean roll(Stat stat) {
		return Utils.percentRoll(getEffectiveness(stat));
	}

	public void randomize(int lvl, int minStatVal) {
		final int maxStatVal = RAND_MAX + lvl;
		maxStatSum += lvl * ROLLABLE_STATS;

		int sum = 0;
		while (sum != maxStatSum) {
			for (Stat stat : Stat.values())
				if (!stat.excludeFromSum())
					set(stat, Utils.rand(minStatVal, maxStatVal));
			sum = getSum();
		}
	}

	public void randomize(int lvl) {
		randomize(lvl, RAND_MIN);
	}

	public void randomize() {
		randomize(0, RAND_MIN);
	}

	@Override
	public String toString() {
		return String.format("Base stats: {%s}",
				Arrays.stream(Stat.values())
						.map(stat -> stat.name() + ": " + get(stat))
						.collect(Collectors.joining(", ")));
	}
}
