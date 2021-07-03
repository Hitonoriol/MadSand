package hitonoriol.madsand.gui.dialogs;

import java.io.File;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;

public class LoadWorldDialog extends GameDialog {

	float PAD_TITLE = 15;
	float PAD_BTN = 5;

	float BTN_WIDTH = Gui.defLblWidth;
	float BTN_HEIGHT = 40;

	Skin skin = Gui.skin;

	AutoFocusScrollPane scroll;
	Table scrollTable;

	public LoadWorldDialog() {
		super(MadSand.mainMenu);
		createDialog();
	}

	void createDialog() {
		float width = Gui.defLblWidth;

		scrollTable = new Table();
		scroll = new AutoFocusScrollPane(scrollTable);

		super.setTitle("\nLoad Game\n");
		super.getTitleLabel().setAlignment(Align.center);
		super.add().padBottom(PAD_TITLE).row();
		super.add(scroll).size(WIDTH, HEIGHT).row();

		refreshSaveList();

		TextButton cbtn = new TextButton("Cancel", skin);
		super.add(cbtn).size(width / 2, BTN_HEIGHT).pad(PAD_BTN).row();
		Gui.setAction(cbtn, () -> remove());
	}

	private void refreshSaveList() {
		String[] worldDirs = new File(MadSand.MAPDIR).list((current, name) -> new File(current, name).isDirectory());
		scrollTable.clear();

		TextButton loadButton, delButton;
		for (String worldName : worldDirs) {
			loadButton = new TextButton(worldName, skin);
			delButton = new TextButton("X", skin);

			scrollTable.add(loadButton).pad(PAD_BTN).size(BTN_WIDTH, BTN_HEIGHT);
			scrollTable.add(delButton).size(BTN_HEIGHT).padRight(PAD_BTN).row();

			Gui.setAction(loadButton, () -> {
				MadSand.WORLDNAME = worldName;
				if (GameSaver.load(worldName)) {
					MadSand.switchScreen(MadSand.gameScreen);
					MadSand.enterWorld();
				}

				Gui.overlay.refresh();
				remove();
			});

			Gui.setAction(delButton, () -> {
				new ConfirmDialog("Are you sure you want to delete " + worldName + "?",
						() -> {
							if (!GameSaver.deleteDirectory(new File(MadSand.MAPDIR + worldName)))
								Gui.drawOkDialog("Couldn't delete this save slot.");

							refreshSaveList();
						}, stage).show();
			});
		}

		if (worldDirs.length == 0)
			scrollTable.add(new TextButton("No worlds to load", skin)).width(BTN_WIDTH).row();
	}
}
