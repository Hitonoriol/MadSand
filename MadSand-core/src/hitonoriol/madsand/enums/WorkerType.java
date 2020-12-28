package hitonoriol.madsand.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import hitonoriol.madsand.Utils;

public enum WorkerType {
	Sweeper(100),
	Hunter(40),
	Miner(30), Digger(30), Woodcutter(30);

	public static List<WorkerType> workers = new ArrayList<WorkerType>(Arrays.asList(values()));
	static {
		Collections.sort(workers, new Comparator<WorkerType>() {
			@Override
			public int compare(WorkerType o1, WorkerType o2) {
				return Double.compare(o1.probability, o2.probability);
			}
		});
	}

	double probability;

	WorkerType(double probability) {
		this.probability = probability;
	}

	public static WorkerType roll() {
		double roll = Utils.randPercent();
		for (WorkerType type : workers) {
			if (Utils.percentRoll(roll, type.probability))
				return type;
		}
		return Sweeper;
	}

	public Skill getSkill() {
		if (this == Miner)
			return Skill.Mining;
		else if (this == Woodcutter)
			return Skill.Woodcutting;
		else
			return Skill.None;
	}
}
