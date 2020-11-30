package hitonoriol.madsand.desktop;

import java.io.File;
import java.io.PrintStream;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;

public class Launcher {

	static LwjglApplicationConfiguration config;

	public static void main(String[] args) throws Exception {
		config = new LwjglApplicationConfiguration();
		config.resizable = false;

		config.addIcon("icon-256.png", FileType.Internal);
		config.addIcon("icon-64.png", FileType.Internal);
		config.addIcon("icon-32.png", FileType.Internal);

		// config.setFromDisplayMode(LwjglApplicationConfiguration.getDesktopDisplayMode());

		config.vSyncEnabled = true;
		config.width = 1280;
		config.height = 720;
		config.foregroundFPS = 59;
		config.backgroundFPS = -1;

		applyArgs(args);

		new com.badlogic.gdx.backends.lwjgl.LwjglApplication(new MadSand(), config);
	}

	final static String debugFlag = "debug";
	final static String fullscreenFlag = "fullscreen";

	private static void applyArgs(String[] args) throws Exception{
		if (args.length == 0)
			return;

		ArgParser parser = new ArgParser(args);

		Utils.debugMode = parser.argExists(debugFlag);
		config.fullscreen = parser.argExists(fullscreenFlag);

		if (config.fullscreen)
			config.setFromDisplayMode(LwjglApplicationConfiguration.getDesktopDisplayMode());
		
		if (!Utils.debugMode) {
			System.setOut(new PrintStream(new File(Resources.OUTFILE)));
			System.setErr(new PrintStream(new File(Resources.ERRFILE)));
		}
	}
}
