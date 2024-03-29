package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.util.Utils;

public class OkDialog extends GameDialog {
	private final static float BTN_WIDTH = 100, BUTTON_HEIGHT = 40;
	private final static float BUTTON_PAD_BOTTOM = 10, BUTTON_PAD_TOP = 30;
	public final static String DEFAULT_TITLE = "Info";
	private static float LBL_MIN_WIDTH = Gui.getTextWidth(DEFAULT_TITLE, Gui.FONT_M);
	private Cell<?> textCell;
	private Cell<?> optionalCell;
	private boolean fillScreen = false;

	public OkDialog(String title, String text, Stage stage) {
		super(stage);
		super.makeBordered();
		super.align(Align.left);
		super.setTitle(title);

		super.centerTitle();
		textLbl = Widgets.typingLabel(text);
		textLbl.setWrap(true);
		textCell = add(new AutoFocusScrollPane(textLbl));
		textCell.align(Align.left)
			.maxHeight(Gui.screenHeight(0.3f))
			.pad(25)
			.row();
		optionalCell = add();
		optionalCell.row();
		var okCell = addButton("Ok", this::hide);
		okCell
			.size(BTN_WIDTH, BUTTON_HEIGHT)
			.padTop(BUTTON_PAD_TOP)
			.padBottom(BUTTON_PAD_BOTTOM)
			.row();
		layout();
		updateWidth(false);
		layout();
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

	public void updateWidth(boolean fill) {
		fillScreen = fill;
		final float maxWidth = fill ? Gdx.graphics.getWidth() : Gui.screenWidth(0.3f);
		var prefWidth = Math.max(
			LBL_MIN_WIDTH,
			Math.min(
				maxWidth,
				Gui.getTextWidth(textLbl.storedText)
			)
		);
		textCell.minWidth(prefWidth);
		Utils.out("`%s`: %f", textLbl.storedText, Gui.getTextWidth(textLbl.storedText));
		Utils.out("Chose width: %f", prefWidth);
	}

	public OkDialog fillScreen() {
		updateWidth(true);
		return this;
	}

	public Cell<?> addContents(Actor actor) {
		optionalCell.setActor(actor);
		pack();
		return optionalCell;
	}

	@Override
	public void show() {
		super.show();
		Utils.out("Label: %f", textLbl.getWidth());
	}
}
