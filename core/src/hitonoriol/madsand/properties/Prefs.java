package hitonoriol.madsand.properties;

import java.io.File;
import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import hitonoriol.madsand.Utils;

public class Prefs {
	public static String PREFS_FILE = "prefs.json";
	private static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	private static Prefs values = new Prefs();
	public static int REFRESH_RATE = 60;
	public static int MIN_SCREEN_WIDTH = 1100;
	public int screenWidth = 1280, screenHeight = 720;
	public boolean fullscreen = false;
	DisplayMode displayModes[];

	public boolean enableRealtimeMechanics = true;
	public boolean skipTutorials = false;

	@JsonIgnore
	public DisplayMode[] getDisplayModes() {
		if (displayModes == null) {
			displayModes = Gdx.graphics.getDisplayModes();
			Arrays.sort(displayModes, (o1, o2) -> Integer.compare(o1.width, o2.width));
		}
		return displayModes;
	}

	@JsonIgnore
	public int getCurDisplayModeIdx() {
		int modeIdx = 0;
		for (DisplayMode mode : displayModes) {
			if (screenWidth == mode.width && screenHeight == mode.height && mode.refreshRate == REFRESH_RATE)
				break;
			++modeIdx;
		}
		return modeIdx;
	}

	@JsonIgnore
	public DisplayMode getCurDisplayMode() {
		return getDisplayModes()[getCurDisplayModeIdx()];
	}

	public void apply() {
		if (fullscreen)
			Gdx.graphics.setFullscreenMode(getCurDisplayMode());
		else
			Gdx.graphics.setWindowedMode(screenWidth, screenHeight);
	}

	public static Prefs values() {
		return values;
	}

	public static void loadPrefs() {
		File prefs = new File(PREFS_FILE);
		if (!prefs.exists())
			return;

		try {
			values = mapper.readValue(prefs, Prefs.class);
		} catch (Exception e) {
			Utils.out("Failed to restore preferences from " + PREFS_FILE);
			e.printStackTrace();
		}
	}

	public static void savePrefs() {
		try {
			mapper.writeValue(new File(PREFS_FILE), values);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
