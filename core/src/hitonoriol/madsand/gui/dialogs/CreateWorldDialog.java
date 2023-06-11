package hitonoriol.madsand.gui.dialogs;

import java.io.File;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.MadSand.Screens;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.GameSaver;

public class CreateWorldDialog extends GameDialog {
	private final static float BTN_WIDTH = defaultWidth() / 2, BTN_HEIGHT = 40;

	public CreateWorldDialog() {
		super(Screens.MainMenu.screen());
		createDialog();
	}

	private void createDialog() {
		Label nameLabel = Widgets.label("World name:");
		TextField worldNameField = Widgets.textField(Utils.randWord() + " World");
		TextButton createWorldBtn = Widgets.button("Proceed");

		nameLabel.setAlignment(Align.center);
		super.setTitle("New Game");
		super.getTitleLabel().setAlignment(Align.center);
		worldNameField.setAlignment(Align.center);
		super.row();
		super.add(nameLabel).padTop(50).width(defaultWidth()).row();
		super.add(worldNameField).width(defaultWidth()).padBottom(65).row();
		super.add(createWorldBtn).size(BTN_WIDTH, BTN_HEIGHT).row();
		super.add(createCloseButton()).size(BTN_WIDTH, BTN_HEIGHT).row();

		Gui.setAction(createWorldBtn, () -> createWorld(worldNameField.getText().trim()));
	}

	private void createWorld(String worldName) {
		if (worldName.isEmpty())
			return;

		if (new File(GameSaver.SAVEDIR + worldName).exists())
			return;

		remove();
		MadSand.switchScreen(Screens.Game);
		if (!MadSand.isWorldUntouched()) {
			MadSand.initNewGame();
			MadSand.world().generate();
		}

		MadSand.world().setName(worldName);
		MadSand.enterWorld();
		MadSand.player().updCoords();
		new CharacterCreationDialog().show();
		Lua.executeScript(Lua.onNewGame);
	}
}
