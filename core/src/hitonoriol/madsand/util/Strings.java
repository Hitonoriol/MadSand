package hitonoriol.madsand.util;

import static hitonoriol.madsand.resources.Resources.LINEBREAK;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings {
	public static CharSequence parseRegex(CharSequence str, Pattern regex, Consumer<Matcher> action) {
		var matcher = regex.matcher(str);
		while (matcher.find())
			action.accept(matcher);
		return str;
	}

	public static Optional<String> getFirstMatch(Pattern pattern, String haystack) {
		return getMatch(pattern, haystack, false);
	}

	public static Optional<String> getLastMatch(Pattern pattern, String haystack) {
		return getMatch(pattern, haystack, true);
	}

	private static Optional<String> getMatch(Pattern pattern, String haystack, boolean last) {
		var matcher = pattern.matcher(haystack);
		Optional<String> match = Optional.empty();
		while (matcher.find()) {
			match = Optional.of(matcher.group(1));
			if (!last)
				return match;
		}
		return match;
	}

	public static StringBuilder removeRegex(Pattern pattern, StringBuilder text) {
		return text.replace(0, text.length(), pattern.matcher(text).replaceAll(""));
	}

	public static StringBuilder clearBuilder(StringBuilder sb) {
		sb.setLength(0);
		return sb;
	}

	public static boolean builderEquals(StringBuilder builder, String contents) {
		return builder.length() == contents.length() && builder.indexOf(contents) == 0;
	}

	public static StringBuilder newLine(StringBuilder sb) {
		return sb.append(LINEBREAK);
	}
}
