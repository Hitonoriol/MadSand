package hitonoriol.madsand.launcher.gui.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import hitonoriol.madsand.commons.exception.Exceptions;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

public class ConsoleLayoutController implements Initializable {
	private static final String separator = System.lineSeparator();

	@FXML
	private TextArea consoleOutputArea;
	private InputStream stdOut;

	public ConsoleLayoutController(InputStream stdOut) {
		this.stdOut = stdOut;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		CompletableFuture.runAsync(Exceptions.asUnchecked(() -> {
			BufferedReader in = new BufferedReader(new InputStreamReader(stdOut));
			String line;
			while ((line = in.readLine()) != null)
				print(line);
			print("[Game process exited]");
		}))
				.exceptionally(e -> {
					print("An exception has occurred: " + e.getMessage());
					return Exceptions.printStackTrace(e);
				});
	}

	private void print(String text) {
		Platform.runLater(() -> consoleOutputArea.appendText(text + separator));
	}

}
