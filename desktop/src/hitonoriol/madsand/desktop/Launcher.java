package hitonoriol.madsand.desktop;

import java.io.PrintStream;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.Prefs;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Functional;
import hitonoriol.madsand.util.If;

public class Launcher {

	static LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

	public static void main(String[] args) throws Exception {
		main(args, new MadSand());
	}

	public static void main(String[] args, ApplicationListener app) throws Exception {
		applyArgs(args);
		Prefs.loadPrefs();
		Prefs prefs = Prefs.values();
		config.title = "MadSand " + Globals.VERSION;
		config.resizable = false;
		config.allowSoftwareMode = true;

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

		new LwjglApplication(app, config);
	}

	private static void applyArgs(String[] args) throws Exception {
		ArgParser parser = new ArgParser(args);

		Functional.with(parser.argExists("debug"), dbgArg -> If.then(dbgArg, () -> Globals.debugMode = true));
		Globals.silentMode = parser.argExists("silent");

		if (!Globals.debugMode) {
			System.setOut(new PrintStream(Resources.OUT_FILE));
			System.setErr(new PrintStream(Resources.ERR_FILE));
		}
	}

	public static void startHidden() {
		config.x = config.y = Integer.MAX_VALUE;
		Globals.HEADLESS = true;
	}

}
