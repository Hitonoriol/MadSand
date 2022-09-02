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
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.PlayerStats;
import hitonoriol.madsand.entities.Reputation;
import hitonoriol.madsand.entities.skill.Skill;
import hitonoriol.madsand.entities.skill.SkillContainer;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
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
		Table scrollTable = new Table();
		scrollTable.defaults().width(Gui.DEFAULT_WIDTH);
		AutoFocusScrollPane dialogScroll = new AutoFocusScrollPane(scrollTable);
		Player player = MadSand.player();
		PlayerStats stats = player.stats;
		Label nameLbl = new Label(player.stats.name, skin);
		Label levelLbl = new Label("Level: " + player.stats.skills.getLvl(Skill.Level) + " ("
				+ player.stats.skills.getExpString(Skill.Level) + ")", skin);

		Gui.setFontSize(levelLbl, Gui.FONT_M);
		Gui.setFontSize(nameLbl, Gui.FONT_M);

		// Dialog Title (Player's name & Level progressbar)
		add(nameLbl).row();
		setBackground(GuiSkin.darkBackground);
		add().row();
		statLabels.refreshStatLabels();
		add(StatProgressBar.createLevelBar()
				.setStatText("LVL " + stats.skills.getLvl())
				.setSkill(player.stats.skills.get(Skill.Level))
				.setProgressSize(BAR_WIDTH * 1.75f, BAR_HEIGHT))
				.row();
		add().width(Gui.DEFAULT_WIDTH).row();

		// Stat list
		addTitle(scrollTable, "Stats:");
		for (StatLabels.StatLabel label : statLabels.labels)
			scrollTable.add(label).padBottom(LINE_PAD).row();

		addTitle(scrollTable, "Miscellaneous:");
		scrollTable.add(createMiscInfoTable()).row();

		// Skill list
		addTitle(scrollTable, "Skills:");
		scrollTable.add(createSkillTable()).row();

		// Reputation list
		addTitle(scrollTable, "Reputation:");
		scrollTable.add(createRepTable()).row();

		add(dialogScroll).width(GameDialog.WIDTH).height(400).row();
		add(createCloseButton()).padTop(35).padBottom(LINE_PAD).row();
	}

	private Table createMiscInfoTable() {
		Table info = new Table(Gui.skin);
		info.align(Align.left);
		info.defaults().align(Align.left).padBottom(LINE_PAD);
		Player player = MadSand.player();
		info.add("Creatures killed: " + player.getKillCount()).row();
		info.add("Settlements established: " + player.getEstablishedSettlements()).row();
		return info;
	}

	private void addTitle(Table table, String text) {
		Label label = new Label(text, Gui.skin);
		label.setStyle(GuiSkin.getLabelStyle(Gui.FONT_M));
		table.add(new Label("", Gui.skin)).row();
		table.add(label)
				.width(Gui.DEFAULT_WIDTH)
				.padLeft(headerLeftPadding)
				.padBottom(headerBottomPadding).row();
	}

	private Table createRepTable() {
		Table repTable = new Table(Gui.skin);
		repTable.align(Align.left);
		for (Faction faction : Faction.values()) {
			if (faction == Faction.None)
				continue;

			repTable.add(faction.name() + ": ")
					.width(130)
					.align(Align.left)
					.padBottom(LINE_PAD);

			repTable.add(new StatProgressBar()
					.setStyle(repStyle)
					.setRange(-Reputation.RANGE, Reputation.RANGE)
					.roundValues(false)
					.setStatText("") // TODO: Reputation levels
					.setValue(MadSand.player().getReputation().get(faction))
					.setProgressSize(BAR_WIDTH, BAR_HEIGHT))
					.size(BAR_WIDTH, BAR_HEIGHT)
					.padBottom(LINE_PAD)
					.row();
		}
		return repTable;
	}

	private Table createSkillTable() {
		Label skillLbl;
		SkillContainer skills = MadSand.player().stats.skills;
		Table skillTable = new Table();
		skillTable.align(Align.left);
		Player player = MadSand.player();
		for (Skill skill : Skill.values()) {
			if (skill == Skill.Level || skill == Skill.None)
				continue;

			skillLbl = new Label(skill + ": ", skin);

			skillTable.add(skillLbl)
					.width(130)
					.align(Align.left)
					.padBottom(LINE_PAD);
			skillTable.add(new StatProgressBar(skill.name())
					.setStyle(skillStyle)
					.setStatText("LVL " + skills.getLvl(skill))
					.setSkill(player.stats.skills.get(skill))
					.setProgressSize(BAR_WIDTH, BAR_HEIGHT))
					.size(BAR_WIDTH, BAR_HEIGHT)
					.padBottom(LINE_PAD);
			skillTable.row();
		}

		return skillTable;
	}
}
