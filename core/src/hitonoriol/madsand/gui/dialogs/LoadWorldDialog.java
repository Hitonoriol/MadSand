package hitonoriol.madsand.gui.dialogs;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.MadSand.Screens;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.textgenerator.StaticTextGenerator;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.gui.widgets.gametooltip.LabelProcessor;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.GameSaver;

public class LoadWorldDialog extends GameDialog {
	private static final float PAD_TITLE = 15, PAD_BTN = 5;
	private static final float BTN_WIDTH = Gui.DEFAULT_WIDTH, BTN_HEIGHT = 40;

	private Table scrollTable = new Table();
	private AutoFocusScrollPane scroll = new AutoFocusScrollPane(scrollTable);
	private TextButton closeBtn = createCloseButton();

	private LabelProcessor labelRefresher = new LabelProcessor();
	private long loadingStart;

	public LoadWorldDialog() {
		createDialog();
		refreshSaveList();
	}

	private void createDialog() {
		scrollTable.defaults().size(BTN_WIDTH, BTN_HEIGHT).pad(PAD_BTN);
		super.setTitle("\nLoad Game\n");
		super.getTitleLabel().setAlignment(Align.center);
		super.add().padBottom(PAD_TITLE).row();
		super.add(scroll).size(WIDTH, HEIGHT).row();
		super.add(closeBtn).size(BTN_WIDTH / 2, BTN_HEIGHT).row();
	}

	private void refreshSaveList() {
		String[] worldDirs = new File(GameSaver.MAPDIR).list((current, name) -> new File(current, name).isDirectory());
		scrollTable.clear();

		for (String worldName : worldDirs) {
			TextButton loadButton = Widgets.button(worldName);
			TextButton delButton = Widgets.button("X");
			scrollTable.add(loadButton).pad(PAD_BTN);
			scrollTable.add(delButton).size(BTN_HEIGHT).padRight(PAD_BTN).row();
			Gui.setAction(loadButton, () -> loadWorld(worldName));
			Gui.setAction(delButton, () -> deleteWorld(worldName));
		}

		if (worldDirs.length == 0)
			scrollTable.add(Widgets.button("No worlds to load")).row();
	}

	private void addEntry(String text) {
		addEntry(Widgets.label(text));
	}

	private void addEntry(Label label) {
		label.setAlignment(Align.center);
		TextButton loadingInfo = Widgets.button();
		loadingInfo.setLabel(label);
		scrollTable.add(loadingInfo).row();
	}

	private void addEntry(Supplier<String> infoSupplier) {
		var infoLabel = labelRefresher
				.addTextGenerator(new StaticTextGenerator(infoSupplier))
				.update(1);
		infoLabel.refresh();
		addEntry(infoLabel);
	}

	private void loadWorld(String path) {
		loadingStart = Utils.now();
		closeBtn.setDisabled(true);
		scrollTable.addAction(sequence(
				fadeOut(0.1f),
				run(() -> {
					scrollTable.clearChildren();
					addEntry("Loading...");
					addEntry(() -> String.format("%s", Utils.timeString(Utils.now() - loadingStart)));
				}),
				fadeIn(0.1f)));

		CompletableFuture.supplyAsync(new GameSaver(path)::load)
				.thenAccept(loaded -> {
					if (loaded) {
						remove();
						MadSand.switchScreen(Screens.Game);
						MadSand.enterWorld();
						Gui.overlay.refresh();
					}
				});
	}

	private void deleteWorld(String path) {
		new ConfirmDialog("Are you sure you want to delete " + path + "?",
				() -> {
					if (!GameSaver.deleteDirectory(new File(GameSaver.MAPDIR + path)))
						Gui.drawOkDialog("Couldn't delete this save slot.");
					refreshSaveList();
				}, stage).show();
	}
}
