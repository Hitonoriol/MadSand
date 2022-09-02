package hitonoriol.madsand.gui.dialogs;

import java.util.Arrays;

import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand.Screens;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.widgets.AutoCheckBox;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.gui.widgets.AutoFocusSelectBox;
import hitonoriol.madsand.properties.Prefs;
import hitonoriol.madsand.properties.Prefs.DisplayModeDescriptor;
import hitonoriol.madsand.util.TimeUtils;

public class SettingsDialog extends GameDialog {

	Prefs prefs = Prefs.values();

	int curDisplayMode = prefs.getCurDisplayModeIdx();

	SelectBox<DisplayModeDescriptor> resolutionSelector = new AutoFocusSelectBox<>();
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
		prepareResolutionList();
		addSetting("Resolution", resolutionSelector).width(Gui.BTN_WIDTH);
		addSetting("Fullscreen", new AutoCheckBox(prefs.fullscreen, checked -> prefs.fullscreen = checked));

		addTitle("Gameplay");
		addSetting("Skip tutorials", new AutoCheckBox(prefs.skipTutorials, checked -> prefs.skipTutorials = checked));
		addSetting("Enable in-game action suggestion button",
				new AutoCheckBox(prefs.enableActionBtn, checked -> prefs.enableActionBtn = checked));
		addSetting("Realtime mechanics", new AutoCheckBox(prefs.enableRealtimeMechanics,
				checked -> prefs.enableRealtimeMechanics = checked));

		super.add(applyBtn).size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT).row();
		super.addCloseButton();

		Gui.setAction(applyBtn, () -> applySettings());
	}

	private void prepareResolutionList() {
		resolutionSelector.setAlignment(Align.center);
		resolutionSelector.setMaxListCount(10);
		resolutionSelector.getList().setAlignment(Align.center);
		resolutionSelector.setItems(Arrays.stream(prefs.getDisplayModes())
				.filter(mode -> mode.width >= Prefs.MIN_SCREEN_WIDTH)
				.map(mode -> new DisplayModeDescriptor(mode))
				.toArray(DisplayModeDescriptor[]::new));
		resolutionSelector.setSelected(new DisplayModeDescriptor(prefs.getCurDisplayMode()));
	}

	private void addTitle(String text) {
		buttonTbl.add(Gui.setFontSize(new Label(text, Gui.skin), Gui.FONT_M))
				.padTop(Gui.FONT_S)
				.row();
	}

	@SuppressWarnings("unused")
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
		DisplayMode curMode = resolutionSelector.getSelected().mode;
		prefs.screenWidth = curMode.width;
		prefs.screenHeight = curMode.height;
		prefs.apply();
		Prefs.savePrefs();
		TimeUtils.scheduleTask(() -> centerOnStage(true), 0.2f);
	}
}
