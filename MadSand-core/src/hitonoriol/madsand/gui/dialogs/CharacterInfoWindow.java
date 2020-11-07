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
import hitonoriol.madsand.entities.SkillContainer;
import hitonoriol.madsand.entities.Stats;
import hitonoriol.madsand.enums.Skill;
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

	final static float BAR_WIDTH = 150;
	final static float BAR_HEIGHT = 19;
	static ProgressBarStyle skillStyle = Gui.createProgressBarStyle(BAR_WIDTH, BAR_HEIGHT, Color.LIME);

	Label statsLbl, skillsLbl;
	GameDialog dialog;
	StatLabels statLabels;
	Skin skin = Gui.skin;

	public CharacterInfoWindow() {
		statLabels = new StatLabels();
		dialog = new GameDialog(Gui.overlay);
		statsLbl = new Label("Stats:", skin);
		skillsLbl = new Label("\nSkills:", skin);
		skillsLbl.setFontScale(headerScale);
		statsLbl.setFontScale(headerScale);
		createDialog();
	}

	public void createDialog() {
		//dialog.debugAll();
		Stats stats = World.player.stats;
		float width = Gui.defLblWidth;
		Label nameLbl = new Label(World.player.stats.name, skin);
		Label levelLbl = new Label("Level: " + World.player.stats.skills.getLvl(Skill.Level) + " ("
				+ World.player.stats.skills.getExpString(Skill.Level) + ")", skin);

		levelLbl.setFontScale(headerScale);
		nameLbl.setFontScale(headerScale);
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

		dialog.add(statsLbl).width(width).padLeft(headerLeftPadding).padBottom(headerBottomPadding).row();

		for (StatLabels.StatLabel label : statLabels.labels)
			dialog.add(label).width(width).row();

		dialog.add(skillsLbl).width(width).padLeft(headerLeftPadding).padBottom(headerBottomPadding).row();

		Skill skill;
		Label skillLbl;
		SkillContainer skills = World.player.stats.skills;
		Table skillTable = new Table();
		skillTable.align(Align.left);

		for (int i = 1; i < Skill.values().length; ++i) {
			skill = Skill.values()[i];

			if (skill == Skill.Level)
				continue;

			skillLbl = new Label(skill + ": ", skin);

			skillTable.add(skillLbl)
					.width(130)
					.align(Align.left)
					.padBottom(5);
			skillTable.add(new StatProgressBar(skill.name())
					.setStyle(skillStyle)
					.setRange(0, skills.get(skill).requiredExp)
					.setStatText("LVL " + skills.getLvl(skill))
					.setValue(skills.getExp(skill))
					.setProgressSize(BAR_WIDTH, BAR_HEIGHT))
					.size(BAR_WIDTH, BAR_HEIGHT)
					.padBottom(5);
			skillTable.row();
		}
		dialog.add(skillTable).width(width).row();

		TextButton ok = new TextButton("Close", skin);
		ok.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				dialog.remove();
				dialog.clearActions();
				Gui.overlay.toggleStatsWindow();
			}

		});
		dialog.add(ok).width(width).padTop(35).padBottom(5).row();
	}

	public void show() {
		dialog.show(Gui.overlay);
	}

	public void remove() {
		dialog.remove();
	}
}
