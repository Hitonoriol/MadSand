package hitonoriol.madsand.world;

import hitonoriol.madsand.containers.rolltable.RollTable;
import hitonoriol.madsand.entities.skill.Skill;

public enum WorkerType {
	Sweeper,
	Hunter,
	Miner, Digger, Woodcutter;

	public static WorkerType workers[] = values();
	public static RollTable<WorkerType> workerRollTable = new RollTable<>();
	static {
		workerRollTable
				.add(100, Sweeper)
				.add(40, Hunter)
				.add(30, Miner, Digger, Woodcutter);
	}

	public static WorkerType roll() {
		return workerRollTable.rollSingle();
	}

	public Skill getSkill() {
		switch (this) {
		case Miner:
			return Skill.Mining;

		case Woodcutter:
			return Skill.Woodcutting;

		case Digger:
			return Skill.Digging;

		default:
			return Skill.None;
		}
	}
}
