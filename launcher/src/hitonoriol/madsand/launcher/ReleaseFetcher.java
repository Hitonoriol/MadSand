package hitonoriol.madsand.launcher;

import static java.util.stream.StreamSupport.stream;
import static javafx.application.Platform.runLater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.Releases;

import com.github.rjeschke.txtmark.Processor;

import hitonoriol.madsand.commons.exception.Exceptions;
import hitonoriol.madsand.launcher.gui.controller.LauncherLayoutController;
import hitonoriol.madsand.launcher.util.GitHubUtils;
import hitonoriol.madsand.launcher.util.RateLimiter;

public class ReleaseFetcher {
	private static final String GAME_REPO = "Hitonoriol/MadSand";
	private static final int PREV_RELEASES = 100;

	private LauncherLayoutController controller;

	private List<GHRelease> releases = new ArrayList<>();
	private LinkedList<GameVersionEntry> gameVersions;

	public ReleaseFetcher(LauncherLayoutController controller) {
		this.controller = controller;
		gameVersions = controller.getVersionList();
	}

	public CompletableFuture<Void> refresh() {
		setStatusText("Connecting to GitHub...");
		return CompletableFuture.runAsync(Exceptions.asUnchecked(this::connect))
				.thenRunAsync(() -> {
					setStatusText("Processing release entries...");
					parseReleases();
					runLater(() -> {
						setStatusText("Done");
						controller.setChangelogViewContents(getChangelog());
					});
				})
				.exceptionally(e -> {
					setStatusText("Failed to fetch release info from `%s`", GitHubUtils.API_HOST);
					return Exceptions.printStackTrace(e);
				});
	}

	private void connect() throws IOException {
		GitHub github = new GitHubBuilder()
				.withRateLimitChecker(new RateLimiter())
				.build();

		setStatusText("Fetching release info...");
		stream(Releases.get(github, GAME_REPO).withPageSize(PREV_RELEASES).spliterator(), false)
				.limit(PREV_RELEASES)
				.forEach(releases::add);
	}

	private void setStatusText(String format, Object... args) {
		runLater(() -> controller.setStatusText(format, args));
	}

	@SuppressWarnings("deprecation")
	/* No idea why they marked GHRelease#assets() as deprecated as this is the only
	 * way to access cached version of release's asset list. */
	private void parseReleases() {
		var newestCachedVersion = !gameVersions.isEmpty() ? gameVersions.getFirst() : GameVersionEntry.OLDEST;
		getReleases().stream()
				.filter(release -> !release.getTagName().contains("launcher"))
				.map(release -> {
					return new GameVersionEntry(release.getTagName(), release.assets());
				})
				.takeWhile(version -> version.greaterThan(newestCachedVersion))
				.filter(GameVersionEntry::isDownloadable)
				.forEach(gameVersions::addFirst);
		gameVersions.sort(Comparator.reverseOrder());
	}

	private List<GHRelease> getReleases() {
		return releases;
	}

	private String getChangelog() {
		StringBuilder sb = new StringBuilder();
		releases.forEach(release -> {
			String changelog = Processor.process(release.getBody());
			sb.append(String.format("<h1>%s</h1><h3>[%s]</h3>%s<hr>",
					release.getTagName(), GitHubUtils.getPublishedDateString(release), changelog));
		});
		return sb.toString();
	}

	public CompletableFuture<Void> downloadGame(GameVersionEntry game) {
		setStatusText("Downloading `%s`...", game.getFilename());
		return new NetWorker(game.getDownloadURL())
				.downloadFile(game.getFilename(), downloadProgress -> {
					runLater(() -> controller.setActionProgressValue(downloadProgress));
				})
				.thenAccept(gameFile -> {
					setStatusText("Done");
				})
				.exceptionally(e -> {
					setStatusText("Failed to download the game file");
					return Exceptions.printStackTrace(e);
				});
	}
}
