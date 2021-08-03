package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameTextSubstitutor;
import hitonoriol.madsand.entities.Stats;
import hitonoriol.madsand.gui.widgets.stats.StatLabels;
import hitonoriol.madsand.lua.Lua;

public class CharacterCreationDialog {
	StatLabels statLabels;
	Stats stats;
	PlayerStatDialog dialog;
	static String titleString = "Character Creation";

	public CharacterCreationDialog() {
		statLabels = new StatLabels();
		stats = statLabels.getStats();
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

		TextButton statRollBtn = new TextButton("Reroll", Gui.skin);
		TextButton createCharBtn = new TextButton("Create", Gui.skin);
		dialog.add(statRollBtn).width(width).row();
		dialog.row();
		dialog.add(createCharBtn).width(width).row();

		Gui.setAction(createCharBtn, () -> {
			if (dialog.hasUnassignedPoints())
				return;

			if (!dialog.nameField.getText().trim().equals("")) {
				MadSand.player().setName(dialog.nameField.getText());
				MadSand.player().reinit();
				dialog.remove();
				Gui.gameUnfocused = false;
				Gui.refreshOverlay();
				GameTextSubstitutor.add(GameTextSubstitutor.PLAYER_NAME, dialog.nameField.getText());
				Lua.executeScript(Lua.onCreationScript);
			}
		});
		Gui.setAction(statRollBtn, () -> rollStats());
	}

	public void show() {
		dialog.show(Gui.overlay);
	}
}
