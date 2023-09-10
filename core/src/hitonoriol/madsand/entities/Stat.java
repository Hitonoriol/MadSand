package hitonoriol.madsand.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Stat {
	Constitution, Dexterity, Strength, Accuracy, Intelligence, Luck, Defense;

	public static final List<Stat> rollableStats = new ArrayList<>(Arrays.asList(values()));
	static {
		var it = rollableStats.iterator();
		while (it.hasNext()) {
			if (it.next().excludeFromSum())
				it.remove();
		}
	}

	public boolean excludeFromSum() {
		return (this == Defense);
	}

	public static int totalRollableStats() {
		return rollableStats.size();
	}
	
	public String getEffectDescription(Stats stats) {
		switch (this) {
		case Accuracy:
			return String.format("Minimum damage: [STAT]%.2f%%[]", stats.baseStats.getEffectiveness(Stat.Accuracy) * 100.0f);
		case Constitution:
			return String.format("Health points: [STAT]%d[]", stats.mhp);
		case Dexterity:
			return String.format("Speed: [STAT]%.2f[]", stats.actionPtsMax);
		case Strength:
			return String.format("Damage: [STAT]%d - %d[]", stats.getMinDamage(Stat.Strength), stats.getMaxDamage(Stat.Strength));
		case Luck:
			return String.format("Luck chance: [STAT]%.2f%%[]", stats.baseStats.getEffectiveness(Stat.Luck) * 100.0f);
		default:
			return "";
		}
	}
}
