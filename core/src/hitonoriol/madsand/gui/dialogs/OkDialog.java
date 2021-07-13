package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;

public class OkDialog extends GameDialog {

	float BUTTON_WIDTH = 100;
	float BUTTON_HEIGHT = 40;

	float BUTTON_PAD_BOTTOM = 10;
	float BUTTON_PAD_TOP = 30;

	float WIDTH = 300;
	float HEIGHT = 200;
	float LBL_MAX_WIDTH = 350;

	float CONTAINER_PADDING = 5;

	public static String DEFAULT_TITLE = "Info";

	public OkDialog(String title, String text, Stage stage) {
		super(stage);
		super.makeBordered();
		super.align(Align.left);
		super.setTitle(title);

		textLbl = new Label(text, Gui.skin);
		textLbl.setWrap(true);
		textLbl.setFillParent(true);
		textLbl.setAlignment(Align.left);
		dialogContainer.add(new AutoFocusScrollPane(textLbl))
				.align(Align.left)
				.width(Math.max(LBL_MAX_WIDTH, Gui.getTextWidth(text))).maxHeight(LBL_MAX_WIDTH)
				.pad(25)
				.row();

		TextButton okButton = new TextButton("Ok", Gui.skin);

		okButton.align(Align.center);
		dialogContainer.add(okButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).padTop(BUTTON_PAD_TOP)
				.padBottom(BUTTON_PAD_BOTTOM).row();

		
		super.getTitleLabel().setAlignment(Align.center);

		Gui.setAction(okButton, () -> remove());
	}

	public OkDialog(String text, Stage stage) {
		this(DEFAULT_TITLE, text, stage);
	}

}
