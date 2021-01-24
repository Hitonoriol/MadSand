package hitonoriol.madsand.entities;

import java.util.Arrays;
import java.util.List;

public enum Faction {
	None, Outlaws, Marauders, Partisans, Monsters, Animals;

	public static List<Faction> humanFactions = Arrays.asList(Outlaws, Marauders, Partisans);
	
	public boolean isHuman() {
		return humanFactions.contains(this);
	}
}
