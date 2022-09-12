package hitonoriol.madsand.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarInputStream;

public class GameLauncher {
	private static final String JAVA_BIN = String.format("\"%s/bin/javaw\"", System.getProperty("java.home"));
	public static final String DEFAULT_VM_ARGS = "-Xmx1024m -Xms256m";
	public static final String JAR_DIRECTORY = "MadSand";

	File gameFile;
	private final List<String> launchCmd = new ArrayList<>();

	public GameLauncher(File gameFile) {
		Prefs prefs = Prefs.values();
		prepareLaunchCommand(gameFile.getAbsolutePath(), prefs.vmArgs.getValue(), prefs.gameArgs.getValue());
		this.gameFile = gameFile;
	}

	private void prepareLaunchCommand(String gameFilename, String vmArgs, String gameArgs) {
		launchCmd.add(JAVA_BIN);
		launchCmd.addAll(split(vmArgs));
		launchCmd.add("-jar");
		if (!gameArgs.isEmpty())
			launchCmd.addAll(split(gameArgs));
		launchCmd.add(gameFilename);
	}

	public void launch() {
		var window = LauncherApp.getMainWindow();
		try {
			Process game = new ProcessBuilder(launchCmd)
					.directory(new File(JAR_DIRECTORY))
					.start();
			window.hide();
			game.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			window.show();
		}
	}

	public String getGameVersion() {
		try (JarInputStream jarStream = new JarInputStream(new FileInputStream(gameFile))) {
			return jarStream.getManifest().getMainAttributes().getValue("Implementation-Version");
		} catch (Exception e) {
			e.printStackTrace();
			return "-";
		}
	}

	private static List<String> split(String cmd) {
		return Arrays.asList(cmd.split(" "));
	}

	public static void createGameDirectory() {
		new File(JAR_DIRECTORY).mkdirs();
	}
}
