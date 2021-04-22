package hitonoriol.madsand.entities;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.util.Utils;

public class StatContainer extends HashMap<Stat, Integer> {
	static final int STAT_RAND_MAX = 8;
	static final int STAT_RAND_MIN = 3;
	static int STAT_MIN_SUM = 20; //for roll() method

	public int maxStatSum = 20;

	public StatContainer() {
		super();
	}

	public StatContainer(StatContainer stats) {
		set(stats);
	}

	public int get(Stat stat) {
		return super.getOrDefault(stat, 0);
	}

	public void set(Stat stat, int value) {
		super.put(stat, value);
	}

	public void set(StatContainer stats) {
		for (Stat stat : Stat.values())
			set(stat, stats.get(stat));
	}

	// Copy specified stat from stat container
	public void set(Stat stat, StatContainer stats) {
		set(stat, stats.get(stat));
	}

	public void add(StatContainer statBonus) {
		for (Stat stat : Stat.values())
			increase(stat, statBonus.get(stat));
	}

	public void sub(StatContainer statBonus) {
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

	@JsonIgnore
	public int getFreePoints() {
		return maxStatSum - getSum();
	}

	public void roll(int lvl) {
		final int maxStatVal = STAT_RAND_MAX + lvl;
		final int rollableStats = Stat.rollableStats.size();

		int sum = 0;
		while (sum < STAT_MIN_SUM || sum > maxStatSum + lvl * rollableStats) {
			for (Stat stat : Stat.values())
				if (!stat.excludeFromSum())
					set(stat, Utils.rand(STAT_RAND_MIN, maxStatVal));
			sum = getSum();
		}
	}

	public void roll() {
		roll(0);
	}
}
