package hitonoriol.madsand.enums;

public enum WorkerType {
	Miner, Digger, Woodcutter;
	
	public Skill getSkill() {
		if (this == Miner)
			return Skill.Mining;
		else if (this == Woodcutter)
			return Skill.Woodcutting;
		else
			return Skill.None;
	}
}
