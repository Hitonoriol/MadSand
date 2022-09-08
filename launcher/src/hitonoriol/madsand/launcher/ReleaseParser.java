package hitonoriol.madsand.launcher;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import com.github.rjeschke.txtmark.Processor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ReleaseParser {
	static final String RELEASES_URL = "https://" + NetUtils.API_HOST + "/repos/hitonoriol/madsand/releases";

	/* Version / Date / Changelog */
	private static final String changelogTemplate = "<h1>%s</h1><h3>[%s]</h3>%s<hr>";
	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
			.withLocale(Locale.getDefault())
			.withZone(ZoneId.systemDefault());

	private JsonArray releases;
	private String latestVersion, gameLink;

	public ReleaseParser() {}

	public void refresh() {
		releases = JsonParser.parseString(NetUtils.getResponse(RELEASES_URL)).getAsJsonArray();
		JsonObject latestEntry = getLatestRelease();
		latestVersion = getVersion(latestEntry);
		gameLink = getGameURL(latestEntry);
	}

	private JsonObject getLatestRelease() {
		JsonObject latestRelease = null;
		for (JsonElement entry : releases) {
			if (!getVersion(latestRelease = entry.getAsJsonObject()).contains("launcher"))
				break;
		}
		return latestRelease;
	}

	private String getGameURL(JsonObject entry) {
		return entry.get("assets").getAsJsonArray()
				.get(0).getAsJsonObject()
				.get("browser_download_url").getAsString();
	}

	private String getVersion(JsonObject entry) {
		return entry.get("tag_name").getAsString();
	}

	private String getDate(JsonObject entry) {
		return dateFormatter.format(Instant.parse(entry.get("published_at").getAsString()));
	}

	public String getLatestVersion() {
		return latestVersion;
	}

	public String getChangelog() {
		StringBuilder sb = new StringBuilder();
		releases.forEach(element -> {
			JsonObject release = element.getAsJsonObject();
			String changelog = Processor.process(release.get("body").getAsString());
			sb.append(String.format(changelogTemplate, getVersion(release), getDate(release), changelog));
		});
		return sb.toString();
	}

	public void downloadGame() {
		NetUtils.downloadFile(gameLink, GameLauncher.GAME_FILENAME);
	}
}
