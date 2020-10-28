package hitonoriol.madsand.dialog;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.Utils;

public class DialogChainGenerator {
	public static final String DEFAULT_BTN_TEXT = "[Proceed]";
	public static final String DEFAULT_TITLE_TEXT = "";

	public static final String DIALOG_TEXT_DELIMITER = "=>";

	public static final String BTN_NEXT_DIALOG = "^"; // Shows which button will call the next dialog
	public static final String DIALOG_BTN_SCRIPT_DELIMITER = "@"; // Everything after this character will be executed as a Lua script
	public static final String DIALOG_BTN_TREE_REPLY_DELIMITER = "$";

	public static final String DIALOG_TITLE_REGEX = "\\#(.*?)\\#";
	public static final String DIALOG_BUTTON_REGEX = "\\[(.*?)\\]";

	public static Pattern titlePattern = Pattern.compile(DIALOG_TITLE_REGEX);
	public static Pattern buttonPattern = Pattern.compile(DIALOG_BUTTON_REGEX);

	private String dialogChainString;

	/*
	 * Chain dialog generator Syntax:
	 * 
	 * #Title# Dialog text [Button text] [Script Button Text @
	 * world.player:fooBar()] => Next Text => ....
	 * 
	 */

	public DialogChainGenerator(String text) {
		dialogChainString = text;
	}

	private TextButton generateDialog(String dialogBody, GameDialog dialog) { // returns a reference to the button which calls the next dialog
		if (!dialogBody.contains("["))
			dialogBody += DEFAULT_BTN_TEXT;
		Matcher buttonMatcher = buttonPattern.matcher(dialogBody);
		String buttonString;

		String title = getFirstMatch(titlePattern, dialogBody);
		if (title.equals(""))
			title = DEFAULT_TITLE_TEXT;

		String buttonTokens[];
		TextButton scriptButton;

		TextButton nextDialogButton = new TextButton("", Gui.skin);

		while (buttonMatcher.find()) {
			buttonString = buttonMatcher.group(1);

			if (buttonString.contains(DIALOG_BTN_SCRIPT_DELIMITER)) {
				Utils.out("Contains Script Character! buttonString: [" + buttonString + "]");
				buttonTokens = buttonString.split(DIALOG_BTN_SCRIPT_DELIMITER); // [Button Text @ lua code]
				scriptButton = new TextButton(buttonTokens[0], Gui.skin);
				final String buttonScriptString = buttonTokens[1];
				dialog.addButton(scriptButton);
				scriptButton.addListener(new ChangeListener() {

					@Override
					public void changed(ChangeEvent event, Actor actor) {
						dialog.remove();
						LuaUtils.execute(buttonScriptString);
					}

				});

			} else {
				nextDialogButton.setText(buttonString);
				dialog.addButton(nextDialogButton);
				nextDialogButton.addListener(new ChangeListener() {

					@Override
					public void changed(ChangeEvent event, Actor actor) {
						dialog.remove();

					}
				});

			}
		}

		dialog.setTitle(title);
		dialog.setText(unescapeChars(removeDialogRegex(dialogBody)));

		return nextDialogButton;
	}

	private String getFirstMatch(Pattern pattern, String haystack) {
		Matcher matcher = pattern.matcher(haystack);
		if (matcher.find())
			return matcher.group(1);
		else
			return "";
	}

	private String removeDialogRegex(String text) {
		Matcher matcher;
		String match;
		matcher = titlePattern.matcher(text);
		if (matcher.find()) {
			match = text.substring(matcher.start(), matcher.end());
			text = text.replace(match, "");
		}

		matcher = buttonPattern.matcher(text);
		while (matcher.find()) {
			match = text.substring(matcher.start(), matcher.end());
			text = text.replace(match, "");
		}
		return text.trim();
	}

	public static String LBRACKET = "-(", RBRACKET = ")-";

	private String unescapeChars(String string) {
		string = string.replace(LBRACKET, "[");
		string = string.replace(RBRACKET, "]");
		return string;
	}

	public GameDialog generate(Stage stage) { // generates a chain of dialogs
		StringTokenizer dialogTokens = new StringTokenizer(dialogChainString, DIALOG_TEXT_DELIMITER);

		GameDialog dialog; // Current dialog
		GameDialog newDialog; // Next dialog in chain
		TextButton nextButton;
		TextButton newNextButton;

		String dialogText; // Current dialog body text

		dialog = new GameDialog("", stage);
		nextButton = generateDialog(dialogTokens.nextToken(), dialog);
		GameDialog ret = dialog;

		while (dialogTokens.hasMoreTokens()) {
			dialogText = dialogTokens.nextToken();

			newDialog = new GameDialog("", stage);
			newNextButton = generateDialog(dialogText, newDialog);

			dialog.chainReply(nextButton, newDialog);

			nextButton = newNextButton;
			dialog = newDialog;
		}

		//dialog.addOkButton(DEFAULT_BTN_TEXT);
		return ret;
	}
}
