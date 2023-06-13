package hitonoriol.madsand.dialog;

import static hitonoriol.madsand.util.Strings.getFirstMatch;
import static hitonoriol.madsand.util.Strings.getLastMatch;
import static hitonoriol.madsand.util.Strings.parseRegex;
import static hitonoriol.madsand.util.Strings.removeRegex;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.util.Utils;

public class DialogChainGenerator {
	public static final String DEFAULT_BTN_TEXT = "[Proceed]";

	/* Dialog body delimiter */
	public static final String DIALOG_DELIMITER = "=>";

	public static final String BTN_NEXT_DIALOG = "^"; // Shows which button will call the next dialog
	public static final String DIALOG_BTN_SCRIPT_DELIMITER = "@"; // Everything after this character will be executed as a Lua script
	public static final String DIALOG_BTN_TREE_REPLY_DELIMITER = "$";

	/* Dialog markup tags: */

	/* Defaults(...) -- set default tags for each dialog in chain */
	public static Pattern defaultsPattern = property("Defaults", true);

	/* StageOpacity(float) -- set stage opacity */
	public static Pattern stageBgPattern = property("StageOpacity");

	/* BackgroundOpacity(float) -- set dialog background opacity */
	public static Pattern bgPattern = property("BackgroundOpacity");

	/* FadeIn(float) -- fade id duration in seconds */
	public static Pattern fadeInPattern = property("FadeIn");

	/* FadeOut(float) -- fade id duration in seconds */
	public static Pattern fadeOutPattern = property("FadeOut");

	/* %Title% -- set default title for every dialog in chain after the current one */
	public static Pattern defTitlePattern = Pattern.compile("\\%(.*?)\\%");

	/* #Title# -- set current dialog title */
	public static Pattern titlePattern = Pattern.compile("\\#(.*?)\\#");

	/* [Text] -- specify the "continue" button text */
	public static Pattern buttonPattern = Pattern.compile("\\[(.*?)\\]");

	private String dialogChainString;
	private String defaultTitle = "";
	private String defaults = "";

	private static Pattern property(String name) {
		return property(name, false);
	}

	private static Pattern property(String name, boolean canContainOtherProperties) {
		return Pattern.compile(
			"\\/" + name + "\\(((.|\\n)*"
				+ (canContainOtherProperties ? "" : "?") + ")\\)"
		);
	}

	public DialogChainGenerator(String text) {
		dialogChainString = text;
	}

	/* Returns a reference to the button which calls the next dialog */
	private TextButton generateDialog(String dialogBody, GameDialog dialog) {
		// If there's no button tags within the dialog markup, add the default button
		if (!dialogBody.contains("["))
			dialogBody += DEFAULT_BTN_TEXT;

		// Parse defaults tag
		getFirstMatch(defaultsPattern, dialogBody).ifPresent(
			defaultTags -> {
				defaults = defaultTags;
				Utils.dbg("Set defaults to: %s", defaults);
			}
		);

		// Insert defaults into the dialog markup string
		dialogBody = dialogBody.replaceFirst(defaultsPattern.pattern(), "");
		if (!defaults.isEmpty())
			dialogBody = defaults + dialogBody;

		// Parse default title tag
		getFirstMatch(defTitlePattern, dialogBody).ifPresent(
			this::setDefaultTitle
		);

		// If title is not set, set it to the default one
		dialog.setTitle(getFirstMatch(titlePattern, dialogBody).orElse(defaultTitle));

		getLastMatch(stageBgPattern, dialogBody).ifPresent(
			stageOpacity -> dialog.setStageBackgroundOpacity(Float.parseFloat(stageOpacity))
		);

		getLastMatch(bgPattern, dialogBody).ifPresent(
			bgOpacity -> dialog.setBackgroundOpacity(Float.parseFloat(bgOpacity))
		);

		getLastMatch(fadeInPattern, dialogBody).ifPresent(
			fadeIn -> dialog.setFadeInDuration(Float.parseFloat(fadeIn))
		);

		getLastMatch(fadeOutPattern, dialogBody).ifPresent(
			fadeOut -> dialog.setFadeOutDuration(Float.parseFloat(fadeOut))
		);

		// Parse button patterns
		var nextDialogButton = Widgets.button("");
		parseRegex(dialogBody, buttonPattern, buttonMatcher -> {
			var buttonString = buttonMatcher.group(1);

			// Add a script button - executes specified lua code
			if (buttonString.contains(DIALOG_BTN_SCRIPT_DELIMITER)) {
				Utils.dbg("Contains Script Character! buttonString: [" + buttonString + "]");
				String buttonTokens[] = buttonString.split(DIALOG_BTN_SCRIPT_DELIMITER);
				var scriptButton = Widgets.button(buttonTokens[0]);
				var buttonScriptString = buttonTokens[1];
				dialog.addButton(scriptButton);
				Gui.setAction(scriptButton, () -> Lua.execute(buttonScriptString));
			}

			// Add a "next" button - closes this dialog, opens the next one in chain
			else {
				nextDialogButton.setText(buttonString);
				dialog.addButton(nextDialogButton);
			}
		});

		dialog.setText(unescapeChars(removeDialogRegex(dialogBody)));
		return nextDialogButton;
	}

	private void setDefaultTitle(String title) {
		defaultTitle = title;
	}

	private String removeDialogRegex(String text) {
		var builder = new StringBuilder(text);
		removeRegex(buttonPattern, builder);
		removeRegex(defTitlePattern, builder);
		removeRegex(titlePattern, builder);
		removeRegex(stageBgPattern, builder);
		removeRegex(bgPattern, builder);
		removeRegex(fadeInPattern, builder);
		removeRegex(fadeOutPattern, builder);
		return builder.toString().trim();
	}

	public static String LBRACKET = "-(", RBRACKET = ")-";

	private String unescapeChars(String string) {
		string = string.replace(LBRACKET, "[");
		return string.replace(RBRACKET, "]");
	}

	public DialogChainGenerator setAllTitles(String title) {
		defaultTitle = title;
		return this;
	}

	public GameDialog generate(Stage stage) { // generates a chain of dialogs
		var dialogTokens = new StringTokenizer(dialogChainString, DIALOG_DELIMITER);
		var dialog = new GameDialog("", stage); // Current dialog
		var nextButton = generateDialog(dialogTokens.nextToken(), dialog);
		var firstInChain = dialog;

		GameDialog newDialog = null; // Next dialog in chain
		TextButton newNextButton = null;
		String dialogText; // Current dialog body text
		while (dialogTokens.hasMoreTokens()) {
			dialogText = dialogTokens.nextToken();

			newDialog = new GameDialog("", stage);
			newNextButton = generateDialog(dialogText, newDialog);

			dialog.chainReply(nextButton, newDialog);

			nextButton = newNextButton;
			dialog = newDialog;
		}

		var lastInChain = dialog;
		Gui.setAction(nextButton, () -> lastInChain.hide());
		return firstInChain;
	}
}
