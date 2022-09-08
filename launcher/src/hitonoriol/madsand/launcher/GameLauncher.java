package hitonoriol.madsand.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameLauncher {
	static final String GAME_FILENAME = "madsand.jar";
	static final File gameFile = new File(GAME_FILENAME);

	private static final String JAVA_BIN = String.format("\"%s/bin/javaw\"", System.getProperty("java.home"));
	private static final String DEFAULT_VM_ARGS = "-Xmx1024m -Xms256m";

	private final List<String> launchCmd = new ArrayList<>();

	public GameLauncher(String vmArgs, String gameArgs) {
		prepareLaunchCommand(vmArgs, gameArgs);
	}

	public GameLauncher() {
		this(DEFAULT_VM_ARGS, "");
	}
	
	private void prepareLaunchCommand(String vmArgs, String gameArgs) {
		launchCmd.add(JAVA_BIN);
		launchCmd.addAll(split(vmArgs));
		launchCmd.add("-jar");
		if (!gameArgs.isEmpty())
			launchCmd.addAll(split(gameArgs));
		launchCmd.add(GAME_FILENAME);		
	}
	
	public void launch() {
		try {
			Process game = new ProcessBuilder(launchCmd).start();
			Main.getWindow().setVisible(false);
			game.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Main.getWindow().setVisible(true);
		}		
	}
	
	private static List<String> split(String cmd) {
		return Arrays.asList(cmd.split(" "));
	}
	
	public static File gameFile() {
		return gameFile;
	}
}
