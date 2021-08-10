package hitonoriol.madsand.tests;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import de.vandermeer.asciitable.Table;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.BaseStats;
import hitonoriol.madsand.entities.Stat;
import hitonoriol.madsand.util.Utils;

public class StatProgressionTest {
	private BaseStats stats = MadSand.player().stats().baseStats.prepareLivingCreature();

	private void forEachStat(Consumer<Stat> action) {
		Stat.rollableStats.stream().forEach(action);
	}

	@Test
	void testEffectivenessProgression() {
		Table table = new Table();
		table.add("Value");
		forEachStat(stat -> table.add(stat.toString()));
		table.row().addRule();
		for (int lvl = 1; lvl <= BaseStats.MAX_LVL; ++lvl) {
			final int newVal = lvl + 1;
			table.add(stats.get(Stat.Luck));
			forEachStat(stat -> {
				table.add(String.format("%.2f", stats.getEffectiveness(stat)));
				stats.set(stat, newVal);
			});
			table.row().addRule();
		}
		Utils.out(table.render());
	}
}
