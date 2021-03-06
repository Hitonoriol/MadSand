package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Faction;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.PlayerStats;
import hitonoriol.madsand.entities.Reputation;
import hitonoriol.madsand.entities.skill.Skill;
import hitonoriol.madsand.entities.skill.SkillContainer;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.gui.widgets.StatLabels;
import hitonoriol.madsand.gui.widgets.StatProgressBar;

/*
 * Character info in-game menu 
 */

public class CharacterInfoWindow {
	private final static float headerLeftPadding = -45f;
	private final static float headerBottomPadding = 5f;

	final static float LINE_PAD = 5;
	final static float BAR_WIDTH = 150;
	final static float BAR_HEIGHT = 19;
	static ProgressBarStyle skillStyle = Gui.createProgressBarStyle(BAR_WIDTH, BAR_HEIGHT, Color.LIME);
	static ProgressBarStyle repStyle = Gui.createProgressBarStyle(BAR_WIDTH, BAR_HEIGHT, Color.ORANGE);

	GameDialog dialog;
	StatLabels statLabels;
	Skin skin = Gui.skin;

	public CharacterInfoWindow() {
		statLabels = new StatLabels();
		dialog = new GameDialog(Gui.overlay);
		createDialog();
	}

	public void createDialog() {
		Table scrollTable = new Table();
		scrollTable.defaults().width(Gui.defLblWidth);
		AutoFocusScrollPane dialogScroll = new AutoFocusScrollPane(scrollTable);
		Player player = MadSand.player();
		PlayerStats stats = player.stats;
		Label nameLbl = new Label(player.stats.name, skin);
		Label levelLbl = new Label("Level: " + player.stats.skills.getLvl(Skill.Level) + " ("
				+ player.stats.skills.getExpString(Skill.Level) + ")", skin);

		Gui.setFontSize(levelLbl, Gui.FONT_M);
		Gui.setFontSize(nameLbl, Gui.FONT_M);

		// Dialog Title (Player's name & Level progressbar)
		dialog.add(nameLbl).row();
		dialog.setBackground(Gui.darkBackground);
		dialog.add().row();
		statLabels.refreshStatLabels();
		dialog.add(StatProgressBar.createLevelBar()
				.setRange(0, stats.skills.get(Skill.Level).requiredExp)
				.setStatText("LVL " + stats.skills.getLvl())
				.setValue(stats.skills.getExp())
				.setProgressSize(BAR_WIDTH * 1.75f, BAR_HEIGHT))
				.row();
		dialog.add().width(Gui.defLblWidth).row();

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

		TextButton ok = new TextButton("Close", skin);

		dialog.add(dialogScroll).width(GameDialog.WIDTH).height(400).row();
		dialog.add(ok).padTop(35).padBottom(LINE_PAD).row();

		ok.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				dialog.hide();
			}

		});
	}
	
	public GameDialog getDialog() {
		return dialog;
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
		label.setStyle(Gui.getLabelStyle(Gui.FONT_M));
		table.add(new Label("", Gui.skin)).row();
		table.add(label)
				.width(Gui.defLblWidth)
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
					.setRange(0, skills.get(skill).requiredExp)
					.setStatText("LVL " + skills.getLvl(skill))
					.setValue(skills.getExp(skill))
					.setProgressSize(BAR_WIDTH, BAR_HEIGHT))
					.size(BAR_WIDTH, BAR_HEIGHT)
					.padBottom(LINE_PAD);
			skillTable.row();
		}

		return skillTable;
	}

	public void show() {
		dialog.show(Gui.overlay);
	}

	public void remove() {
		dialog.remove();
	}
}
