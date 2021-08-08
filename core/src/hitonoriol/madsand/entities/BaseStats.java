package hitonoriol.madsand.entities;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.util.Utils;

public class BaseStats extends HashMap<Stat, Integer> {
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

	public void roll(int lvl) {
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

	public void roll() {
		roll(0);
	}
}
