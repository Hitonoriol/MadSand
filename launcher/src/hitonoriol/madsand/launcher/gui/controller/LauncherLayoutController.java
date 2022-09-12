package hitonoriol.madsand.launcher.gui.controller;

import static hitonoriol.madsand.launcher.resources.Strings.DOWNLOAD_TEXT;
import static hitonoriol.madsand.launcher.resources.Strings.LAUNCH_TEXT;
import static javafx.application.Platform.runLater;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import hitonoriol.madsand.launcher.GameLauncher;
import hitonoriol.madsand.launcher.GameVersionEntry;
import hitonoriol.madsand.launcher.Prefs;
import hitonoriol.madsand.launcher.ReleaseFetcher;
import hitonoriol.madsand.launcher.gui.GuiUtils;
import hitonoriol.madsand.launcher.gui.Layout;
import hitonoriol.madsand.launcher.util.Exceptions;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class LauncherLayoutController implements Initializable {
	@FXML
	private WebView changelogView;
	@FXML
	private ChoiceBox<GameVersionEntry> versionSelector;
	@FXML
	private Button deleteBtn;
	@FXML
	private Button launchBtn;
	@FXML
	private ProgressBar taskProgress;
	@FXML
	private Label statusLabel;

	private ObservableList<GameVersionEntry> versionList = FXCollections.observableList(Prefs.values().gameVersions);
	private ReleaseFetcher fetcher = new ReleaseFetcher(this);

	@Override
	@FXML
	public void initialize(URL location, ResourceBundle resources) {
		changelogView.setContextMenuEnabled(false);
		changelogView.setPageFill(Color.TRANSPARENT);
		versionSelector.setItems(versionList);
		(Prefs.values().autoCheckForUpdates.getValue() ? fetcher.refresh() : CompletableFuture.runAsync(() -> {}))
				.thenRun(() -> {
					Prefs.save();
					runLater(() -> {
						versionSelector.getSelectionModel().selectedItemProperty()
								.addListener((observable, oldVersion, newVersion) -> refreshUI());
						versionSelector.getSelectionModel().selectFirst();
					});
				})
				.exceptionally(Exceptions::printStackTrace);
	}

	private void refreshUI() {
		boolean installed = getSelectedVersion().file().exists();
		deleteBtn.setDisable(!installed);
		launchBtn.setText(installed ? LAUNCH_TEXT : DOWNLOAD_TEXT);
	}

	public void setStatusText(String format, Object... args) {
		String out = String.format(format, args);
		statusLabel.setText(out);
		System.out.println(out);
	}

	public void setActionProgressValue(double progress) {
		taskProgress.setProgress(progress);
	}

	public void setChangelogViewContents(String text) {
		changelogView.getEngine().loadContent(text);
	}

	private GameVersionEntry getSelectedVersion() {
		return versionSelector.getSelectionModel().getSelectedItem();
	}

	public ObservableList<GameVersionEntry> getVersionList() {
		return versionList;
	}

	@FXML
	private void launchSelectedVersionEntry(ActionEvent event) {
		launchBtn.setDisable(true);
		var game = getSelectedVersion();
		var gameFile = game.file();
		if (gameFile.exists()) {
			new GameLauncher(gameFile).launch();
			launchBtn.setDisable(false);
		} else
			fetcher.downloadGame(game).thenRun(() -> launchBtn.setDisable(false));
	}

	@FXML
	private void deleteSelectedVersionEntry(ActionEvent event) {
		getSelectedVersion().file().delete();
	}

	@FXML
	private void openPreferencesView(ActionEvent event) {
		Stage prefsWindow = GuiUtils.loadLayout(Layout.Preferences);
		prefsWindow.setTitle("Preferences");
		prefsWindow.showAndWait();
	}
}
