package hitonoriol.madsand.gui.widgets.stats;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.PlayerStats;
import hitonoriol.madsand.entities.Stat;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.util.Utils;

public class StatLabels {
	public List<StatLabel> labels = new ArrayList<>();
	public Label statSumLbl;
	public Label freeStatPointsLbl;
	public Label hpStatLbl, staminaStatLbl;

	private PlayerStats stats = MadSand.player().stats;

	public StatLabels(Skin skin) {

		for (Stat stat : Stat.values())
			labels.add(new StatLabel(stat));

		statSumLbl = new Label("", skin);
		hpStatLbl = new Label("", skin);
		staminaStatLbl = new Label("", skin);
		freeStatPointsLbl = new Label("", skin);
	}

	public StatLabels() {
		this(Gui.skin);
	}

	public StatLabel getLabel(Stat stat) {
		for (StatLabel label : labels)
			if (label.stat == stat)
				return label;
		return null;
	}

	public void refreshStatLabels() {
		int statSum = stats.getSum();
		stats.calcStats();

		for (StatLabel label : labels)
			label.refresh();

		statSumLbl.setText("\nStat sum: " + statSum);
		freeStatPointsLbl.setText("Free stat points: " + (stats.baseStats.getFreePoints()));

		hpStatLbl.setText("HP: " + stats.hp + "/" + stats.mhp);
		staminaStatLbl.setText("Stamina: " + stats.stamina + "/" + stats.maxstamina);
		Gui.refreshOverlay();
	}

	public void refreshStatLabel(Stat stat) {
		for (StatLabel label : labels)
			if (label.stat == stat)
				label.refresh();
	}

	//public void refreshStatLabel(IntContainer value) {}

	public class StatLabel extends Label {
		public Stat stat;

		public StatLabel(Stat stat) {
			super("", Gui.skin);
			this.stat = stat;
		}

		public StatLabel refresh() {
			String text = stat.name() + ": " + applyColor(stats.get(stat));
			if (stat == Stat.Dexterity)
				text += " (speed: " + Utils.round(stats.actionPtsMax) + ")";
			super.setText(text);
			return this;
		}

		private String applyColor(int stat) {
			return "[#99ffaa]" + stat + "[]";
		}
	}
	
	public PlayerStats getStats() {
		return stats;
	}
}
