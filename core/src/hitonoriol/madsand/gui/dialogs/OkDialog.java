package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameDialog;

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

	Table dialogContainer;

	public OkDialog(String title, String text, Stage stage) {
		super(stage);
		super.clear();
		super.setBackground(Gui.getColorDrawable(Color.DARK_GRAY));
		super.align(Align.left);
		super.setTitle(title);

		dialogContainer = new Table();
		dialogContainer.align(Align.left);
		textLbl = new Label(text, Gui.skin);
		textLbl.setWrap(true);
		textLbl.setAlignment(Align.left);
		dialogContainer.setBackground(Gui.getColorDrawable(Color.LIGHT_GRAY));
		dialogContainer.add(textLbl).align(Align.left).width(LBL_MAX_WIDTH).pad(25).row();

		TextButton okButton = new TextButton("Ok", Gui.skin);

		okButton.align(Align.center);
		dialogContainer.add(okButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).padTop(BUTTON_PAD_TOP)
				.padBottom(BUTTON_PAD_BOTTOM).row();

		super.add(dialogContainer).pad(CONTAINER_PADDING).padTop(20);
		super.getTitleLabel().setAlignment(Align.center);;

		Gui.setAction(okButton, () -> remove());
	}

	public OkDialog(String text, Stage stage) {
		this(DEFAULT_TITLE, text, stage);
	}

}
