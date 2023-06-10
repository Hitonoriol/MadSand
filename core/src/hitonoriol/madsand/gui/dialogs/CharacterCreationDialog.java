package hitonoriol.madsand.gui.dialogs;

import static hitonoriol.madsand.gui.Gui.BTN_HEIGHT;
import static hitonoriol.madsand.gui.Gui.DEFAULT_WIDTH;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Stats;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.stats.StatLabels;

public class CharacterCreationDialog extends PlayerStatDialog {
	private Stats stats;
	private static String titleString = "Character Creation";
	private static final int MIN_STAT_VALUE = 2; 

	public CharacterCreationDialog() {
		super(Gui.overlay, new StatLabels(), titleString);
		stats = statLabels.getStats();
		createCharDialog();
	}

	void rollStats() {
		stats.randomize(0, MIN_STAT_VALUE);
		statLabels.refreshStatLabels();
		Gui.refreshOverlay();
	}

	void createCharDialog() {
		rollStats();
		restoreOnChange = true;

		TextButton statRollBtn = Widgets.button("Reroll");
		TextButton createCharBtn = Widgets.button("Create");
		defaults().size(DEFAULT_WIDTH, BTN_HEIGHT * 2);
		add(statRollBtn).row();
		add(createCharBtn).row();

		Gui.setAction(createCharBtn, () -> {
			if (hasUnassignedPoints())
				return;

			String name = nameField.getText().trim();
			if (!name.isEmpty()) {
				remove();
				MadSand.player().setName(name);
				MadSand.world().finishPlayerCreation();
			}
		});
		Gui.setAction(statRollBtn, () -> rollStats());
	}
}
