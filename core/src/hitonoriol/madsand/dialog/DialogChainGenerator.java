package hitonoriol.madsand.dialog;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.util.Utils;

public class DialogChainGenerator {
	public static final String DEFAULT_BTN_TEXT = "[Proceed]";

	public static final String DIALOG_TEXT_DELIMITER = "=>";

	public static final String BTN_NEXT_DIALOG = "^"; // Shows which button will call the next dialog
	public static final String DIALOG_BTN_SCRIPT_DELIMITER = "@"; // Everything after this character will be executed as a Lua script
	public static final String DIALOG_BTN_TREE_REPLY_DELIMITER = "$";

	public static final String DIALOG_TITLE_REGEX = "\\#(.*?)\\#"; // #Title# -- set current dialog title
	public static final String DEFAULT_TITLE_REGEX = "\\%(.*?)\\%"; // %Title% -- set default title for every dialog in chain after the current one
	public static final String DIALOG_BUTTON_REGEX = "\\[(.*?)\\]";

	public static Pattern defTitlePattern = Pattern.compile(DEFAULT_TITLE_REGEX);
	public static Pattern titlePattern = Pattern.compile(DIALOG_TITLE_REGEX);
	public static Pattern buttonPattern = Pattern.compile(DIALOG_BUTTON_REGEX);

	private String dialogChainString;
	private String defaultTitle;

	/*
	 * Chain dialog generator Syntax:
	 * %Default title%
	 * #Title# Dialog text [Button text] [Script Button Text @ world.player:fooBar()] => Next Text => ....
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

		String defTitle = getFirstMatch(defTitlePattern, dialogBody);
		String title = getFirstMatch(titlePattern, dialogBody);

		if (defTitle != null)
			defaultTitle = defTitle;

		String buttonTokens[];
		TextButton scriptButton;

		TextButton nextDialogButton = Widgets.button("");

		while (buttonMatcher.find()) {
			buttonString = buttonMatcher.group(1);

			if (buttonString.contains(DIALOG_BTN_SCRIPT_DELIMITER)) {
				Utils.dbg("Contains Script Character! buttonString: [" + buttonString + "]");
				buttonTokens = buttonString.split(DIALOG_BTN_SCRIPT_DELIMITER); // [Button Text @ lua code]
				scriptButton = Widgets.button(buttonTokens[0]);
				final String buttonScriptString = buttonTokens[1];
				dialog.addButton(scriptButton);
				Gui.setAction(scriptButton, () -> Lua.execute(buttonScriptString));
			} else {
				nextDialogButton.setText(buttonString);
				dialog.addButton(nextDialogButton);
			}
		}

		if (title == null) {
			if (defaultTitle != null)
				title = defaultTitle;
			else
				title = "";
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
			return null;
	}

	private StringBuilder removeRegex(Pattern pattern, StringBuilder text) {
		return text.replace(0, text.length(), pattern.matcher(text).replaceAll(""));
	}

	private String removeDialogRegex(String text) {
		StringBuilder builder = new StringBuilder(text);
		removeRegex(buttonPattern, builder);
		removeRegex(defTitlePattern, builder);
		removeRegex(titlePattern, builder);
		return builder.toString().trim();
	}

	public static String LBRACKET = "-(", RBRACKET = ")-";

	private String unescapeChars(String string) {
		string = string.replace(LBRACKET, "[");
		string = string.replace(RBRACKET, "]");
		return string;
	}

	public DialogChainGenerator setAllTitles(String title) {
		defaultTitle = title;
		return this;
	}

	public GameDialog generate(Stage stage) { // generates a chain of dialogs
		StringTokenizer dialogTokens = new StringTokenizer(dialogChainString, DIALOG_TEXT_DELIMITER);

		GameDialog dialog = null; // Current dialog
		GameDialog newDialog = null; // Next dialog in chain
		TextButton nextButton = null;
		TextButton newNextButton = null;

		String dialogText; // Current dialog body text

		dialog = new GameDialog("", stage);
		nextButton = generateDialog(dialogTokens.nextToken(), dialog);
		GameDialog firstInChain = dialog;

		while (dialogTokens.hasMoreTokens()) {
			dialogText = dialogTokens.nextToken();

			newDialog = new GameDialog("", stage);
			newNextButton = generateDialog(dialogText, newDialog);

			dialog.chainReply(nextButton, newDialog);

			nextButton = newNextButton;
			dialog = newDialog;
		}

		GameDialog lastInChain = dialog;
		Gui.setAction(nextButton, () -> lastInChain.hide());
		return firstInChain;
	}
}
