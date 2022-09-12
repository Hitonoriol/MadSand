package hitonoriol.madsand.launcher;

import static java.util.stream.StreamSupport.stream;
import static javafx.application.Platform.runLater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.Releases;

import com.github.rjeschke.txtmark.Processor;

import hitonoriol.madsand.launcher.gui.controller.LauncherLayoutController;
import hitonoriol.madsand.launcher.util.Exceptions;
import hitonoriol.madsand.launcher.util.GitHubUtils;
import hitonoriol.madsand.launcher.util.RateLimiter;

public class ReleaseFetcher {
	private static final String GAME_REPO = "Hitonoriol/MadSand";
	private static final int PREV_RELEASES = 10;

	private LauncherLayoutController controller;

	private int maxPrevReleases = PREV_RELEASES;
	private List<GHRelease> releases = new ArrayList<>();
	private List<GameVersionEntry> gameVersions;

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
				.withRateLimitChecker(new RateLimiter(maxPrevReleases))
				.build();
		
		setStatusText("Fetching release info...");
		stream(Releases.get(github, GAME_REPO).withPageSize(maxPrevReleases).spliterator(), false)
				.limit(maxPrevReleases)
				.forEach(releases::add);
	}

	private void setStatusText(String format, Object... args) {
		runLater(() -> controller.setStatusText(format, args));
	}

	@SuppressWarnings("deprecation")
	/* No idea why they marked GHRelease#assets() as deprecated as this is the only
	 * way to access cached version of release's asset list. */
	private void parseReleases() {
		getReleases().stream()
				.filter(release -> !release.getTagName().contains("launcher"))
				.map(release -> {
					return new GameVersionEntry(release.getTagName(), release.assets());
				})
				.filter(GameVersionEntry::isDownloadable)
				.forEach(gameVersions::add);
		gameVersions.sort(Comparator.reverseOrder());
		Set<GameVersionEntry> versionSet = new LinkedHashSet<>(gameVersions);
		gameVersions.clear();
		gameVersions.addAll(versionSet);
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
