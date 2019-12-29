package ru.bernarder.fallenrisefromdust.properties;

import java.util.HashMap;

import ru.bernarder.fallenrisefromdust.Gui;
import ru.bernarder.fallenrisefromdust.dialog.GameDialog;

public class Tutorial {
	public static final String GAME_START = "GameStart";

	public static HashMap<String, String> strings = new HashMap<String, String>();

	public static void show(String name) {
		GameDialog.generateDialogChain(strings.get(name), Gui.overlay).show();
	}
}
