package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gui.widgets.StatLabels;

public class LevelupDialog extends PlayerStatDialog {
	static String title = "Level Up!";
	static String okButtonText = "Done";

	private LevelupDialog(Stage stage, StatLabels statLabels, String title) {
		super(stage, statLabels, title, MadSand.player().stats.baseStats.maxStatSum - 1);
	}

	public LevelupDialog() {
		this(Gui.overlay, new StatLabels(), title);
		super.nameField.setDisabled(true);
		super.nameField.setText(MadSand.player().stats.name);
		TextButton okButton = new TextButton(okButtonText, Gui.skin);
		super.add(okButton).width(Gui.defLblWidth).row();

		okButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				remove();
			}
		});
	}

}
