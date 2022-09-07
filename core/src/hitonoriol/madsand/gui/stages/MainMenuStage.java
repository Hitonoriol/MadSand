package hitonoriol.madsand.gui.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.MadSand.Screens;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.dialogs.CreateWorldDialog;
import hitonoriol.madsand.gui.dialogs.LoadWorldDialog;
import hitonoriol.madsand.gui.dialogs.SettingsDialog;
import hitonoriol.madsand.properties.Globals;

public class MainMenuStage extends Stage {
	private static final float BUTTON_WIDTH = 260, BUTTON_HEIGHT = 60;
	private static float TITLE_PADBOTTOM = 50;

	private Table menuTable = new Table();
	private Table resumeTable = new Table();

	private Label titleLabel = new Label("MadSand", Gui.skin);
	private TextButton newGameButton = new TextButton("New game", Gui.skin);
	private TextButton resumeButton = new TextButton("Resume game", Gui.skin);
	private TextButton saveGameButton = new TextButton("Save game", Gui.skin);
	private TextButton settingsButton = new TextButton("Settings", Gui.skin);
	private TextButton loadGameButton = new TextButton("Load game", Gui.skin);
	private TextButton exitButton = new TextButton("Exit", Gui.skin);
	private Label versionLabel = new Label(getVersion(), Gui.skin);

	public MainMenuStage() {
		super(Gui.viewport());
		resumeTable.defaults().size(BUTTON_WIDTH / 2, BUTTON_HEIGHT);
		resumeTable.add(resumeButton);
		resumeTable.add(saveGameButton);
		resumeTable.setVisible(false);

		Gui.setFontSize(titleLabel, Gui.FONT_XL);
		titleLabel.setAlignment(Align.center);
		versionLabel.setAlignment(Align.center);

		menuTable.setFillParent(true);
		menuTable.setBackground(GuiSkin.darkBackground);
		menuTable.defaults().size(BUTTON_WIDTH, BUTTON_HEIGHT);
		menuTable.add(titleLabel).padBottom(TITLE_PADBOTTOM).row();
		menuTable.add(resumeTable).row();
		menuTable.add(newGameButton).row();
		menuTable.add(loadGameButton).row();
		menuTable.add(settingsButton).row();
		menuTable.add(exitButton).row();
		menuTable.add(versionLabel).padTop(20);
		super.addActor(menuTable);
		initButtonListeners();
	}

	public void showResumeTable() {
		resumeTable.setVisible(true);
	}

	public void hideResumeTable() {
		resumeTable.setVisible(false);
	}

	private void initButtonListeners() {
		Gui.setAction(newGameButton, () -> new CreateWorldDialog().show());
		Gui.setAction(resumeButton, () -> MadSand.switchScreen(Screens.Game));

		Gui.setAction(saveGameButton, () -> {
			MadSand.world().save();
			MadSand.switchScreen(Screens.Game);
		});

		Gui.setAction(loadGameButton, () -> new LoadWorldDialog().show());
		Gui.setAction(settingsButton, () -> new SettingsDialog().show());
		Gui.setAction(exitButton, () -> Gdx.app.exit());

		super.addListener(new InputListener() {
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if (!MadSand.isWorldUntouched() && keycode == Keys.ESCAPE)
					MadSand.switchScreen(Screens.Game);

				return true;
			}
		});
	}

	static String VER_COLOR = "[LIME]";

	private String getVersion() {
		return VER_COLOR + Globals.VERSION;
	}
}
