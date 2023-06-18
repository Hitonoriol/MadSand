package hitonoriol.madsand.dialog;

import static hitonoriol.madsand.util.Strings.getFirstMatch;
import static hitonoriol.madsand.util.Strings.getLastMatch;
import static hitonoriol.madsand.util.Strings.parseRegex;
import static hitonoriol.madsand.util.Strings.removeRegex;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.util.Utils;

public class DialogChainGenerator {
	/* Dialog body delimiter */
	public static final String DIALOG_DELIMITER = "=>";
	public static final String SECTION_DELIMITER = "/Lua/";

	public static final String BTN_NEXT_DIALOG = "^"; // Shows which button will call the next dialog
	public static final String DIALOG_BTN_SCRIPT_DELIMITER = "@"; // Everything after this character will be executed as a Lua script
	public static final String DIALOG_BTN_TREE_REPLY_DELIMITER = "$";

	/* Set default tags for each dialog in chain:
	 * 
	 * /Defaults {
	 * 		...
	 * } Defaults
	 * 
	 */
	public static Pattern defaultsPattern = propertyPattern("Defaults", true);

	/* /DefaultTitle(text) -- set default title for every dialog in chain after the current one */
	public static Pattern defTitlePattern = propertyPattern("DefaultTitle");

	/* /Button(Text) -- specify the "continue" button text */
	public static Pattern buttonPattern = propertyPattern("Button");

	/* Script button:
	 * 
	 *  /Button(Text /OnClick: functionName)
	 * 
	 *  Executes lua code on button press
	 */
	public static String onClickTag = "/OnClick:";

	private Map<MarkupTag, MarkupTagAction> tagParsers = new LinkedHashMap<>();

	private String dialogChainString;
	private String scriptString = "";
	private String defaultTitle = "";
	private String defaults = "";

	public DialogChainGenerator(String text) {
		dialogChainString = text;

		/* /Title(text) -- set current dialog title */
		addParser(
			property("Title").setMatchProvider(
				(pattern, dialogBody) -> Optional.of(getFirstMatch(pattern, dialogBody).orElse(defaultTitle))
			),
			(dialog, title) -> dialog.setTitle(title)
		);

		/* /StageOpacity(float) -- set stage opacity */
		addParser(
			property("StageOpacity"),
			(dialog, stageOpacity) -> dialog.setStageBackgroundOpacity(Float.parseFloat(stageOpacity))
		);

		/* /BackgroundOpacity(float) -- set dialog background opacity */
		addParser(
			property("BackgroundOpacity"),
			(dialog, bgOpacity) -> dialog.setBackgroundOpacity(Float.parseFloat(bgOpacity))
		);

		/* /FadeIn(float) -- fade id duration in seconds */
		addParser(
			property("FadeIn"),
			(dialog, fadeIn) -> dialog.setFadeInDuration(Float.parseFloat(fadeIn))
		);

		/* /FadeOut(float) -- fade id duration in seconds */
		addParser(
			property("FadeOut"),
			(dialog, fadeOut) -> dialog.setFadeOutDuration(Float.parseFloat(fadeOut))
		);
	}

	private void addParser(MarkupTag tag, MarkupTagAction parsedAction) {
		tagParsers.put(tag, parsedAction);
	}

	/* Returns a reference to the button which calls the next dialog */
	private TextButton generateDialog(String dialogBody, GameDialog dialog) {
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
		getFirstMatch(defTitlePattern, dialogBody).ifPresent(this::setDefaultTitle);

		// Parse other dialog tags
		final String fDialogBody = dialogBody;
		tagParsers.forEach((tag, parsedAction) -> {
			tag.parse(dialog, fDialogBody, parsedAction);
		});

		// Parse button tags
		var nextDialogButton = Widgets.button("");
		parseRegex(dialogBody, buttonPattern, buttonMatcher -> {
			var button = nextDialogButton.getText().isEmpty() ? nextDialogButton : Widgets.button();
			var buttonString = buttonMatcher.group(1);

			button.setText(buttonString);
			dialog.addButton(button);

			// Parse `/OnClick: functionName` tag
			if (buttonString.contains(onClickTag)) {
				String tokens[] = buttonString.split(onClickTag, 2);
				button.setText(tokens[0].trim());
				Gui.setAction(button, () -> Lua.execute(scriptString + ";" + tokens[1].trim() + "();"));
			}
		});

		// If no button tags were found, add a default "continue" button
		if (!nextDialogButton.hasParent()) {
			nextDialogButton.setText("Proceed");
			dialog.addButton(nextDialogButton);
		}

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
		tagParsers.keySet().forEach(tag -> removeRegex(tag.pattern, builder));
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
		String markupSections[] = dialogChainString.split(SECTION_DELIMITER, 2);
		if (markupSections.length > 1)
			scriptString = markupSections[1];

		var dialogTokens = new StringTokenizer(markupSections[0], DIALOG_DELIMITER);
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

	private static Pattern propertyPattern(String name) {
		return propertyPattern(name, false);
	}

	private static Pattern propertyPattern(String name, boolean canContainOtherProperties) {
		String beginArgs = canContainOtherProperties ? "\\{" : "\\(";
		String endArgs = canContainOtherProperties ? "\\}\\s*" + name + "(\\/|\\n)" : "\\)";
		var pattern = Pattern.compile(
			"\\/" + name + "\\s*"
				+ beginArgs + "((.|\\n)*?)" + endArgs
		);
		Utils.dbg("Created a property regex: %s", pattern.pattern());
		return pattern;
	}

	private static MarkupTag property(String name) {
		return property(name, false);
	}

	private static MarkupTag property(String name, boolean canContainOtherProperties) {
		return new MarkupTag(propertyPattern(name, canContainOtherProperties));
	}

	private interface MarkupTagAction extends BiConsumer<GameDialog, String> {};

	private interface MatchProvider extends BiFunction<Pattern, String, Optional<String>> {};

	private static class MarkupTag {
		private Pattern pattern;
		private MatchProvider matchProvider;

		public MarkupTag(Pattern pattern) {
			this.pattern = pattern;
			matchProvider = (pat, text) -> getLastMatch(pat, text);
		}

		public MarkupTag setMatchProvider(MatchProvider provider) {
			matchProvider = provider;
			return this;
		}

		public void parse(GameDialog dialog, String text, MarkupTagAction action) {
			matchProvider.apply(pattern, text)
				.ifPresent(tagContents -> action.accept(dialog, tagContents));
		}

		public String regex() {
			return pattern.pattern();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof MarkupTag rhs)
				return regex().equals(rhs.regex());
			return false;
		}

		@Override
		public int hashCode() {
			return pattern.pattern().hashCode();
		}
	}
}
