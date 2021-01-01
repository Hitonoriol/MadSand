package hitonoriol.madsand.gui.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.enums.GameState;
import hitonoriol.madsand.gui.dialogs.CreateWorldDialog;
import hitonoriol.madsand.gui.dialogs.LoadWorldDialog;
import hitonoriol.madsand.gui.dialogs.SettingsDialog;
import hitonoriol.madsand.properties.Globals;

public class MainMenu extends Stage {

	Skin skin;

	final float PAD = 4;
	final float BUTTON_WIDTH = 260;
	private static float TITLE_XPOS = 0, TITLE_YPOS = 0;
	static float TITLE_PADBOTTOM = 50;

	Table menuTable;
	Table resumeTable;

	Label titleLabel;
	TextButton newGameButton;
	public TextButton resumeButton;
	public TextButton saveGameButton;
	TextButton settingsButton;
	TextButton loadGameButton;
	TextButton exitButton;
	Label versionLabel;

	public MainMenu() {
		super();
		skin = Gui.skin;

		newGameButton = new TextButton("New game", skin);
		resumeButton = new TextButton("Resume game", skin);
		saveGameButton = new TextButton("Save game", skin);
		settingsButton = new TextButton("Settings", skin);
		exitButton = new TextButton("Exit", skin);
		loadGameButton = new TextButton("Load game", skin);

		resumeTable = new Table();
		resumeTable.add(resumeButton).width(BUTTON_WIDTH / 2);
		resumeTable.add(saveGameButton).width(BUTTON_WIDTH / 2);
		resumeTable.setVisible(false);

		titleLabel = new Label("MadSand", skin);

		if (TITLE_XPOS == 0) {
			TITLE_XPOS = Gdx.graphics.getWidth() / 2 - titleLabel.getWidth() / 2.0F;
			TITLE_YPOS = Gdx.graphics.getHeight() / 2 + 175;
		}

		titleLabel.setPosition(TITLE_XPOS, TITLE_YPOS);
		titleLabel.setFontScale(1.15f);

		versionLabel = new Label(getVersion(), skin);
		versionLabel.setAlignment(Align.center);

		menuTable = new Table();
		menuTable.setFillParent(true);
		menuTable.setBackground(Gui.darkBackground);
		menuTable.add(titleLabel).padBottom(TITLE_PADBOTTOM).row();

		menuTable.add(resumeTable).width(BUTTON_WIDTH).row();

		menuTable.add(newGameButton).width(BUTTON_WIDTH).row();
		menuTable.add(loadGameButton).width(BUTTON_WIDTH).row();
		//menuTable.add(settingsButton).width(BUTTON_WIDTH).row();
		menuTable.add(exitButton).width(BUTTON_WIDTH).row();
		menuTable.add(versionLabel).padTop(20).width(BUTTON_WIDTH);

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
		newGameButton.addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				new CreateWorldDialog().show();
			}

		});

		resumeButton.addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				Gdx.graphics.setContinuousRendering(false);
				MadSand.switchStage(GameState.GAME, Gui.overlay);
			}

		});

		saveGameButton.addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				GameSaver.saveWorld();
				Gdx.graphics.setContinuousRendering(false);
				MadSand.switchStage(GameState.GAME, Gui.overlay);
			}

		});

		loadGameButton.addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				new LoadWorldDialog().show();
			}

		});

		settingsButton.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				new SettingsDialog().show();
			}
		});

		exitButton.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				Utils.out("Bye!");
				System.exit(0);
			}
		});

	}

	static String VER_COLOR = "[LIME]";

	private String getVersion() {
		return VER_COLOR + Globals.VERSION;
	}
}
