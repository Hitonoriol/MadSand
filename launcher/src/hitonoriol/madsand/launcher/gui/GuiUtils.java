package hitonoriol.madsand.launcher.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GuiUtils {
	public static Stage loadLayout(Stage stage, Layout layout) {
		try {
			Parent root = FXMLLoader.load(layout.getURL());
			stage.setScene(new Scene(root));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stage;
	}
	
	public static Stage loadLayout(Layout layout) {
		return loadLayout(new Stage(), layout);
	}
}
