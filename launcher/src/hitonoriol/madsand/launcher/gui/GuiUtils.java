package hitonoriol.madsand.launcher.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GuiUtils {
	public static Stage loadLayout(Stage stage, Layout layout, Object controller) {
		try {
			Parent root;
			if (controller == null)
				root = FXMLLoader.load(layout.getURL());
			else {
				FXMLLoader loader = new FXMLLoader(layout.getURL());
				loader.setController(controller);
				root = loader.load();
			}
			stage.setScene(new Scene(root));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stage;
	}
	
	public static Stage loadLayout(Stage stage, Layout layout) {
		return loadLayout(stage, layout, null);
	}

	public static Stage loadLayout(Layout layout) {
		return loadLayout(new Stage(), layout);
	}
	
	public static Stage loadLayout(Layout layout, Object controller) {
		return loadLayout(new Stage(), layout, controller);
	}
}
