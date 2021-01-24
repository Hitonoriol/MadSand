package hitonoriol.madsand.desktop;

import java.io.File;
import java.io.PrintStream;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.Prefs;

public class Launcher {

	static LwjglApplicationConfiguration config;

	public static void main(String[] args) throws Exception {
		applyArgs(args);
		Prefs.loadPrefs();
		Prefs prefs = Prefs.values();
		config = new LwjglApplicationConfiguration();
		config.title = "MadSand " + Globals.VERSION;
		config.resizable = false;

		config.addIcon("icons/icon-32.png", FileType.Internal);
		config.addIcon("icons/icon-64.png", FileType.Internal);
		config.addIcon("icons/icon-128.png", FileType.Internal);
		config.addIcon("icons/icon-256.png", FileType.Internal);

		config.vSyncEnabled = true;
		config.width = prefs.screenWidth;
		config.height = prefs.screenHeight;
		config.fullscreen = prefs.fullscreen;
		config.foregroundFPS = 59;
		config.backgroundFPS = -1;

		new com.badlogic.gdx.backends.lwjgl.LwjglApplication(new MadSand(), config);
	}

	final static String debugFlag = "debug";

	private static void applyArgs(String[] args) throws Exception {
		ArgParser parser = new ArgParser(args);

		if (!(Utils.debugMode = parser.argExists(debugFlag))) {
			System.setOut(new PrintStream(new File(Resources.OUT_FILE)));
			System.setErr(new PrintStream(new File(Resources.ERR_FILE)));
		}
	}
}