package hitonoriol.madsand.gui.dialogs;

import java.io.File;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.MadSand.Screens;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.util.Utils;

public class CreateWorldDialog extends GameDialog {

	float BTN_WIDTH = WIDTH / 2;
	float BTN_HEIGHT = 40;

	Skin skin = Gui.skin;

	Label nameLabel;

	public CreateWorldDialog() {
		super(Screens.MainMenu.screen());
		createDialog();
	}

	void createDialog() {
		nameLabel = new Label("World name:", skin);
		nameLabel.setAlignment(Align.center);
		TextField worldNameField = new TextField(Utils.randWord() + " World", skin);
		super.setTitle("New Game");
		super.getTitleLabel().setAlignment(Align.center);
		TextButton createWorldBtn = new TextButton("Proceed", skin);
		TextButton cancelBtn = new TextButton("Cancel", skin);

		worldNameField.setAlignment(Align.center);
		super.row();
		super.add(nameLabel).padTop(50).width(WIDTH).row();
		super.add(worldNameField).width(WIDTH).padBottom(65).row();
		super.add(createWorldBtn).size(BTN_WIDTH, BTN_HEIGHT).row();
		super.add(cancelBtn).size(BTN_WIDTH, BTN_HEIGHT).row();

		Gui.setAction(cancelBtn, () -> remove());
		Gui.setAction(createWorldBtn, () -> {
			Gdx.graphics.setContinuousRendering(false);
			if (!worldNameField.getText().trim().equals("")) {
				String worldName = worldNameField.getText();
				File index = new File(GameSaver.SAVEDIR  + worldName);
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

				MadSand.switchScreen(Screens.Game);
				if (!MadSand.isWorldUntouched()) {
					MadSand.initNewGame();
					MadSand.world().generate();
				}

				MadSand.world().setName(worldName);
				MadSand.enterWorld();
				MadSand.player().updCoords();
				remove();
				new CharacterCreationDialog().show();
			}
		});
	}
}
