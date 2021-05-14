package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameDialog;

public class ConfirmDialog extends GameDialog {

	private float WIDTH = 400;
	private float HEIGHT = 175;

	private float BTN_WIDTH = Gui.defLblWidth / 2;
	private float BTN_HEIGHT = 40;

	private Table buttonTable = new Table();
	private TextButton confirmBtn = new TextButton("Confirm", Gui.skin);
	private TextButton cancelBtn = new TextButton("Cancel", Gui.skin);

	private Label msgLabel;

	public ConfirmDialog(String msg, Runnable action, Stage stage) {
		super(stage);
		super.setBackground(Gui.getColorDrawable(Color.DARK_GRAY));
		super.setSize(WIDTH, HEIGHT);
		super.setTitle("Confirm action");
		super.getTitleLabel().setAlignment(Align.center);
		msgLabel = new Label(msg, Gui.skin);
		msgLabel.setWrap(true);
		msgLabel.setAlignment(Align.center);
		super.add(msgLabel).size(WIDTH, HEIGHT).row();

		buttonTable.add(confirmBtn).size(BTN_WIDTH, BTN_HEIGHT).pad(5);
		buttonTable.add(cancelBtn).size(BTN_WIDTH, BTN_HEIGHT).row();

		super.add(buttonTable);

		Gui.setAction(confirmBtn, () -> {
			action.run();
			remove();
		});
		Gui.setAction(cancelBtn, () -> remove());
	}

	public ConfirmDialog(String msg, Runnable action) {
		this(msg, action, null);
	}
}