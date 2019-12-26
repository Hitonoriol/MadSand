package ru.bernarder.fallenrisefromdust.dialog;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import ru.bernarder.fallenrisefromdust.Gui;
import ru.bernarder.fallenrisefromdust.containers.Tuple;

public class GameDialog extends Dialog {
	public static final float BTN_WIDTH = Gdx.graphics.getWidth() / 4;
	public static final float BTN_HEIGHT = 35;

	private static final float TITLE_YPADDING = 18;
	private static final float TITLE_XPADDING = 3;
	private static final float TEXT_YPADDING = 15;

	public static final float WIDTH = 500;
	public static final float HEIGHT = 250;
	public static final float PADDING = 10;

	private Label textLbl;
	private Stage stage;

	@SafeVarargs
	public GameDialog(String title, String text, Stage stage, Tuple<String, GameDialog>... replies) {
		super(title, Gui.skin);
		Table titleTbl = super.getTitleTable();
		Label titleLbl = super.getTitleLabel();
		titleTbl.getCell(titleLbl);
		titleTbl.padTop(TITLE_YPADDING).padLeft(TITLE_XPADDING);

		row();
		textLbl = new Label(text, Gui.skin);
		textLbl.setAlignment(Align.topLeft);
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

	public GameDialog(String title, String text, Stage stage, String okText) {
		this(title, text, stage);
		addOkButton(okText);
	}

	public void chainReply(String btnText, GameDialog nextDialog) {
		TextButton nextBtn = new TextButton(btnText, Gui.skin);
		nextBtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				nextDialog.show(stage);
				remove();
			}
		});
		addButton(nextBtn);
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

	public static final String DEFAULT_BTN_TEXT = "Proceed";
	public static final String DEFAULT_TITLE_TEXT = "";

	public static final String DIALOG_TEXT_DELIMITER = "=>";

	public static final String DIALOG_TITLE_REGEX = "\\#(.*?)\\#";
	public static final String DIALOG_BUTTON_REGEX = "\\[(.*?)\\]";

	public static Pattern titlePattern = Pattern.compile(DIALOG_TITLE_REGEX);
	public static Pattern buttonPattern = Pattern.compile(DIALOG_BUTTON_REGEX);

	/*
	 * Chain dialog generator Syntax: #Title# Dialog text [Button text] => Next Text
	 * => ....
	 */

	public static Tuple<String, String> getDialogParams(String text) {
		String buttonText, titleText;

		buttonText = getFirstMatch(buttonPattern, text);
		titleText = getFirstMatch(titlePattern, text);

		if (buttonText == "")
			buttonText = DEFAULT_BTN_TEXT;
		if (titleText == "")
			titleText = DEFAULT_TITLE_TEXT;

		return new Tuple<String, String>(titleText, buttonText);
	}

	public static String getFirstMatch(Pattern pattern, String haystack) {
		Matcher matcher = pattern.matcher(haystack);
		if (matcher.find())
			return matcher.group(1);
		else
			return "";
	}

	public static String removeDialogRegex(String text) {
		Matcher matcher;
		String match;
		matcher = titlePattern.matcher(text);
		if (matcher.find()) {
			match = text.substring(matcher.start(), matcher.end());
			text = text.replace(match, "");
		}
		matcher = buttonPattern.matcher(text);
		if (matcher.find()) {
			match = text.substring(matcher.start(), matcher.end());
			text = text.replace(match, "");
		}
		return text.trim();
	}

	public static GameDialog generateDialogChain(String text, Stage stage) { // generates a chain of one-button dialogs
		GameDialog dialog = null;
		GameDialog newDialog;
		StringTokenizer dialogTokens = new StringTokenizer(text, DIALOG_TEXT_DELIMITER);
		String dialogText, buttonText, newButtonText, titleText;
		Tuple<String, String> params;

		dialogText = dialogTokens.nextToken();
		params = getDialogParams(dialogText);
		dialogText = removeDialogRegex(dialogText);
		buttonText = params.r;
		dialog = new GameDialog(params.l, dialogText, stage);
		GameDialog ret = dialog;

		while (dialogTokens.hasMoreTokens()) {
			dialogText = dialogTokens.nextToken();

			params = getDialogParams(dialogText);
			newButtonText = params.r;
			titleText = params.l;

			dialogText = removeDialogRegex(dialogText);

			newDialog = new GameDialog(titleText, dialogText, stage);
			dialog.chainReply(buttonText, newDialog);
			buttonText = newButtonText;

			dialog = newDialog;
		}

		dialog.addOkButton(DEFAULT_BTN_TEXT);
		return ret;
	}

}
