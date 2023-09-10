package hitonoriol.madsand.gui.widgets.stats;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.PlayerStats;
import hitonoriol.madsand.entities.Stat;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;

public class StatLabels {
	private List<StatLabel> labels = new ArrayList<>();
	private List<Label> effectDescriptionLabels = new ArrayList<>();
	
	private Label freeStatPointsLbl;
	private Label hpStatLbl, staminaStatLbl;

	private PlayerStats stats = MadSand.player().stats;

	public StatLabels(Skin skin) {
		for (Stat stat : Stat.values()) {
			labels.add(new StatLabel(stat));
			effectDescriptionLabels.add(Widgets.label(stat.getEffectDescription(stats)));
		}

		hpStatLbl = new Label("", skin);
		staminaStatLbl = new Label("", skin);
		freeStatPointsLbl = new Label("", skin);
	}

	public StatLabels() {
		this(Gui.skin);
	}

	public List<StatLabel> getLabels() {
		return labels;
	}
	
	public List<Label> getEffectDescriptionLabels() {
		return effectDescriptionLabels;
	}
	
	public Label getStatPointsLabel() {
		return freeStatPointsLbl;
	}
	
	public Label getHpLabel() {
		return hpStatLbl;
	}
	
	public Label getStaminaLabel() {
		return staminaStatLbl;
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

		for (int i = 0; i < labels.size(); ++i) {
			var statLabel = labels.get(i);
			statLabel.refresh();
			effectDescriptionLabels.get(i).setText(statLabel.stat.getEffectDescription(stats));
		}

		freeStatPointsLbl.setText(String.format("Free points: %d (%d spent)", stats.baseStats.getFreePoints(), statSum));
		hpStatLbl.setText(String.format("HP: %d / %d", stats.hp, stats.mhp));
		staminaStatLbl.setText(String.format("Stamina: %.1f / %.1f", stats.stamina, stats.maxstamina));
		Gui.refreshOverlay();
	}

	//public void refreshStatLabel(IntContainer value) {}

	public class StatLabel extends Label {
		public Stat stat;

		public StatLabel(Stat stat) {
			super("", Gui.skin);
			this.stat = stat;
		}

		public StatLabel refresh() {
			var text = stat.name() + ": " + applyColor(stats.get(stat));
			super.setText(text);
			
			return this;
		}

		private String applyColor(int stat) {
			return "[STAT]" + stat + "[]";
		}
	}

	public PlayerStats getStats() {
		return stats;
	}
}
