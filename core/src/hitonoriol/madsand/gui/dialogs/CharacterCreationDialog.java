package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameTextSubstitutor;
import hitonoriol.madsand.entities.Stats;
import hitonoriol.madsand.gui.widgets.StatLabels;
import hitonoriol.madsand.lua.Lua;

public class CharacterCreationDialog {
	StatLabels statLabels;
	Stats stats;
	PlayerStatDialog dialog;
	static String titleString = "Character Creation";

	public CharacterCreationDialog() {
		statLabels = new StatLabels();
		stats = statLabels.stats;
		createCharDialog();
	}

	void rollStats() {
		stats.roll();
		statLabels.refreshStatLabels();
		Gui.refreshOverlay();
	}

	void createCharDialog() {
		float width = Gui.defLblWidth;
		rollStats();
		dialog = new PlayerStatDialog(Gui.overlay, statLabels, titleString);
		dialog.restoreOnChange = true;

		TextButton rbtn = new TextButton("Reroll", Gui.skin);
		TextButton cbtn = new TextButton("Create", Gui.skin);
		dialog.add(rbtn).width(width).row();
		dialog.row();
		dialog.add(cbtn).width(width).row();

		cbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {

				if (stats.baseStats.getFreePoints() > 0) {
					Gui.drawOkDialog("You still have unassigned stat points left!");
					return;
				}

				if (!dialog.nameField.getText().trim().equals("")) {
					MadSand.player().setName(dialog.nameField.getText());
					MadSand.player().reinit();
					dialog.remove();
					Gui.gameUnfocused = false;
					Gui.refreshOverlay();
					GameTextSubstitutor.add(GameTextSubstitutor.PLAYER_NAME, dialog.nameField.getText());
					Lua.executeScript(Lua.onCreationScript);
				}
			}

		});
		rbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				rollStats();
			}

		});

	}

	public void show() {
		dialog.show(Gui.overlay);
	}
}
