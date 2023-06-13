package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.Widgets;

public class ConfirmDialog extends GameDialog {

	private float WIDTH = 400;
	private float HEIGHT = 175;

	private float BTN_WIDTH = Gui.DEFAULT_WIDTH / 2;
	private float BTN_HEIGHT = 40;

	private Table buttonTable = Widgets.table();
	private TextButton confirmBtn = Widgets.button("Confirm");
	private TextButton cancelBtn = Widgets.button("Cancel");

	private Label msgLabel;

	public ConfirmDialog(String msg, Runnable action, Stage stage) {
		super(stage);
		super.setBackground(GuiSkin.getColorDrawable(Color.DARK_GRAY));
		super.setSize(WIDTH, HEIGHT);
		super.setTitle("Confirm action");
		super.getTitleLabel().setAlignment(Align.center);
		msgLabel = Widgets.label(msg);
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
		Gui.setAction(cancelBtn, this::remove);
	}

	public ConfirmDialog(String msg, Runnable action) {
		this(msg, action, null);
	}
}