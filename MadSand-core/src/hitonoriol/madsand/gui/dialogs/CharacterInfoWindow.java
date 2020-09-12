package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.enums.Skill;
import hitonoriol.madsand.gui.widgets.StatLabels;
import hitonoriol.madsand.world.World;

/*
 * Character info in-game menu 
 */

public class CharacterInfoWindow {
	private final static float headerLeftPadding = -45f;
	private final static float headerBottomPadding = 5f;
	private final static float headerScale = 1.11f;

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
		float width = Gui.defLblWidth;
		Label nameLbl = new Label(World.player.stats.name, skin);
		Label levelLbl = new Label("Level: " + World.player.stats.skills.getLvl(Skill.Level) + " ("
				+ World.player.stats.skills.getExpString(Skill.Level) + ")", skin);

		levelLbl.setFontScale(headerScale);
		nameLbl.setFontScale(headerScale);
		dialog.add(nameLbl).row();
		TextButton ok = new TextButton("Close", skin);
		ok.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				dialog.remove();
				dialog.clearActions();
			}

		});
		dialog.setBackground(Gui.darkBackground);
		dialog.setMovable(true);
		dialog.add(new Label("", skin));
		statLabels.refreshStatLabels();
		dialog.row();
		dialog.add(levelLbl);
		dialog.row();
		dialog.add(new Label("", skin)).width(width).row();
		dialog.row();
		dialog.add(statsLbl).width(width).padLeft(headerLeftPadding).padBottom(headerBottomPadding).row();
		dialog.row();
		dialog.add(statLabels.hpStatLbl).width(width).row();
		dialog.row();
		dialog.add(statLabels.staminaStatLbl).width(width).row();
		dialog.row();
		dialog.add(statLabels.strStatLbl).width(width).row();
		dialog.row();
		dialog.add(statLabels.accStatLbl).width(width).row();
		dialog.row();
		dialog.add(statLabels.intStatLbl).width(width).row();
		dialog.row();
		dialog.add(statLabels.luckStatLbl).width(width).row();
		dialog.row();
		dialog.add(statLabels.dexStatLbl).width(width).row();
		dialog.row();
		dialog.add(skillsLbl).width(width).padLeft(headerLeftPadding).padBottom(headerBottomPadding).row();
		dialog.row();
		
		Skill skill;
		Label skillLbl;

		for (int i = 1; i < Skill.len(); ++i) {
			skill = Skill.get(i);
			
			if (skill == Skill.Level)
				continue;
			
			skillLbl = new Label(skill + ": " + World.player.stats.skills.getLvlString(skill), skin);
			
			dialog.add(skillLbl).width(width).row();
			dialog.row();
		}

		dialog.add(ok).width(width).padTop(35).padBottom(5).row();
	}

	public void show() {
		dialog.show(Gui.overlay);
	}
	
	public void remove() {
		dialog.remove();
	}
}
