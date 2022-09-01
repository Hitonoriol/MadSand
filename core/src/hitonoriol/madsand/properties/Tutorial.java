package hitonoriol.madsand.properties;

import java.util.HashMap;

import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.Gui;

public class Tutorial {
	public static final String GAME_START = "GameStart";

	public static HashMap<String, String> strings = new HashMap<String, String>();

	public static void show(String name) {
		if (!Prefs.values().skipTutorials)
			GameDialog.generateDialogChain(strings.get(name).replace(System.lineSeparator(), ""), Gui.overlay).show();
	}

}
