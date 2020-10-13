package hitonoriol.madsand.gui.dialogs;

import java.io.File;
import java.io.FilenameFilter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.enums.GameState;
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
		super(Gui.mainMenu);
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

		cbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				remove();
			}

		});
	}

	private void refreshSaveList() {
		int slots = 0;

		File file = new File(MadSand.MAPDIR);

		String[] dirs = file.list(new FilenameFilter() {
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		if (dirs != null)
			slots = dirs.length;

		if (slots > MadSand.MAXSAVESLOTS)
			slots = MadSand.MAXSAVESLOTS;

		scrollTable.clear();

		TextButton loadButton, delButton;
		int i = 0;
		while (i < slots) {
			loadButton = new TextButton(dirs[i], skin);
			delButton = new TextButton("X", skin);

			scrollTable.add(loadButton).pad(PAD_BTN).size(BTN_WIDTH, BTN_HEIGHT);
			scrollTable.add(delButton).size(BTN_HEIGHT).padRight(PAD_BTN).row();
			String sa = dirs[i];

			loadButton.addListener(new ChangeListener() {
				public void changed(ChangeListener.ChangeEvent event, Actor actor) {
					MadSand.WORLDNAME = sa;
					remove();
					if (GameSaver.loadWorld(sa)) {
						MadSand.state = GameState.GAME;
						Gdx.input.setInputProcessor(Gui.overlay);
						Gdx.graphics.setContinuousRendering(false);
						MadSand.worldEntered();
					}

					Gui.overlay.refreshOverlay();
				}
			});

			delButton.addListener(new ChangeListener() {

				@Override
				public void changed(ChangeEvent event, Actor actor) {
					new ConfirmDialog(new ConfirmDialog.Action() {

						@Override
						public void run() {
							if (!GameSaver.deleteDirectory(new File(MadSand.MAPDIR + sa)))
								Gui.drawOkDialog("Couldn't delete this save slot.", Gui.mainMenu);

							refreshSaveList();

						}
					}, "Are you sure you want to delete " + sa + "?", Gui.mainMenu).show();
				}
			});
			i++;
		}

		if (slots == 0)
			scrollTable.add(new TextButton("No worlds to load", skin)).width(BTN_WIDTH).row();
	}

	public void show() {
		super.show(Gui.mainMenu);
	}

}
