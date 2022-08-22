package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand.Screens;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.widgets.AutoCheckBox;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.properties.Prefs;
import hitonoriol.madsand.util.TimeUtils;

public class SettingsDialog extends GameDialog {

	Prefs prefs = Prefs.values();

	DisplayMode displayModes[] = prefs.getDisplayModes();
	int curDisplayMode = prefs.getCurDisplayModeIdx();

	TextButton resolutionBtn = new TextButton("", Gui.skin);
	TextButton applyBtn = new TextButton("Apply", Gui.skin);

	Table buttonTbl = new Table(Gui.skin);

	static float HEIGHT = 350;

	public SettingsDialog() {
		super(Screens.MainMenu.stage());
		createDialog();
	}

	void createDialog() {
		super.setTitle("Settings");
		super.centerTitle();
		super.skipLine();

		buttonTbl.align(Align.left);
		buttonTbl.defaults().align(Align.left);
		super.add(new AutoFocusScrollPane(buttonTbl))
				.height(HEIGHT)
				.padLeft(Gui.BTN_HEIGHT).padRight(Gui.BTN_HEIGHT)
				.row();
		super.skipLine();

		addTitle("Video");
		addSetting("Resolution", resolutionBtn, () -> nextDisplayMode());
		addSetting("Fullscreen", new AutoCheckBox(prefs.fullscreen, checked -> prefs.fullscreen = checked));

		addTitle("Gameplay");
		addSetting("Skip tutorials", new AutoCheckBox(prefs.skipTutorials, checked -> prefs.skipTutorials = checked));
		addSetting("Enable in-game action suggestion button",
				new AutoCheckBox(prefs.enableActionBtn, checked -> prefs.enableActionBtn = checked));
		addSetting("Realtime mechanics", new AutoCheckBox(prefs.enableRealtimeMechanics,
				checked -> prefs.enableRealtimeMechanics = checked));

		refreshResolutionBtn();

		super.add(applyBtn).size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT).row();
		super.addCloseButton();

		Gui.setAction(applyBtn, () -> applySettings());
	}

	private void addTitle(String text) {
		buttonTbl.add(Gui.setFontSize(new Label(text, Gui.skin), Gui.FONT_M))
				.padTop(Gui.FONT_S)
				.row();
	}

	private void addSetting(String name, Button button, Runnable action) {
		addSetting(name, button).width(Gui.BTN_WIDTH);
		Gui.setClickAction(button, action);
	}

	static float LBL_WIDTH = Gui.BTN_WIDTH * 1.1f;

	private Cell<Actor> addSetting(String name, Actor actor) {
		Label settingLbl = new Label(name + ":", Gui.skin);
		settingLbl.setWrap(true);
		settingLbl.setAlignment(Align.right);
		buttonTbl.add(settingLbl)
				.size(LBL_WIDTH, Gui.BTN_HEIGHT)
				.align(Align.right);
		Cell<Actor> cell = buttonTbl.add(actor).size(Gui.BTN_HEIGHT).pad(PADDING);
		cell.row();
		return cell;
	}

	private void applySettings() {
		DisplayMode curMode = displayModes[curDisplayMode];
		prefs.screenWidth = curMode.width;
		prefs.screenHeight = curMode.height;
		prefs.apply();
		Prefs.savePrefs();
		TimeUtils.scheduleTask(() -> centerOnStage(true), 0.2f);
	}

	private void nextDisplayMode() {
		++curDisplayMode;
		if (curDisplayMode >= displayModes.length)
			curDisplayMode = 0;

		DisplayMode mode = displayModes[curDisplayMode];

		if (mode.width < Prefs.MIN_SCREEN_WIDTH)
			nextDisplayMode();

		refreshResolutionBtn();
	}

	private void refreshResolutionBtn() {
		DisplayMode curMode = displayModes[curDisplayMode];
		resolutionBtn.setText(curMode.width + "x" + curMode.height + "@" + curMode.refreshRate);
	}
}
