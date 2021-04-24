package hitonoriol.madsand.properties;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Optional;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.entities.ability.ActiveAbility;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.World;

public class Prefs {
	public static String PREFS_FILE = "prefs.json";
	private static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	private static Prefs values = new Prefs();

	/* Video */
	public static int MIN_SCREEN_WIDTH = 1100;
	public int screenWidth = 1280, screenHeight = 720;
	public boolean fullscreen = false;
	DisplayMode displayModes[];

	/* Keybinds */
	public LinkedHashMap<Integer, Integer> abilityKeyBinds;

	/* Gameplay */
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
		if (!fullscreen)
			return displayModes.length - 1;

		int modeIdx = 0;
		for (DisplayMode mode : displayModes) {
			if (screenWidth == mode.width && screenHeight == mode.height)
				break;
			++modeIdx;
		}
		return modeIdx;
	}

	@JsonIgnore
	public DisplayMode getCurDisplayMode() {
		return getDisplayModes()[getCurDisplayModeIdx()];
	}

	public int getAbilityKey(int id) {
		return values.abilityKeyBinds.entrySet().stream()
				.filter(bind -> bind.getValue() == id)
				.findFirst()
				.map(bind -> bind.getKey())
				.orElse(-1);
	}

	public void bindAbility(int key, int abilityId) {
		Optional.of(getAbilityKey(abilityId))
				.filter(boundKey -> boundKey != -1)
				.ifPresent(boundKey -> abilityKeyBinds.remove(boundKey));

		abilityKeyBinds.put(key, abilityId);
		Gui.overlay.hotbar.addEntry(World.player.getAbility(abilityId).as(ActiveAbility.class).get());
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
		if (values.abilityKeyBinds == null)
			values.abilityKeyBinds = new LinkedHashMap<>();

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
