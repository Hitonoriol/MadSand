package hitonoriol.madsand.gui.dialogs;

import java.io.File;
import java.io.FilenameFilter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.enums.GameState;
import hitonoriol.madsand.world.World;

public class CreateWorldDialog extends Dialog {
	Skin skin = Gui.skin;

	public CreateWorldDialog() {
		super("", Gui.skin);
		createDialog();
	}

	void createDialog() {

		File file = new File(MadSand.MAPDIR);
		String[] dirs = file.list(new FilenameFilter() {
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		int slots = 0;

		if (dirs != null)
			slots = dirs.length;

		if (slots > MadSand.MAXSAVESLOTS)
			slots = MadSand.MAXSAVESLOTS;

		final TextField worldtxt = new TextField("World #" + (++slots), skin);
		super.text("New game");
		TextButton okbtn = new TextButton("Proceed", skin);
		TextButton nobtn = new TextButton("Cancel", skin);

		if (slots >= MadSand.MAXSAVESLOTS) {
			worldtxt.setText("No free slots left!");
			worldtxt.setDisabled(true);
			okbtn.setDisabled(true);
		}

		nobtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				remove();
			}

		});

		okbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				if (!worldtxt.getText().trim().equals("")) {
					MadSand.WORLDNAME = worldtxt.getText();
					File index = new File("MadSand_Saves/" + MadSand.WORLDNAME);
					String[] entries = index.list();

					if (entries != null) {
						int j = entries.length;
						for (int i = 0; i < j; i++) {
							String s = entries[i];
							File currentFile = new File(index.getPath(), s);
							currentFile.delete();
						}
						index.delete();
					}

					MadSand.switchStage(GameState.GAME, Gui.overlay);
					if (!MadSand.justStarted)
						MadSand.world.generate();
					// World.player.x = new Random().nextInt(World.MAPSIZE);
					// World.player.y = new Random().nextInt(World.MAPSIZE);
					World.player.updCoords();
					Gui.overlay.exitToMenuButton.setVisible(false);
					Gui.overlay.craftMenuButton.setVisible(false); // TODO: Move to InventoryUI
					Gui.inventoryActive = false;
					remove();
					MadSand.ZOOM = MadSand.DEFAULT_ZOOM;
					Gui.overlay.createCharDialog();
					Gdx.graphics.setContinuousRendering(false);
				}

			}
		});

		worldtxt.setTextFieldListener(new TextField.TextFieldListener() {

			public void keyTyped(TextField textField, char key) {
			}

		});

		super.row();
		super.add(new Label("\n\n", skin)).width(Gdx.graphics.getWidth() / 2).row();
		super.add(new Label("\n\nWorld name:\n", skin)).width(Gdx.graphics.getWidth() / 2).row();
		super.add(worldtxt).width(Gdx.graphics.getWidth() / 2).row();
		super.add(okbtn).width(Gdx.graphics.getWidth() / 2).row();
		super.add(nobtn).width(Gdx.graphics.getWidth() / 2).row();
		super.add(new Label("\n\n", skin)).width(Gdx.graphics.getWidth() / 2).row();
	}

	public void show() {
		super.show(Gui.mainMenu);
	}
}
