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
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.PlayerStats;
import hitonoriol.madsand.entities.Reputation;
import hitonoriol.madsand.entities.SkillContainer;
import hitonoriol.madsand.enums.Faction;
import hitonoriol.madsand.enums.Skill;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.gui.widgets.StatLabels;
import hitonoriol.madsand.gui.widgets.StatProgressBar;
import hitonoriol.madsand.world.World;

/*
 * Character info in-game menu 
 */

public class CharacterInfoWindow {
	private final static float headerLeftPadding = -45f;
	private final static float headerBottomPadding = 5f;
	private final static float headerScale = 1.11f;

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
		AutoFocusScrollPane dialogScroll = new AutoFocusScrollPane(scrollTable);
		PlayerStats stats = World.player.stats;
		float width = Gui.defLblWidth;
		Label nameLbl = new Label(World.player.stats.name, skin);
		Label levelLbl = new Label("Level: " + World.player.stats.skills.getLvl(Skill.Level) + " ("
				+ World.player.stats.skills.getExpString(Skill.Level) + ")", skin);
		levelLbl.setFontScale(headerScale);
		nameLbl.setFontScale(headerScale);

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
		dialog.add().width(width).row();

		// Stat list
		addTitle(scrollTable, "Stats:");
		for (StatLabels.StatLabel label : statLabels.labels)
			scrollTable.add(label).width(width).padBottom(LINE_PAD).row();

		// Skill list
		addTitle(scrollTable, "Skills:");
		scrollTable.add(createSkillTable()).width(width).row();

		// Reputation list
		addTitle(scrollTable, "Reputation:");
		scrollTable.add(createRepTable()).width(width).row();

		TextButton ok = new TextButton("Close", skin);

		dialog.add(dialogScroll).width(GameDialog.WIDTH).height(400).row();
		dialog.add(ok).width(width).padTop(35).padBottom(LINE_PAD).row();

		ok.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				Gui.overlay.toggleStatsWindow();
			}

		});
	}

	private void addTitle(Table table, String text) {
		Label label = new Label(text, Gui.skin);
		label.setFontScale(headerScale);
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
					.setValue(World.player.reputation.get(faction))
					.setProgressSize(BAR_WIDTH, BAR_HEIGHT))
					.size(BAR_WIDTH, BAR_HEIGHT)
					.padBottom(LINE_PAD)
					.row();
		}
		return repTable;
	}

	private Table createSkillTable() {
		Label skillLbl;
		SkillContainer skills = World.player.stats.skills;
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
