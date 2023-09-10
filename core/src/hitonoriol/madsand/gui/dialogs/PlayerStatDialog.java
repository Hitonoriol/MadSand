package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.BaseStats;
import hitonoriol.madsand.entities.Stat;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.stats.StatLabels;
import me.xdrop.jrand.JRand;

public class PlayerStatDialog extends GameDialog {
	private static float WIDTH = Gui.screenWidth(0.4f);
	
	private static int DEFAULT_STAT_SUM = Stat.totalRollableStats();
	private int maxStatSum = MadSand.player().stats.baseStats.maxStatSum;
	private int minStatSum;
	protected boolean restoreOnChange = false;
	protected StatLabels statLabels;
	protected TextField nameField;

	public PlayerStatDialog(Stage stage, StatLabels statLabels, String title, int minStatSum) {
		super(stage);
		this.statLabels = statLabels;
		this.minStatSum = minStatSum;
		statLabels.refreshStatLabels();
		var titleLabel = super.getTitleLabel();

		titleLabel.setText(title);
		titleLabel.setAlignment(Align.center);

		nameField = new TextField(JRand.name().gen(), Gui.skin);
		statLabels.refreshStatLabels();
		super.add(Widgets.label("\nCharacter name:")).width(WIDTH).row();

		super.add(nameField).width(WIDTH).row();
		super.add("").row();
		
		var header = Widgets.table();
		header.add(Widgets.label("Stat", Gui.FONT_M)).width(Value.percentWidth(.3f, this)).align(Align.left);
		header.add(Widgets.label("Effect", Gui.FONT_M)).width(Value.percentWidth(.45f, this)).align(Align.left);
		header.pack();
		super.add(header).width(WIDTH);
		super.row();
		
		var labels = statLabels.getLabels();
		var descriptions = statLabels.getEffectDescriptionLabels();
		for (int i = 0; i < labels.size(); ++i) {
			var label = labels.get(i);
			var description = descriptions.get(i);
			if (!label.stat.excludeFromSum())
				addStatEntry(label, description);
		}

		super.add("").row();
		super.add(statLabels.getStatPointsLabel()).height(10).row();
		super.add("").row();
		pack();
	}

	public PlayerStatDialog(Stage stage, StatLabels statLabels, String title) {
		this(stage, statLabels, title, DEFAULT_STAT_SUM);
	}

	private float BUTTON_WIDTH = 15, BUTTON_PADDING = 4;
	private float ENTRY_HEIGHT = 15;
	private float LABEL_WIDTH = WIDTH - ((BUTTON_WIDTH + BUTTON_PADDING) * 2);

	private void addStatEntry(StatLabels.StatLabel label, Label description) {
		var stat = label.stat;
		var group = Widgets.table();

		var incButton = getStatButton(stat, true);
		var decButton = getStatButton(stat, false);

		incButton.setSize(BUTTON_WIDTH, BUTTON_WIDTH);
		decButton.setSize(BUTTON_WIDTH, BUTTON_WIDTH);
		label.setWidth(LABEL_WIDTH);

		group.defaults().align(Align.left).height(ENTRY_HEIGHT);
		group.add(label).width(Value.percentWidth(.3f, this));
		group.add(description).width(Value.percentWidth(.45f, this));
		group.add(decButton).size(BUTTON_WIDTH, ENTRY_HEIGHT).padRight(BUTTON_PADDING);
		group.add(incButton).size(BUTTON_WIDTH, ENTRY_HEIGHT).padRight(BUTTON_PADDING);
		group.pack();
		
		super.add(group);
		super.row();
	}

	private TextButton getStatButton(Stat stat, boolean inc) {
		var button = Widgets.button(inc ? "+" : "-");
		ChangeListener listener = new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				var stats = statLabels.getStats();
				var baseStats = stats.baseStats;
				int statSum = stats.getSum();
				int curValue = baseStats.get(stat);

				if (inc) {
					if (statSum < maxStatSum && curValue < BaseStats.MAX_LVL)
						baseStats.increase(stat);
				} else {
					if (statSum > minStatSum && curValue > 1)
						baseStats.decrease(stat);
				}
				statLabels.refreshStatLabels();

				if (restoreOnChange)
					stats.restore();

				Gui.refreshOverlay();
			}
		};

		button.addListener(listener);
		return button;
	}

	public boolean hasUnassignedPoints() {
		if (statLabels.getStats().baseStats.getFreePoints() > 0) {
			Gui.drawOkDialog("You still have unassigned stat points left!");
			return true;
		}
		return false;
	}

}
