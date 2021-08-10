package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameTextSubstitutor;
import hitonoriol.madsand.entities.Stats;
import hitonoriol.madsand.gui.widgets.stats.StatLabels;
import hitonoriol.madsand.lua.Lua;

public class CharacterCreationDialog extends PlayerStatDialog {
	Stats stats;
	static String titleString = "Character Creation";

	public CharacterCreationDialog() {
		super(Gui.overlay, new StatLabels(), titleString);
		stats = statLabels.getStats();
		createCharDialog();
	}

	void rollStats() {
		stats.randomize();
		statLabels.refreshStatLabels();
		Gui.refreshOverlay();
	}

	void createCharDialog() {
		float width = Gui.defLblWidth;
		rollStats();
		restoreOnChange = true;

		TextButton statRollBtn = new TextButton("Reroll", Gui.skin);
		TextButton createCharBtn = new TextButton("Create", Gui.skin);
		add(statRollBtn).width(width).row();
		add(createCharBtn).width(width).row();

		Gui.setAction(createCharBtn, () -> {
			if (hasUnassignedPoints())
				return;

			String name = nameField.getText().trim();
			if (!name.equals("")) {
				MadSand.player().setName(name);
				MadSand.player().reinit();
				remove();
				Gui.refreshOverlay();
				GameTextSubstitutor.add(GameTextSubstitutor.PLAYER_NAME, name);
				Lua.executeScript(Lua.onCreationScript);
			}
		});
		Gui.setAction(statRollBtn, () -> rollStats());
	}
}
