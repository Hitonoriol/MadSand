package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.Stage;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.world.Settlement;

public class LandDialog extends GameDialog {

	private Settlement settlement;

	private LandDialog(Stage stage) {
		super(stage);
	}

	public LandDialog(Settlement settlement) {
		super(Gui.overlay);
		this.settlement = settlement;

	}

}
