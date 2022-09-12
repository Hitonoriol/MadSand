package hitonoriol.madsand.launcher.util;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import org.kohsuke.github.GHRelease;

public class GitHubUtils {
	public static final String API_HOST = "api.github.com";
	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
			.withLocale(Locale.getDefault())
			.withZone(ZoneId.systemDefault());
	
	public static String getPublishedDateString(GHRelease release) {
		return dateFormatter.format(release.getPublished_at().toInstant());
	}
}
