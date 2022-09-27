package hitonoriol.madsand.gamecontent;

import java.util.HashMap;
import java.util.Map;

import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.resources.GameAssetManager;
import hitonoriol.madsand.resources.loaders.JsonLoader;

public class Tutorial implements Loadable {
	public static final String GAME_START = "GameStart";

	private Map<String, String> strings = new HashMap<String, String>();

	private static Tutorial instance = new Tutorial();

	public static void show(String name) {
		if (!Prefs.values().skipTutorials)
			GameDialog.generateDialogChain(instance.strings.get(name).replace(System.lineSeparator(), ""), Gui.overlay)
					.show();
	}

	@Override
	public void registerLoader(GameAssetManager manager) {
		manager.setLoader(Tutorial.class, new JsonLoader<>(manager, Tutorial.class) {
			@Override
			protected void load(Tutorial object) {
				instance = object;
			}
		});
	}
	
	public static Tutorial get() {
		return instance;
	}
}
