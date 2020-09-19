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
import hitonoriol.madsand.containers.IntContainer;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Stats;
import hitonoriol.madsand.gui.widgets.StatLabels;
import hitonoriol.madsand.world.World;

public class PlayerStatDialog extends GameDialog {

	static int DEFAULT_STAT_SUM = 6;
	int maxStatSum = World.player.stats.maxStatSum;
	int minStatSum;
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

		nameField = new TextField("Player", Gui.skin);
		statLabels.refreshStatLabels();
		super.add(new Label("\nCharacter name:", Gui.skin)).width(width).row();

		super.add(nameField).width(width).row();

		addStatEntry(statLabels.conStatLbl);
		addStatEntry(statLabels.strStatLbl);
		addStatEntry(statLabels.accStatLbl);
		addStatEntry(statLabels.intStatLbl);
		addStatEntry(statLabels.luckStatLbl);
		addStatEntry(statLabels.dexStatLbl);

		super.add(statLabels.statSumLbl).width(width).row();
		super.add(statLabels.freeStatPointsLbl).width(width).row();
	}

	float BUTTON_WIDTH = 15;
	float ENTRY_HEIGHT = 15;
	float BUTTON_PADDING = 4;
	float LABEL_WIDTH = Gui.defLblWidth - ((BUTTON_WIDTH + BUTTON_PADDING) * 2);

	private void addStatEntry(Label label) {
		IntContainer stat = getStatByLabel(label);
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

	private IntContainer getStatByLabel(Label label) {
		IntContainer stat = new IntContainer();
		Stats stats = World.player.stats;

		if (label == statLabels.conStatLbl)
			stat.set(StatLabels.conString, stats.constitution);
		else if (label == statLabels.strStatLbl)
			stat.set(StatLabels.strString, stats.strength);
		else if (label == statLabels.accStatLbl)
			stat.set(StatLabels.accString, stats.accuracy);
		else if (label == statLabels.intStatLbl)
			stat.set(StatLabels.intString, stats.intelligence);
		else if (label == statLabels.luckStatLbl)
			stat.set(StatLabels.luckString, stats.luck);
		else if (label == statLabels.dexStatLbl)
			stat.set(StatLabels.dexString, stats.dexterity);

		return stat;
	}

	private TextButton getStatButton(IntContainer stat, boolean inc) {
		TextButton button = new TextButton(inc ? "+" : "-", Gui.skin);
		ChangeListener listener = new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				int statSum = statLabels.stats.getSum();
				if (inc) {
					if (statSum < maxStatSum)
						++stat.value;
				} else {
					if (statSum > minStatSum && stat.value > 1)
						--stat.value;
				}
				statLabels.refreshStatLabel(stat);
			}
		};

		button.addListener(listener);
		return button;
	}

}
