package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.PlayerStats;
import hitonoriol.madsand.entities.Stat;
import hitonoriol.madsand.entities.StatContainer;
import hitonoriol.madsand.gui.widgets.StatLabels;
import me.xdrop.jrand.JRand;

public class PlayerStatDialog extends GameDialog {
	static int DEFAULT_STAT_SUM = 6;
	int maxStatSum = MadSand.player().stats.baseStats.maxStatSum;
	int minStatSum;
	boolean restoreOnChange = false;
	protected StatLabels statLabels;
	String titleString;
	TextField nameField;

	public PlayerStatDialog(Stage stage, StatLabels statLabels, String title, int minStatSum) {
		super(stage);
		this.statLabels = statLabels;
		this.minStatSum = minStatSum;
		titleString = title;
		statLabels.refreshStatLabels();
		init();
	}

	public PlayerStatDialog(Stage stage, StatLabels statLabels, String title) {
		this(stage, statLabels, title, DEFAULT_STAT_SUM);
	}

	private void init() {
		float width = Gui.defLblWidth;

		Label title = super.getTitleLabel();

		title.setText(titleString);
		title.setAlignment(Align.center);

		nameField = new TextField(JRand.name().gen(), Gui.skin);
		statLabels.refreshStatLabels();
		super.add(new Label("\nCharacter name:", Gui.skin)).width(width).row();

		super.add(nameField).width(width).row();

		for (StatLabels.StatLabel label : statLabels.labels)
			if (!label.stat.excludeFromSum())
				addStatEntry(label);

		super.add(statLabels.statSumLbl).width(width).row();
		super.add(statLabels.freeStatPointsLbl).width(width).row();
	}

	float BUTTON_WIDTH = 15;
	float ENTRY_HEIGHT = 15;
	float BUTTON_PADDING = 4;
	float LABEL_WIDTH = Gui.defLblWidth - ((BUTTON_WIDTH + BUTTON_PADDING) * 2);

	private void addStatEntry(StatLabels.StatLabel label) {
		Stat stat = label.stat;
		Table group = new Table();

		TextButton incButton = getStatButton(stat, true);
		TextButton decButton = getStatButton(stat, false);

		incButton.setSize(BUTTON_WIDTH, BUTTON_WIDTH);
		decButton.setSize(BUTTON_WIDTH, BUTTON_WIDTH);
		label.setWidth(LABEL_WIDTH);

		group.add(label).size(LABEL_WIDTH, ENTRY_HEIGHT);
		group.add(decButton).size(BUTTON_WIDTH, ENTRY_HEIGHT).padRight(BUTTON_PADDING);
		group.add(incButton).size(BUTTON_WIDTH, ENTRY_HEIGHT).padRight(BUTTON_PADDING);

		super.add(group);
		super.row();
	}

	private TextButton getStatButton(Stat stat, boolean inc) {
		TextButton button = new TextButton(inc ? "+" : "-", Gui.skin);
		ChangeListener listener = new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				PlayerStats stats = statLabels.getStats();
				StatContainer baseStats = stats.baseStats;
				int statSum = stats.getSum();

				if (inc) {
					if (statSum < maxStatSum)
						baseStats.increase(stat);
				} else {
					if (statSum > minStatSum && baseStats.get(stat) > 1)
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
