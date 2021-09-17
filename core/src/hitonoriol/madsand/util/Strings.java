package hitonoriol.madsand.util;

import static hitonoriol.madsand.resources.Resources.LINEBREAK;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings {
	public static CharSequence parseRegex(CharSequence str, Pattern regex, Consumer<Matcher> action) {
		Matcher matcher = regex.matcher(str);
		while (matcher.find())
			action.accept(matcher);
		return str;
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
