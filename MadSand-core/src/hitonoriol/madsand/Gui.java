package hitonoriol.madsand;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.enums.GameState;
import hitonoriol.madsand.gui.dialogs.OkDialog;
import hitonoriol.madsand.gui.stages.CraftMenu;
import hitonoriol.madsand.gui.stages.DeathStage;
import hitonoriol.madsand.gui.stages.MainMenu;
import hitonoriol.madsand.gui.stages.Overlay;
import hitonoriol.madsand.screens.TravelScreen;
import hitonoriol.madsand.world.World;

public class Gui {
	public static final float DEFWIDTH = 250f;
	public static final float defLblWidth = Gdx.graphics.getWidth() / 4;
	public static int BTN_WIDTH = 150, BTN_HEIGHT = 30;

	public static boolean gameUnfocused = false;
	public static boolean inventoryActive = false;
	public static boolean dialogActive = false;

	public static NinePatchDrawable transparency;

	public static Table darkness;

	public static BitmapFont font;
	public static BitmapFont fontMedium;
	public static BitmapFont fontBig;

	public static NinePatchDrawable darkBackground;
	public static NinePatchDrawable darkBackgroundSizeable;
	public static NinePatchDrawable dialogBackground;

	public static Skin skin;

	public static ScreenViewport uiViewport = new ScreenViewport();
	public static Overlay overlay;
	public static MainMenu mainMenu;
	public static DeathStage deathStage;
	public static CraftMenu craftMenu;

	private static void initSkin() { // TODO: Remove this shit and move skin to json for fucks sake
		font = Resources.createFont(16);
		fontMedium = Resources.createFont(20);
		fontBig = Resources.createFont(24);

		skin = new Skin();
		skin.add("default", font);
		loadNinePatches();

		Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 12, Pixmap.Format.RGB888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		skin.add("background", new Texture(pixmap));

		Slider.SliderStyle slst = new Slider.SliderStyle();
		slst.background = skin.newDrawable("background", Color.DARK_GRAY);
		slst.background.setMinHeight(30);
		slst.background.setMinWidth(10);

		Drawable knob = skin.newDrawable("background", Color.GRAY);
		knob.setMinWidth(10);
		knob.setMinHeight(30);
		slst.knob = knob;
		skin.add("default-horizontal", slst);

		TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
		textButtonStyle.up = skin.newDrawable("background", Color.GRAY);
		textButtonStyle.down = skin.newDrawable("background", Color.DARK_GRAY);
		textButtonStyle.over = skin.newDrawable("background", new Color(0xa5a5a5ff));
		textButtonStyle.font = skin.getFont("default");
		textButtonStyle.disabled = skin.newDrawable("background", new Color(0x3f3f3fdc));
		skin.add("default", textButtonStyle);

		Label.LabelStyle labelStyle = new Label.LabelStyle();
		labelStyle.font = font;
		labelStyle.fontColor = Color.WHITE;
		skin.add("default", labelStyle);

		Window.WindowStyle ws = new Window.WindowStyle();

		ws.background = skin.newDrawable("background", Color.LIGHT_GRAY);

		ws.stageBackground = darkBackground;
		ws.titleFontColor = Color.WHITE;
		ws.titleFont = fontMedium;
		skin.add("default", ws);

		TextField.TextFieldStyle tx = new TextField.TextFieldStyle();
		tx.font = font;
		tx.fontColor = Color.WHITE;
		tx.background = skin.newDrawable("background", Color.DARK_GRAY);
		tx.background.setMinHeight(35.0F);
		tx.selection = skin.newDrawable("background", Color.LIGHT_GRAY);
		tx.cursor = skin.newDrawable("background", Color.GRAY);
		tx.cursor.setMinWidth(1.0F);
		tx.cursor.setMinHeight(tx.background.getMinHeight());
		skin.add("default", tx);

		ScrollPane.ScrollPaneStyle spx = new ScrollPane.ScrollPaneStyle();
		skin.add("default", spx);

		TextTooltip.TextTooltipStyle txtool = new TextTooltip.TextTooltipStyle();
		txtool.background = darkBackground;
		txtool.label = labelStyle;
		skin.add("default", txtool);

		Drawable barKnob = skin.newDrawable("background", Color.DARK_GRAY);
		barKnob.setMinWidth(5);
		ProgressBar.ProgressBarStyle barStyle = new ProgressBarStyle(skin.newDrawable("background", Color.GRAY),
				barKnob);
		barStyle.knobBefore = barStyle.knob;
		skin.add("default-horizontal", barStyle);

	}

