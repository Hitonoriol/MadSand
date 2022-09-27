package hitonoriol.madsand.gui;

import static hitonoriol.madsand.gui.Gui.clearMinSize;
import static hitonoriol.madsand.gui.Gui.setMinSize;
import static hitonoriol.madsand.resources.Resources.loadNinePatch;
import static hitonoriol.madsand.util.Functional.with;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.assets.loaders.SkinLoader.SkinParameter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.ObjectMap;

import hitonoriol.madsand.resources.Resources;

public class GuiSkin {
	private static final String PATH = "skin/skin.json";
	private static final float MIN_HEIGHT = 35f, MIN_PADDING = 10f;

	private static Skin skin;
	private static Map<Integer, LabelStyle> labelStyles = new HashMap<>();

	private GuiSkin() {}

	private static void load() {
		skin = Resources.manager().loadAndWait(PATH, Skin.class, prepareFonts());
		setMinSizes();
	}

	private static void setMinSizes() {
		skin.get(TextField.TextFieldStyle.class).background.setMinHeight(MIN_HEIGHT);
		setMinSize(skin.get("default-horizontal", Slider.SliderStyle.class).background, MIN_PADDING);
		with(skin.get(List.ListStyle.class).selection, selection -> {
			selection.setTopHeight(MIN_PADDING);
			selection.setBottomHeight(MIN_PADDING);
		});
	}

	private static SkinParameter prepareFonts() {
		var fonts = new ObjectMap<String, Object>();
		fonts.put("default", Gui.getFont(Gui.FONT_S));
		fonts.put("h1", Gui.getFont(Gui.FONT_XL));
		fonts.put("h2", Gui.getFont(Gui.FONT_L));
		fonts.put("h3", Gui.getFont(Gui.FONT_M));
		return new SkinParameter("skin/skin.atlas", fonts);
	}

	public static Drawable transparency() {
		return loadNinePatch("misc/transparency");
	}

	public static NinePatchDrawable darkBackground() {
		return clearMinSize(new NinePatchDrawable(loadNinePatch("misc/darkness")));
	}

	public static NinePatchDrawable dialogBackground() {
		var background = new NinePatchDrawable(skin.getPatch("window-notitle"));
		return clearMinSize(background.tint(GuiColors.invert(skin.getColor("background"))));
	}

	public BitmapFont getDefaultFont() {
		return skin.getFont("default");
	}

	public static LabelStyle getLabelStyle(int size) {
		if (!labelStyles.containsKey(size))
			labelStyles.put(size, createLabelStyle(size));

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
				.filter(size -> Gui.getFont(size) == font)
				.findFirst().orElse(-1);
	}

	public static Drawable getColorDrawable(Color color) {
		Drawable drawable = skin.newDrawable("dot", color);
		clearMinSize(drawable);
		return drawable;
	}

	public static ProgressBarStyle createProgressBarStyle(float width, float height, Color color, boolean transparent) {
		Drawable knob = getColorDrawable(color);
		knob.setMinWidth(5);
		ProgressBarStyle style = new ProgressBarStyle(transparent ? transparency() : getColorDrawable(Color.GRAY),
				knob);

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
		if (skin == null)
			load();
		return skin;
	}
}
