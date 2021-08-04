package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gui.widgets.stats.StatLabels;

public class LevelupDialog extends PlayerStatDialog {
	public LevelupDialog() {
		super(Gui.overlay, new StatLabels(), "Level Up!", MadSand.player().stats().baseStats.getMinSum());
		super.nameField.setDisabled(true);
		super.nameField.setText(MadSand.player().stats.name);
		TextButton okButton = new TextButton("Done", Gui.skin);
		super.add(okButton).width(Gui.defLblWidth).row();

		Gui.setAction(okButton, () -> {
			if (!hasUnassignedPoints())
				remove();
		});
	}

}
