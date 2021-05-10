package hitonoriol.madsand.entities.skill;

public enum Skill {
	None, Level, Melee, Evasion, Woodcutting, Gathering, Mining, Digging, Survival, Crafting, Farming, Fishing;

	public boolean isResourceSkill() {
		return this == Woodcutting || this == Gathering || this == Mining || this == Digging || this == Farming;
	}
	
	public boolean isFightingSkill() {
		return this == Melee || this == Evasion;
	}

	public static final float SKILL_DMG_COEF = 1.125f;
}
