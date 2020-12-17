package hitonoriol.madsand.enums;

public enum Faction {
	None, Outlaws, Marauders, Partisans, Monsters, Animals;

	public boolean isHuman() {
		return this == Outlaws ||
				this == Marauders ||
				this == Partisans;
	}
}
