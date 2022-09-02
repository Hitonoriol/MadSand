package hitonoriol.madsand.properties;

import java.io.File;
import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import hitonoriol.madsand.util.Utils;

public class Prefs {
	public static String PREFS_FILE = "prefs.json";
	private static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	private static Prefs values;

	/* Video */
	public static int MIN_SCREEN_WIDTH = 1100;
	public int screenWidth = 1280, screenHeight = 720;
	public boolean fullscreen = false;
	DisplayMode displayModes[];

	/* Global Keybinds ? 
	 * ----------
	 */

	/* Gameplay */
	public boolean enableRealtimeMechanics = true;
	public boolean skipTutorials = false;
	public boolean enableActionBtn = true;

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
		for (DisplayMode mode : getDisplayModes()) {
			if (screenWidth == mode.width && screenHeight == mode.height)
				break;
			++modeIdx;
		}

		if (modeIdx == getDisplayModes().length)
			return modeIdx - 1;

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

	public static boolean actionButtonEnabled() {
		return values.enableActionBtn;
	}

	public static void loadPrefs() {
		File prefs = new File(PREFS_FILE);
		if (prefs.exists())
			try {
				values = mapper.readValue(prefs, Prefs.class);
			} catch (Exception e) {
				Utils.dbg("Failed to restore preferences from " + PREFS_FILE);
				e.printStackTrace();
			}
		else
			values = new Prefs();
	}

	public static void savePrefs() {
		try {
			mapper.writeValue(new File(PREFS_FILE), values);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class DisplayModeDescriptor {
		public DisplayMode mode;

		public DisplayModeDescriptor(DisplayMode mode) {
			this.mode = mode;
		}

		@Override
		public String toString() {
			return mode.width + "x" + mode.height + "@" + mode.refreshRate;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			
			if (obj == null || !(obj instanceof DisplayModeDescriptor))
				return true;
			
			return mode.equals(((DisplayModeDescriptor) obj).mode);
		}
	}
}
