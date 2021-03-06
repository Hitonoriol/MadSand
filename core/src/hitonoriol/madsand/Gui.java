package hitonoriol.madsand;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.dialogs.OkDialog;
import hitonoriol.madsand.gui.stages.Overlay;
import hitonoriol.madsand.screens.TravelScreen;
import hitonoriol.madsand.util.TimeUtils;

public class Gui {
	public static final float DEFWIDTH = 250f;
	public static final float defLblWidth = Gdx.graphics.getWidth() / 4;
	public static int BTN_WIDTH = 150, BTN_HEIGHT = 30;
	public final static int FONT_XXS = 12, FONT_XS = 14, FONT_S = 16, FONT_M = 20, FONT_L = 24, FONT_XL = 28;
	public final static float DELAY = 0.05f;

	public static boolean gameUnfocused = false;
	public static boolean dialogActive = false;

	public static NinePatchDrawable transparency;

	public static NinePatchDrawable darkBackground;
	public static NinePatchDrawable darkBackgroundSizeable;
	public static NinePatchDrawable dialogBackground;

	public static Skin skin;

	public static ScreenViewport uiViewport = new ScreenViewport();
	public static Overlay overlay;
	private static GlyphLayout glyphLayout = new GlyphLayout();

	static Color mouseOverColor = new Color(0xa5a5a5ff);
	private static Map<Integer, LabelStyle> labelStyles = new HashMap<>();

	private static void initSkin() {
		skin = new Skin();
		skin.add("default", getFont(FONT_S));
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
		textButtonStyle.over = skin.newDrawable("background", mouseOverColor);
		textButtonStyle.font = skin.getFont("default");
		textButtonStyle.disabled = skin.newDrawable("background", new Color(0x3f3f3fdc));
		skin.add("default", textButtonStyle);

		Label.LabelStyle labelStyle = createLabelStyle(FONT_S);
		skin.add("default", labelStyle);

		Window.WindowStyle ws = new Window.WindowStyle();
		ws.background = skin.newDrawable("background", Color.LIGHT_GRAY);
		ws.stageBackground = darkBackground;
		ws.titleFontColor = Color.WHITE;
		ws.titleFont = getFont(FONT_M);
		skin.add("default", ws);

		TextField.TextFieldStyle tx = new TextField.TextFieldStyle();
		tx.font = getFont(FONT_S);
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

		CheckBox.CheckBoxStyle boxStyle = new CheckBoxStyle();
		boxStyle.checkboxOn = setMinSize(getColorDrawable(Color.DARK_GRAY), BTN_HEIGHT);
		boxStyle.checkboxOff = setMinSize(getColorDrawable(Color.GRAY), BTN_HEIGHT);
		boxStyle.font = textButtonStyle.font;
		boxStyle.checkboxOver = setMinSize(getColorDrawable(mouseOverColor), BTN_HEIGHT);
		skin.add("default", boxStyle);
	}

	static private void loadNinePatches() {
		dialogBackground = Resources.loadNinePatch("misc/bg.png");
		setMinSize(dialogBackground, 100, 50);

		transparency = Resources.loadNinePatch("misc/transparency.png");

		darkBackground = Resources.loadNinePatch("misc/darkness.png");
		darkBackgroundSizeable = new NinePatchDrawable(darkBackground);
		darkBackgroundSizeable.setMinHeight(0);
		darkBackgroundSizeable.setMinWidth(0);
	}

	static void init() {
		initSkin();
		overlay = new Overlay();
		createTransitionScreens();
	}

	public static Screen travelScreen;

	private static void createTransitionScreens() {
		travelScreen = new TravelScreen();
	}

	public static void openCraftMenu(int id) {
		MadSand.craftScreen.getStage().refreshCraftMenu(id);
		MadSand.switchScreen(MadSand.craftScreen);
	}

	private static void drawOkDialog(String title, String msg, Stage stage) {
		TimeUtils.scheduleTask(() -> new OkDialog(title, msg, stage).show(), DELAY);
	}

	public static void drawOkDialog(String title, String msg) {
		drawOkDialog(title, msg, null);
	}

	public static void drawOkDialog(String msg) {
		drawOkDialog(OkDialog.DEFAULT_TITLE, msg);
	}

	public static void gameUnfocus() {
		dialogActive = gameUnfocused = true;
		overlay.hideTooltip();
	}

	public static void gameResumeFocus(GameDialog dialog) {
		boolean noDialogsLeft = false;
		Stage stage = MadSand.getStage();

		if (stage != overlay)
			return;

		if (dialog == null)
			noDialogsLeft = !hasDialogs(stage);
		else
			noDialogsLeft = dialog.isOnlyDialog();

		if (noDialogsLeft) {
			forceResumeFocus();
			overlay.showTooltip();
		}
	}

	public static void forceResumeFocus() {
		dialogActive = gameUnfocused = false;
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

	public static Drawable getColorDrawable(Color color) {
		return skin.newDrawable("background", color);
	}

	public static Drawable setMinSize(Drawable drawable, int width, int height) {
		drawable.setMinWidth(width);
		drawable.setMinHeight(height);
		return drawable;
	}

	public static Drawable setMinSize(Drawable drawable, int size) {
		return setMinSize(drawable, size, size);
	}

	public static ClickListener setClickAction(Actor actor, int button, Runnable action) {
		ClickListener listener;
		actor.addListener(listener = new ClickListener(button) {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				action.run();
			}
		});
		return listener;
	}

	public static ClickListener setClickAction(Actor actor, Runnable action) {
		return setClickAction(actor, Buttons.LEFT, action);
	}

	public static void setAction(Actor actor, Runnable action) {
		actor.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				action.run();
			}
		});
	}

	public static BitmapFont getFont(int size) {
		return getLabelStyle(size).font;
	}

	public static LabelStyle getLabelStyle(int size) {
		if (!labelStyles.containsKey(size))
			labelStyles.put(size, createLabelStyle(size));

		return labelStyles.get(size);
	}

	private static LabelStyle createLabelStyle(int size) {
		LabelStyle style = new LabelStyle();
		style.font = Resources.createFont(size);
		style.fontColor = Color.WHITE;
		return style;
	}

	public static Label setFontSize(Label label, int size) {
		label.setStyle(Gui.getLabelStyle(size));
		return label;
	}

	public static int getFontSize(BitmapFont font) {
		return labelStyles.keySet().stream()
				.filter(size -> getFont(size) == font)
				.findFirst().orElse(-1);
	}

	private static GlyphLayout modifyGlyph(String text, int fontSize) {
		glyphLayout.setText(getFont(fontSize), text);
		return glyphLayout;
	}

	private static GlyphLayout modifyGlyph(String text) {
		return modifyGlyph(text, FONT_S);
	}

	public static float getTextWidth(String text, int fontSize) {
		return modifyGlyph(text, fontSize).width;
	}

	public static float getTextWidth(String text) {
		return getTextWidth(text, FONT_S);
	}

	public static float getTextHeight(String text) {
		return modifyGlyph(text).height;
	}

	public static float getTextHeight(String text, int fontSize) {
		return modifyGlyph(text, fontSize).height;
	}

	public static void skipLine(Table table) {
		table.add(new Label("", skin)).row();
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
