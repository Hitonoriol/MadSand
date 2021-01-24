package hitonoriol.madsand.gui.dialogs;

import java.io.File;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.world.World;

public class CreateWorldDialog extends GameDialog {

	float BTN_WIDTH = WIDTH / 2;
	float BTN_HEIGHT = 40;

	Skin skin = Gui.skin;

	Label nameLabel;

	public CreateWorldDialog() {
		super(MadSand.mainMenu);
		createDialog();
	}

	void createDialog() {
		File file = new File(MadSand.MAPDIR);
		String[] dirs = file.list((File current, String name) -> new File(current, name).isDirectory());

		int slots = 0;

		if (dirs != null)
			slots = dirs.length;

		if (slots > MadSand.MAXSAVESLOTS)
			slots = MadSand.MAXSAVESLOTS;

		nameLabel = new Label("World name:", skin);
		nameLabel.setAlignment(Align.center);
		final TextField worldtxt = new TextField("World #" + (++slots), skin);
		super.setTitle("New Game");
		super.getTitleLabel().setAlignment(Align.center);
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

					MadSand.switchScreen(MadSand.gameScreen);

					if (!MadSand.isWorldUntouched) {
						MadSand.initNewGame();
						MadSand.world.generate();
					}

					MadSand.worldEntered();

					World.player.updCoords();
					Gui.inventoryActive = false;
					remove();
					Gui.overlay.createCharDialog();
					Gdx.graphics.setContinuousRendering(false);
				}

			}
		});

		worldtxt.setAlignment(Align.center);

		super.row();
		super.add(nameLabel).padTop(50).width(WIDTH).row();
		super.add(worldtxt).width(WIDTH).padBottom(65).row();
		super.add(okbtn).size(BTN_WIDTH, BTN_HEIGHT).row();
		super.add(nobtn).size(BTN_WIDTH, BTN_HEIGHT).row();
	}
}
