package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.properties.Prefs;

public class SettingsDialog extends GameDialog {

	Prefs prefs = Prefs.values();

	DisplayMode displayModes[] = prefs.getDisplayModes();
	int curDisplayMode = prefs.getCurDisplayModeIdx();

	CheckBox fullscreenBox = new CheckBox("", Gui.skin);
	TextButton resolutionBtn = new TextButton("", Gui.skin);

	TextButton applyBtn = new TextButton("Apply", Gui.skin);

	public SettingsDialog() {
		super(MadSand.mainMenu);
		createDialog();
	}

	void createDialog() {
		super.setTitle("Settings");
		super.centerTitle();
		super.skipLine();
		super.skipLine();

		Table buttonTbl = new Table(Gui.skin);
		buttonTbl.align(Align.left);
		buttonTbl.defaults().align(Align.left);
		buttonTbl.add("Resolution: ").align(Align.right);
		buttonTbl.add(resolutionBtn).size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT).pad(PADDING).row();
		buttonTbl.add("Fullscreen: ").align(Align.right);
		buttonTbl.add(fullscreenBox).size(Gui.BTN_HEIGHT).pad(PADDING).row();
		super.add(buttonTbl).row();
		super.skipLine();

		refreshResolutionBtn();
		fullscreenBox.setChecked(prefs.fullscreen);

		super.add(applyBtn).size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT).row();
		super.addCloseButton();

		resolutionBtn.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				nextDisplayMode();
			}
		});

		applyBtn.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				applySettings();
			}
		});
	}

	private void applySettings() {
		DisplayMode curMode = displayModes[curDisplayMode];
		prefs.screenWidth = curMode.width;
		prefs.screenHeight = curMode.height;
		prefs.fullscreen = fullscreenBox.isChecked();
		prefs.apply();
		Prefs.savePrefs();
	}

	private void nextDisplayMode() {
		++curDisplayMode;
		if (curDisplayMode >= displayModes.length)
			curDisplayMode = 0;

		DisplayMode mode = displayModes[curDisplayMode];

		if (mode.refreshRate != Prefs.REFRESH_RATE || mode.width < Prefs.MIN_SCREEN_WIDTH)
			nextDisplayMode();

		refreshResolutionBtn();
	}

	private void refreshResolutionBtn() {
		DisplayMode curMode = displayModes[curDisplayMode];
		resolutionBtn.setText(curMode.width + "x" + curMode.height);
	}
}
