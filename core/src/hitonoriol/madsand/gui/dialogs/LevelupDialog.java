package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.stats.StatLabels;

public class LevelupDialog extends PlayerStatDialog {
	public LevelupDialog() {
		super(Gui.overlay, new StatLabels(), "Level Up!", MadSand.player().stats().baseStats.getMinSum());
		super.nameField.setDisabled(true);
		super.nameField.setText(MadSand.player().stats.name);
		TextButton okButton = Widgets.button("Done");
		super.add(okButton).width(Gui.DEFAULT_WIDTH).row();

		Gui.setAction(okButton, () -> {
			if (!hasUnassignedPoints())
				remove();
		});
	}

}
