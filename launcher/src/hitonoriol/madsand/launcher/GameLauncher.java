package hitonoriol.madsand.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarInputStream;

import hitonoriol.madsand.launcher.gui.GuiUtils;
import hitonoriol.madsand.launcher.gui.Layout;
import hitonoriol.madsand.launcher.gui.controller.ConsoleLayoutController;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GameLauncher {
	private static final String JAVA_BIN = String.format("%s/bin/java", System.getProperty("java.home"));
	public static final String DEFAULT_VM_ARGS = "-Xmx1024m -Xms256m";
	public static final String JAR_DIRECTORY = "MadSand";

	private File gameFile;
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
		launchCmd.add(gameFilename);
		if (!gameArgs.isEmpty())
			launchCmd.addAll(split(gameArgs));
	}

	public void launch() {
		var window = LauncherApp.getMainWindow();
		try {
			ProcessBuilder builder = new ProcessBuilder(launchCmd);
			boolean showingConsole = Prefs.values().showConsole.getValue();
			if (showingConsole)
				builder.redirectErrorStream(true);

			Process game = builder
					.directory(new File(JAR_DIRECTORY))
					.start();
			window.hide();
			
			if (showingConsole)
				attachConsole(game);
			game.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			window.show();
		}
	}

	private void attachConsole(Process game) {
		Stage consoleWindow = GuiUtils.loadLayout(Layout.Console,
				new ConsoleLayoutController(game.getInputStream()));
		consoleWindow.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, event -> game.destroy());
		consoleWindow.setTitle(gameFile.getName());
		consoleWindow.showAndWait();
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
