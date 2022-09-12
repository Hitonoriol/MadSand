package hitonoriol.madsand.launcher.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import hitonoriol.madsand.launcher.Prefs;
import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PreferencesLayoutController implements Initializable {

	@FXML
	private TextField vmArgsField;
	@FXML
	private TextField gameArgsField;
	@FXML
	private CheckBox showConsoleBox;
	@FXML
	private CheckBox autoUpdateBox;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Prefs prefs = Prefs.values();
		bind(vmArgsField, prefs.vmArgs);
		bind(gameArgsField, prefs.gameArgs);
		bind(showConsoleBox, prefs.showConsole);
		bind(autoUpdateBox, prefs.autoCheckForUpdates);
	}

	private void bind(TextField textField, Property<String> property) {
		textField.textProperty().bindBidirectional(property);
	}
	
	private void bind(CheckBox checkBox, Property<Boolean> property) {
		checkBox.selectedProperty().bindBidirectional(property);
	}
	
	@FXML
	private void applyAndClose(ActionEvent event) {
		Prefs.save();
		close(event);
	}

	@FXML
	private void close(ActionEvent event) {
		Node source = (Node) event.getSource();
		Stage stage = (Stage) source.getScene().getWindow();
		stage.close();
	}
}
