package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Faction;
import hitonoriol.madsand.entities.Reputation;
import hitonoriol.madsand.entities.skill.Skill;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.gui.widgets.RowColorTable;
import hitonoriol.madsand.gui.widgets.stats.StatLabels;
import hitonoriol.madsand.gui.widgets.stats.StatProgressBar;

/*
 * Character info in-game menu
 */

public class CharacterInfoWindow extends GameDialog {
	private final static float headerLeftPadding = -45f;
	private final static float headerBottomPadding = 5f;

	final static float LINE_PAD = 5;
	final static float BAR_WIDTH = 150;
	final static float BAR_HEIGHT = 19;
	static ProgressBarStyle skillStyle = GuiSkin.createProgressBarStyle(BAR_WIDTH, BAR_HEIGHT, Color.LIME);
	static ProgressBarStyle repStyle = GuiSkin.createProgressBarStyle(BAR_WIDTH, BAR_HEIGHT, Color.ORANGE);

	StatLabels statLabels;
	Skin skin = Gui.skin;

	public CharacterInfoWindow() {
		statLabels = new StatLabels();
		createDialog();
	}

	public void createDialog() {
		var scrollTable = Widgets.table();
		scrollTable.defaults().width(Gui.DEFAULT_WIDTH);
		var dialogScroll = new AutoFocusScrollPane(scrollTable);
		var player = MadSand.player();
		var stats = player.stats;
		var nameLbl = new Label(player.stats.name, skin);
		var levelLbl = new Label(
			"Level: " + player.stats.skills.getLvl(Skill.Level) + " ("
				+ player.stats.skills.getExpString(Skill.Level) + ")",
			skin
		);

		Gui.setFontSize(levelLbl, Gui.FONT_M);
		Gui.setFontSize(nameLbl, Gui.FONT_M);

		// Dialog Title (Player's name & Level progressbar)
		add(nameLbl).row();

		add().row();
		statLabels.refreshStatLabels();
		add(
			StatProgressBar.createLevelBar()
				.setStatText("LVL " + stats.skills.getLvl())
				.setSkill(player.stats.skills.get(Skill.Level))
				.setProgressSize(BAR_WIDTH * 1.75f, BAR_HEIGHT)
		)
			.row();
		add().width(Gui.DEFAULT_WIDTH).row();

		// Stat list
		addTitle(scrollTable, "Stats:");
		var statTable = new RowColorTable();
		statTable.left();
		for (StatLabels.StatLabel label : statLabels.getLabels()) {
			label.setAlignment(Align.left);
			statTable.add(label).width(Gui.DEFAULT_WIDTH).padBottom(LINE_PAD);
			statTable.row();
		}
		scrollTable.add(statTable);
		scrollTable.row();

		addTitle(scrollTable, "Miscellaneous:");
		scrollTable.add(createMiscInfoTable()).expandX();
		scrollTable.row();

		// Skill list
		addTitle(scrollTable, "Skills:");
		scrollTable.add(createSkillTable());
		scrollTable.row();

		// Reputation list
		addTitle(scrollTable, "Reputation:");
		scrollTable.add(createRepTable());
		scrollTable.row();

		add(dialogScroll).width(GameDialog.defaultWidth()).height(400).row();
		add(createCloseButton()).padTop(35).padBottom(LINE_PAD).row();
	}

	private Table createMiscInfoTable() {
		var info = new RowColorTable();
		info.align(Align.left);
		info.defaults().align(Align.left);
		var player = MadSand.player();
		info.add("Creatures killed: " + player.getTotalKillCount()).width(Gui.DEFAULT_WIDTH).padBottom(LINE_PAD);
		info.row();
		info.add("Settlements established: " + player.getEstablishedSettlements()).width(Gui.DEFAULT_WIDTH).padBottom(LINE_PAD);
		info.row();
		return info;
	}

	private void addTitle(Table table, String text) {
		var label = Widgets.label(text);
		label.setStyle(GuiSkin.getLabelStyle(Gui.FONT_M));
		table.add(Widgets.label(""));
		table.row();
		table.add(label)
			.width(Gui.DEFAULT_WIDTH)
			.padLeft(headerLeftPadding)
			.padBottom(headerBottomPadding);
		table.row();
	}

	private Table createRepTable() {
		var repTable = Widgets.table();
		repTable.align(Align.left);
		for (Faction faction : Faction.values()) {
			if (faction == Faction.None)
				continue;

			repTable.add(faction.name() + ": ")
				.width(130)
				.align(Align.left)
				.padBottom(LINE_PAD);

			repTable.add(
				new StatProgressBar()
					.setStyle(repStyle)
					.setRange(-Reputation.RANGE, Reputation.RANGE)
					.roundValues(false)
					.setStatText("") // TODO: Reputation levels
					.setValue(MadSand.player().getReputation().get(faction))
					.setProgressSize(BAR_WIDTH, BAR_HEIGHT)
			)
				.size(BAR_WIDTH, BAR_HEIGHT)
				.padBottom(LINE_PAD)
				.row();
		}
		return repTable;
	}

	private Table createSkillTable() {
		Label skillLbl;
		var skills = MadSand.player().stats.skills;
		var skillTable = Widgets.table();
		skillTable.align(Align.left);
		var player = MadSand.player();
		for (Skill skill : Skill.values()) {
			if (skill == Skill.Level || skill == Skill.None)
				continue;

			skillLbl = new Label(skill + ": ", skin);

			skillTable.add(skillLbl)
				.width(130)
				.align(Align.left)
				.padBottom(LINE_PAD);
			skillTable.add(
				new StatProgressBar(skill.name())
					.setStyle(skillStyle)
					.setStatText("LVL " + skills.getLvl(skill))
					.setSkill(player.stats.skills.get(skill))
					.setProgressSize(BAR_WIDTH, BAR_HEIGHT)
			)
				.size(BAR_WIDTH, BAR_HEIGHT)
				.padBottom(LINE_PAD);
			skillTable.row();
		}

		return skillTable;
	}
}
