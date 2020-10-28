package hitonoriol.madsand.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Mouse;
import hitonoriol.madsand.containers.Tuple;
import hitonoriol.madsand.world.World;

public class GameDialog extends Dialog {
	public static final float BTN_WIDTH = Gdx.graphics.getWidth() / 4;
	public static final float BTN_HEIGHT = 35;

	private static final float TITLE_YPADDING = 18;
	private static final float TITLE_XPADDING = 3;
	private static final float TEXT_YPADDING = 15;

	public static final float WIDTH = 500;
	public static final float HEIGHT = 250;
	public static final float PADDING = 10;

	protected Label textLbl;
	private Stage stage;

	@SafeVarargs
	public GameDialog(String title, String text, Stage stage, Tuple<String, GameDialog>... replies) {
		super(title, Gui.skin);
		Table titleTbl = super.getTitleTable();
		Label titleLbl = super.getTitleLabel();
		titleTbl.getCell(titleLbl);
		titleTbl.padTop(TITLE_YPADDING).padLeft(TITLE_XPADDING);

		row();
		textLbl = new Label("", Gui.skin);
		setText(text);
		textLbl.setAlignment(Align.topLeft);
		textLbl.setWrap(true);
		add(textLbl).width(WIDTH).height(HEIGHT).pad(PADDING).padTop(TEXT_YPADDING).row();
		this.stage = stage;
		if (replies.length > 0)
			for (Tuple<String, GameDialog> reply : replies) {
				chainReply(reply.l, reply.r);
			}
	}

	public GameDialog(String text, Stage stage) {
		this("", text, stage);
	}

	public GameDialog(Stage stage) {
		this("", "", stage);
		Cell<Label> cell = getCell(textLbl);
		textLbl.remove();
		getCells().removeValue(cell, true);
		invalidate();
	}

	public GameDialog(String title, String text, Stage stage, String okText) {
		this(title, text, stage);
		addOkButton(okText);
	}

	public void setText(String text) {
		textLbl.setText(replaceDialogConstants(text));
	}

	public void setTitle(String text) {
		super.getTitleLabel().setText(text);
	}

	@Override
	public boolean remove() {
		boolean ret = super.remove();
		Gui.gameResumeFocus(this);
		Mouse.justClicked = false;
		return ret;
	}

	@Override
	public Dialog show(Stage stage) {
		Dialog ret = super.show(stage);
		Gui.gameUnfocus();
		Gui.overlay.hideTooltip();

		return ret;
	}

	public void chainReply(TextButton replyButton, final GameDialog nextDialog) {
		replyButton.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				remove();
				nextDialog.show(stage);
			}
		});
	}

	public void chainReply(String btnText, GameDialog nextDialog) {
		TextButton nextBtn = new TextButton(btnText, Gui.skin);
		addButton(nextBtn);
		chainReply(nextBtn, nextDialog);
	}

	public void addOkButton(String text) {
		TextButton okBtn = new TextButton(text, Gui.skin);
		okBtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				remove();
			}
		});
		addButton(okBtn);
	}

	public void addButton(TextButton button) {
		add(button).width(BTN_WIDTH).height(BTN_HEIGHT).pad(PADDING).row();
	}

	public void show() {
		show(stage);
	}

	public static final String DIALOG_PLAYER_NAME_CONSTANT = "{PLAYER}";
	public static final String DIALOG_LINEBREAK_CONSTANT = "{br}";

	public String replaceDialogConstants(String text) {
		text = text.replace(DIALOG_PLAYER_NAME_CONSTANT, World.player.stats.name);
		text = text.replace(DIALOG_LINEBREAK_CONSTANT, "\n");
		return text;
	}

	public static GameDialog generateDialogChain(String text, Stage stage) {
		return new DialogChainGenerator(text).generate(stage);
	}

	public boolean isOnlyDialog() { // If this dialog is the only one in stage
		return !Gui.hasDialogs(stage, this);
	}

}
