package hitonoriol.madsand.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public enum Stat {
	Constitution, Dexterity, Strength, Accuracy, Intelligence, Luck, Defense;

	public static final List<Stat> rollableStats = new ArrayList<>(Arrays.asList(values()));
	static {
		Iterator<Stat> it = rollableStats.iterator();
		while (it.hasNext()) {
			if (it.next().excludeFromSum())
				it.remove();
		}

	}

	public boolean excludeFromSum() {
		return (this == Defense);
	}
}
