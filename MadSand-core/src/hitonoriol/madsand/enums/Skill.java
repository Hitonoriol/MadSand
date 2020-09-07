package hitonoriol.madsand.enums;

public enum Skill {
	None(0), Level(1), Woodcutting(2), Harvesting(3), Mining(4), Survival(5), Crafting(6), Farming(7), Digging(8);
	
	public static final int SKILL_DMG_DENOMINATOR = 2;

	private final int val;

	private Skill(int val) {
		this.val = val;
	}

	public int get() {
		return val;
	}

	private static Skill[] values = Skill.values();

	public static Skill get(int i) {
		if (i < 0)
			i = 0;
		return values[i];
	}

	public static int len() {
		return values.length;
	}
}
