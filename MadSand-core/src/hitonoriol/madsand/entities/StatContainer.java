package hitonoriol.madsand.entities;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.enums.Stat;

public class StatContainer {
	static final int STAT_RAND_MAX = 8;
	static final int STAT_RAND_MIN = 3;
	static int STAT_MIN_SUM = 20; //for roll() method

	public HashMap<Stat, Integer> stats = new HashMap<>();
	public int maxStatSum = 20;

	public StatContainer() {

	}

	public StatContainer(StatContainer stats) {
		set(stats);
	}

	public int get(Stat stat) {
		return stats.getOrDefault(stat, 0);
	}

	public void set(Stat stat, int value) {
		stats.put(stat, value);
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
		int sum = 0;
		int maxSum = STAT_RAND_MAX + lvl;
		while (sum < STAT_MIN_SUM || sum > maxStatSum + lvl * 6) {
			for (Stat stat : Stat.values())
				if (!stat.excludeFromSum())
					set(stat, Utils.rand(STAT_RAND_MIN, maxSum));
			sum = getSum();
		}
	}

	public void roll() {
		roll(0);
	}
}