	static private void loadNinePatches() {
		dialogBackground = Resources.loadNinePatch("misc/bg.png");
		dialogBackground.setMinHeight(50);
		dialogBackground.setMinWidth(100);

		transparency = Resources.loadNinePatch("misc/transparency.png");

		darkBackground = Resources.loadNinePatch("misc/darkness.png");
		darkBackgroundSizeable = new NinePatchDrawable(darkBackground);
		darkBackgroundSizeable.setMinHeight(0);
		darkBackgroundSizeable.setMinWidth(0);
	}

	static void init() {
		initSkin();

		overlay = new Overlay();

		darkness = new Table();
		darkness.setBackground(darkBackground);
		darkness.setFillParent(true);
		darkness.setVisible(false);
		overlay.addActor(darkness);

		mainMenu = new MainMenu();
		deathStage = new DeathStage();

		craftMenu = new CraftMenu();

		createTransitionScreens(); // These worked when WorldGen & GameSaver were in a separate thread; for now they're useless

		MadSand.state = GameState.NMENU;
		Gdx.input.setInputProcessor(mainMenu);
	}

	static Stage worldGenStage;
	static Stage loadWorldStage;
	public static Screen travelScreen;

	private static void createTransitionScreens() {
		createTransitionScreen(worldGenStage, "Generating your world...");
		createTransitionScreen(loadWorldStage, "Loading...");
		travelScreen = new TravelScreen(MadSand.game);
	}

	private static void createTransitionScreen(Stage stage, String text) {
		stage = new Stage();
		Label label = new Label(text, skin);
		label.setFontScale(1.5f);
		label.setAlignment(Align.center);
		Table tbl = new Table();
		tbl.setFillParent(true);
		tbl.add(label).width(Gdx.graphics.getWidth()).row();
		stage.addActor(tbl);
	}

	public static void openCraftMenu(int id) {
		craftMenu.refreshCraftMenu(id);
		MadSand.switchStage(GameState.CRAFT, Gui.craftMenu);
	}

	public static void drawOkDialog(String title, String msg, Stage stage) {
		new OkDialog(title, msg, stage).show();
	}

	public static void drawOkDialog(String msg, Stage stage) {
		new OkDialog(msg, stage).show();
	}

	public static void gameUnfocus() {
		dialogActive = gameUnfocused = true;
		overlay.hideTooltip();
	}

	public static void gameResumeFocus(GameDialog dialog) {
		boolean noDialogsLeft = false;

		if (dialog == null)
			noDialogsLeft = !hasDialogs(overlay);
		else
			noDialogsLeft = dialog.isOnlyDialog();

		if (noDialogsLeft) {
			dialogActive = gameUnfocused = false;
			overlay.showTooltip();
		}
	}

	public static boolean hasDialogs(Stage stage, GameDialog dialog) { // If stage has dialogs except <dialog>
		for (Actor actor : stage.getActors())
			if (actor != dialog && actor instanceof GameDialog)
				return true;
		return false;
	}

	public static boolean hasDialogs(Stage stage) { // If stage has any GameDialog
		return hasDialogs(stage, null);
	}

	public static void gameResumeFocus() {
		gameResumeFocus(null);
	}

	public static boolean isGameUnfocused() {
		return gameUnfocused || dialogActive;
	}

	public static void refreshOverlay() {
		overlay.refresh();
	}

	public static float horizontalCenter(Actor actor) {
		return (Gdx.graphics.getWidth() / 2) - actor.getWidth();
	}

	public static void toggleInventory() {
		if (inventoryActive)
			World.player.hideInventory();
		else
			World.player.showInventory();
	}

	public static Drawable getColorDrawable(Color color) {
		return skin.newDrawable("background", color);
	}

	public static ProgressBarStyle createProgressBarStyle(float width, float height, Color color, boolean transparent) {
		Drawable knob = Gui.getColorDrawable(color);
		knob.setMinWidth(5);
		ProgressBarStyle style = new ProgressBarStyle(transparent ? transparency : getColorDrawable(Color.GRAY), knob);

		style.background.setMinWidth(width);
		style.background.setMinHeight(height);

		style.knob.setMinHeight(height);
		style.knobBefore = style.knob;
		style.knobBefore.setMinHeight(height);
		return style;
	}

	public static ProgressBarStyle createProgressBarStyle(float width, float height, Color color) {
		return createProgressBarStyle(width, height, color, false);
	}

}
