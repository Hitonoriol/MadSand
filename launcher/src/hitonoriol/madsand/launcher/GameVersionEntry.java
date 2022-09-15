package hitonoriol.madsand.launcher;

import static java.lang.Character.isAlphabetic;
import static java.lang.Character.isDigit;

import java.io.File;
import java.util.List;

import org.kohsuke.github.GHAsset;

import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.Semver.SemverType;

public class GameVersionEntry implements Comparable<GameVersionEntry> {
	public static final GameVersionEntry OLDEST = new GameVersionEntry("0.0.0");
	
	private Semver version;
	private String filename;
	private String downloadLink;

	public GameVersionEntry(String version, List<GHAsset> assets) {
		this(version);
		assets.stream()
				.filter(GameVersionEntry::isGameJar)
				.findFirst().ifPresent(this::extractLink);
	}

	private GameVersionEntry(String version) {
		this.version = toSemver(version);
	}
	
	private void extractLink(GHAsset asset) {
		downloadLink = asset.getBrowserDownloadUrl();
		filename = GameLauncher.JAR_DIRECTORY + File.separator + asset.getName();
	}

	public boolean isDownloadable() {
		return downloadLink != null;
	}

	public String getDownloadURL() {
		return downloadLink;
	}

	public String getVersionString() {
		return version.toString();
	}

	public Semver getVersion() {
		return version;
	}

	public String getFilename() {
		return filename;
	}

	public File file() {
		return new File(filename);
	}

	public boolean greaterThan(GameVersionEntry versionEntry) {
		return version.isGreaterThan(versionEntry.version);
	}
	
	@Override
	public int compareTo(GameVersionEntry rhs) {
		return version.compareTo(rhs.version);
	}

	@Override
	public String toString() {
		String str = getVersionString();
		if (!file().exists())
			str += " (not installed)";
		return str;
	}

	@Override
	public boolean equals(Object rhs) {
		return rhs instanceof GameVersionEntry && version.equals(((GameVersionEntry) rhs).version);
	}

	@Override
	public int hashCode() {
		return version.hashCode();
	}

	private static boolean isGameJar(GHAsset asset) {
		String filename = asset.getName();
		return filename.contains("MadSand") && filename.contains(".jar");
	}

	private static Semver toSemver(String version) {
		/* Rewrite `v1.2.3abc` -> `v1.2.3-abc` */
		for (int i = 0, length = version.length(); i < length; ++i)
			if (i + 1 < length && isDigit(version.charAt(i)) && isAlphabetic(version.charAt(i + 1))) {
				version = String.format("%s-%s", version.substring(0, i + 1), version.substring(i + 1));
				break;
			}
		/* Remove leading letter before the version number */
		if (Character.isAlphabetic(version.charAt(0)))
			version = version.substring(1);
		return new Semver(version, SemverType.LOOSE);
	}
}
