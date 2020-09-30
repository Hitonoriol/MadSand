package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameDialog;

public class OkDialog extends GameDialog {

	float BUTTON_WIDTH = 100;
	float BUTTON_HEIGHT = 40;

	float BUTTON_PAD_BOTTOM = 10;
	float BUTTON_PAD_TOP = 35;

	float WIDTH = 300;
	float HEIGHT = 200;

	float CONTAINER_PADDING = 5;

	Table dialogContainer;

	boolean isOnTop = false; // If the dialog is displayed on top of another dialog

	private OkDialog(Stage stage) {
		super(stage);
	}

	public OkDialog(String text, Stage stage) {
		this(stage);
		super.clear();
		super.setBackground(Gui.getColorDrawable(Color.DARK_GRAY));

		isOnTop = Gui.dialogActive;

		dialogContainer = new Table();
		textLbl = new Label(text, Gui.skin);
		dialogContainer.setBackground(Gui.getColorDrawable(Color.LIGHT_GRAY));
		dialogContainer.add(textLbl).pad(25).row();

		TextButton okButton = new TextButton("Ok", Gui.skin);

		okButton.align(Align.center);
		dialogContainer.add(okButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).padTop(BUTTON_PAD_TOP)
				.padBottom(BUTTON_PAD_BOTTOM).row();

		super.add(dialogContainer).pad(CONTAINER_PADDING);

		okButton.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				remove();
				Gui.dialogActive = Gui.gameUnfocused = isOnTop;
			}

		});
		
		dialogContainer.addListener(new InputListener() {
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				Gui.gameUnfocused = true;
				Gui.overlay.hideTooltip();
			}
		});
	}

}
