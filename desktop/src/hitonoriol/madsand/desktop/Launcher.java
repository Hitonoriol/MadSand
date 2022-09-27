package hitonoriol.madsand.desktop;

import java.io.PrintStream;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.GLEmulation;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gamecontent.Globals;
import hitonoriol.madsand.gamecontent.Prefs;
import hitonoriol.madsand.util.Functional;
import hitonoriol.madsand.util.If;
import hitonoriol.madsand.util.Log;

public class Launcher {
	private static Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

	public static void main(String[] args) throws Exception {
		main(args, new MadSand());
	}

	public static void main(String[] args, ApplicationListener app) throws Exception {
		applyArgs(args);
		Prefs.loadPrefs();
		Prefs prefs = Prefs.values();
		config.setTitle("MadSand " + Globals.VERSION);
		config.setResizable(false);
		config.setOpenGLEmulation(GLEmulation.GL20, 3, 2);

		config.setWindowIcon(FileType.Internal,
				"icons/icon-32.png",
				"icons/icon-64.png",
				"icons/icon-128.png",
				"icons/icon-256.png");
		config.useVsync(true);
		if (prefs.fullscreen)
			config.setFullscreenMode(prefs.getCurDisplayMode());
		else
			config.setWindowedMode(prefs.screenWidth, prefs.screenHeight);
		config.setForegroundFPS(59);
		config.setIdleFPS(10);

		new Lwjgl3Application(app, config);
	}

	private static void applyArgs(String[] args) throws Exception {
		ArgParser parser = new ArgParser(args);

		Functional.with(parser.argExists("debug"), dbgArg -> If.then(dbgArg, () -> Globals.debugMode = true));
		Globals.silentMode = parser.argExists("silent");

		if (!Globals.debugMode) {
			PrintStream out = new PrintStream(Log.OUT_FILE);
			System.setOut(out);
			System.setErr(out);
		}
	}

	public static void startHidden() {
		config.setWindowPosition(Integer.MAX_VALUE, Integer.MAX_VALUE);
		Globals.HEADLESS = true;
	}
}
