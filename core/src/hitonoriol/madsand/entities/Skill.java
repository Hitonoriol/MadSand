package hitonoriol.madsand.entities;

public enum Skill {
	None, Level, Melee, Evasion, Woodcutting, Gathering, Mining, Digging, Survival, Crafting, Farming, Fishing;

	public boolean isResourceSkill() {
		return this == Woodcutting || this == Gathering || this == Mining || this == Digging || this == Farming;
	}
	
	public boolean isFightingSkill() {
		return this == Melee || this == Evasion;
	}

	public static final int SKILL_DMG_DENOMINATOR = 2;
}
