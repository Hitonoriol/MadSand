package hitonoriol.madsand.launcher;

import java.io.File;
import java.util.List;

import org.kohsuke.github.GHAsset;

import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.Semver.SemverType;

public class GameVersionEntry implements Comparable<GameVersionEntry> {
	private Semver version;
	private String filename;
	private String downloadLink;

	public GameVersionEntry(String version, List<GHAsset> assets) {
		this.version = toSemver(version);
		assets.stream()
				.filter(GameVersionEntry::isGameJar)
				.findFirst().ifPresent(this::extractLink);
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
		int lastIdx = version.length() - 1;
		char lastChar = version.charAt(lastIdx);
		/* Rewrite `v1.2.3a` -> `v1.2.3-a` */
		if (Character.isAlphabetic(lastChar))
			version = String.format("%s-%s", version.substring(0, lastIdx), Character.toString(lastChar));
		if (Character.isAlphabetic(version.charAt(0)))
			version = version.substring(1);
		return new Semver(version, SemverType.LOOSE);
	}
}
