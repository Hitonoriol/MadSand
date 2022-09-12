package hitonoriol.madsand.launcher.gui;

import java.net.URL;

public enum Layout {
	Launcher("/LauncherLayout.fxml"),
	Preferences("/PreferencesLayout.fxml"),
	Console("/ConsoleLayout.fxml");
	
	private final URL resourceURL;
	Layout(String resourcePath) {
		resourceURL = getClass().getResource(resourcePath);
	}
	
	public URL getURL() {
		return resourceURL;
	}
}
