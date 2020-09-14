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

public class MainMenu extends Stage {

	Skin skin;

	private static float TITLE_XPOS = 0, TITLE_YPOS = 0;

	Table menuTable;

	Label titleLabel;
	TextButton newGameButton;
	public TextButton resumeButton;
	TextButton settingsButton;
	TextButton loadGameButton;
	TextButton exitButton;
	Label versionLabel;

	public MainMenu() {
		super();
		skin = Gui.skin;
		float width = Gui.DEFWIDTH;

		newGameButton = new TextButton("New game", skin);
		resumeButton = new TextButton("Resume game", skin);
		resumeButton.setVisible(false);
		settingsButton = new TextButton("Settings", skin);
		exitButton = new TextButton("Exit", skin);
		loadGameButton = new TextButton("Load game", skin);

		titleLabel = new Label("MadSand: Fallen. Rise From Dust", skin);

		if (TITLE_XPOS == 0) {
			TITLE_XPOS = Gdx.graphics.getWidth() / 2 - titleLabel.getWidth() / 2.0F;
			TITLE_YPOS = Gdx.graphics.getHeight() / 2 + 125;
		}

		titleLabel.setPosition(TITLE_XPOS, TITLE_YPOS);
		titleLabel.setFontScale(1.15f);

		versionLabel = new Label(getVersion(), skin);
		versionLabel.setAlignment(Align.center);

		menuTable = new Table();
		menuTable.setFillParent(true);
		menuTable.setBackground(Gui.darkBackground);
		menuTable.add(titleLabel);
		menuTable.row();
		menuTable.add(resumeButton).width(width);
		menuTable.row();
		menuTable.add(newGameButton).width(width);
		menuTable.row();
		menuTable.add(loadGameButton).width(width);
		menuTable.row();
		menuTable.add(settingsButton).width(width);
		menuTable.row();
		menuTable.add(exitButton).width(width);
		menuTable.row();
		menuTable.add(versionLabel).width(width);

		super.addActor(menuTable);

		initButtonListeners();
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
				MadSand.state = GameState.GAME;
				Gdx.input.setInputProcessor(Gui.overlay);
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

	private String getVersion() {
		String verFile = "MadSandData/version.dat";
		String ver;

		if (!GameSaver.getExternal(verFile).equals(""))
			ver = "\n[GREEN]" + (GameSaver.getExternal(verFile));
		else
			ver = "\n[GREEN]Version file not found";

		return ver;

	}
}
