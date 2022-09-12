package hitonoriol.madsand.launcher;

import hitonoriol.madsand.launcher.gui.GuiUtils;
import hitonoriol.madsand.launcher.gui.Layout;
import hitonoriol.madsand.launcher.resources.Strings;
import javafx.application.Application;
import javafx.stage.Stage;

public class LauncherApp extends Application {
	private static Stage mainWindow;
	
	@Override
	public void start(Stage stage) throws Exception {
		mainWindow = stage;
		GameLauncher.createGameDirectory();
		Prefs.load();
		GuiUtils.loadLayout(stage, Layout.Launcher);
		stage.setTitle("MadSand Launcher " + Strings.VERSION);
		stage.show();
	}

	public static void main(String args[]) {
		launch(args);
	}
	
	public static Stage getMainWindow() {
		return mainWindow;
	}
}
