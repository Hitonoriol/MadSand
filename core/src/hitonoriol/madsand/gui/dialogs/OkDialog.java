package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;

public class OkDialog extends GameDialog {
	private final static float BTN_WIDTH = 100, BUTTON_HEIGHT = 40;
	private final static float BUTTON_PAD_BOTTOM = 10, BUTTON_PAD_TOP = 30;
	public final static String DEFAULT_TITLE = "Info";
	private static float LBL_MAX_WIDTH = 350, LBL_MIN_WIDTH = Gui.getTextWidth(DEFAULT_TITLE, Gui.FONT_M);
	private Cell<AutoFocusScrollPane> textCell;
	private Cell<?> optionalCell;
	private boolean fillScreen = false;

	public OkDialog(String title, String text, Stage stage) {
		super(stage);
		super.makeBordered();
		super.align(Align.left);
		super.setTitle(title);

		textLbl = new Label(text, Gui.skin);
		textLbl.setWrap(true);
		textLbl.setFillParent(true);
		textLbl.setAlignment(Align.left);
		textCell = add(new AutoFocusScrollPane(textLbl));
		textCell.align(Align.left)
				.maxHeight(LBL_MAX_WIDTH)
				.pad(25)
				.row();
		optionalCell = add();
		optionalCell.row();
		setFillScreen(false);

		TextButton okButton = new TextButton("Ok", Gui.skin);
		okButton.align(Align.center);
		add(okButton).size(BTN_WIDTH, BUTTON_HEIGHT).padTop(BUTTON_PAD_TOP)
				.padBottom(BUTTON_PAD_BOTTOM).row();

		super.centerTitle();
		Gui.setAction(okButton, () -> remove());
	}

	public OkDialog(String text, Stage stage) {
		this("Info", text, stage);
	}

	@Override
	public void setText(String text) {
		super.setText(text);
		if (fillScreen)
			fillScreen();
	}

	public void setFillScreen(boolean fill) {
		fillScreen = fill;
		final float maxWidth = fill ? Gdx.graphics.getWidth() : LBL_MAX_WIDTH;
		textCell.width(Math.max(LBL_MIN_WIDTH, Math.min(maxWidth, Gui.getTextWidth(textLbl.getText().toString()))));
	}

	public OkDialog fillScreen() {
		setFillScreen(true);
		return this;
	}
	
	public OkDialog setMaxWidth(float maxWidth) {
		LBL_MAX_WIDTH = maxWidth;
		return this;
	}
	
	public Cell<?> addContents(Actor actor) {
		optionalCell.setActor(actor);
		pack();
		return optionalCell;
	}
}
