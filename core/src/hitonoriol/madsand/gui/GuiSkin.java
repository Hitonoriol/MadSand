package hitonoriol.madsand.gui;

import static hitonoriol.madsand.gui.Gui.BTN_HEIGHT;
import static hitonoriol.madsand.gui.Gui.getFont;
import static hitonoriol.madsand.gui.Gui.setMinSize;
import static hitonoriol.madsand.resources.Resources.loadNinePatch;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

import hitonoriol.madsand.resources.Resources;

public class GuiSkin {
	private static final Skin skin = new Skin();

	public static final Drawable transparency = loadNinePatch("misc/transparency");
	public static final NinePatchDrawable darkBackground = loadNinePatch("misc/darkness");
	public static final NinePatchDrawable darkBackgroundSizeable = setMinSize(new NinePatchDrawable(darkBackground), 0);
	public static final Drawable dialogBackground = setMinSize(Resources.loadNinePatch("gui/bg"), 100, 50);

	private static Map<Integer, LabelStyle> labelStyles = new HashMap<>();

	static Skin init() {
		createInitialColorDrawable();
		skin.add("default", getFont(Gui.FONT_S));

		Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
		sliderStyle.background = setMinSize(GuiSkin.getColorDrawable(Color.DARK_GRAY), 10, 30);
		sliderStyle.knob = setMinSize(GuiSkin.getColorDrawable(Color.GRAY), 10, 30);
		skin.add("default-horizontal", sliderStyle);

		TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
		textButtonStyle.up = GuiSkin.getColorDrawable(Color.GRAY);
		textButtonStyle.down = GuiSkin.getColorDrawable(Color.DARK_GRAY);
		textButtonStyle.over = GuiSkin.getColorDrawable(GuiColors.MOUSEOVER);
		textButtonStyle.font = skin.getFont("default");
		textButtonStyle.disabled = GuiSkin.getColorDrawable(GuiColors.BUTTON_DISABLED);
		skin.add("default", textButtonStyle);

		Label.LabelStyle labelStyle = GuiSkin.createLabelStyle(Gui.FONT_S);
		skin.add("default", labelStyle);

		Window.WindowStyle windowStyle = new Window.WindowStyle();
		windowStyle.background = GuiSkin.getColorDrawable(Color.LIGHT_GRAY);
		windowStyle.stageBackground = darkBackground;
		windowStyle.titleFontColor = Color.WHITE;
		windowStyle.titleFont = getFont(Gui.FONT_M);
		skin.add("default", windowStyle);

		TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
		textFieldStyle.font = getFont(Gui.FONT_S);
		textFieldStyle.fontColor = Color.WHITE;
		textFieldStyle.background = GuiSkin.getColorDrawable(Color.DARK_GRAY);
		textFieldStyle.background.setMinHeight(35.0f);
		textFieldStyle.selection = GuiSkin.getColorDrawable(Color.LIGHT_GRAY);
		textFieldStyle.cursor = setMinSize(GuiSkin.getColorDrawable(Color.GRAY), 1f,
				textFieldStyle.background.getMinHeight());
		skin.add("default", textFieldStyle);

		ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
		skin.add("default", scrollStyle);

		TextTooltip.TextTooltipStyle tooltipStyle = new TextTooltip.TextTooltipStyle();
		tooltipStyle.background = darkBackground;
		tooltipStyle.label = labelStyle;
		skin.add("default", tooltipStyle);

		Drawable barKnob = GuiSkin.getColorDrawable(Color.DARK_GRAY);
		barKnob.setMinWidth(5);

		ProgressBar.ProgressBarStyle barStyle = new ProgressBarStyle(GuiSkin.getColorDrawable(Color.GRAY), barKnob);
		barStyle.knobBefore = barStyle.knob;
		skin.add("default-horizontal", barStyle);

		CheckBox.CheckBoxStyle boxStyle = new CheckBoxStyle();
		boxStyle.checkboxOn = setMinSize(GuiSkin.getColorDrawable(Color.DARK_GRAY), BTN_HEIGHT);
		boxStyle.checkboxOnOver = setMinSize(GuiSkin.getColorDrawable(GuiColors.LIGHT_DARK_GRAY), BTN_HEIGHT);
		boxStyle.checkboxOff = setMinSize(GuiSkin.getColorDrawable(Color.GRAY), BTN_HEIGHT);
		boxStyle.checkboxOver = setMinSize(GuiSkin.getColorDrawable(GuiColors.MOUSEOVER), BTN_HEIGHT);
		boxStyle.font = textButtonStyle.font;
		skin.add("default", boxStyle);
		return skin;
	}

	private static void createInitialColorDrawable() {
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGB888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		skin.add("background", new Texture(pixmap));
	}

	public static LabelStyle getLabelStyle(int size) {
		if (!labelStyles.containsKey(size))
			labelStyles.put(size, GuiSkin.createLabelStyle(size));

		return labelStyles.get(size);
	}

	static LabelStyle createLabelStyle(int size) {
		LabelStyle style = new LabelStyle();
		style.font = Resources.createFont(size);
		style.fontColor = Color.WHITE;
		return style;
	}

	public static int getFontSize(BitmapFont font) {
		return labelStyles.keySet().stream()
				.filter(size -> getFont(size) == font)
				.findFirst().orElse(-1);
	}

	public static Drawable getColorDrawable(Color color) {
		Drawable drawable = skin.newDrawable("background", color);
		Gui.setMinSize(drawable, 10);
		return drawable;
	}

	public static ProgressBarStyle createProgressBarStyle(float width, float height, Color color, boolean transparent) {
		Drawable knob = getColorDrawable(color);
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

	public static Skin get() {
		return skin;
	}
}
