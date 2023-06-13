package hitonoriol.madsand.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;

public class GuiColors extends Color {
	public static final Color MOUSEOVER = new Color(0xa5a5a5ff);
	public static final Color LIGHT_DARK_GRAY = new Color(0x4f4f4fff);
	public static final Color WARNING = new Color(0xff9185ff);
	public static final Color NOTICE = new Color(0x16e1eaff);
	public static final Color NOTICE_ALT = new Color(0x58ffb1ff);
	public static final Color BUTTON_DISABLED = new Color(0x3f3f3fdc);

	static {
		Colors.put("WARNING", WARNING);
		Colors.put("NOTICE", NOTICE);
		Colors.put("NOTICE_ALT", NOTICE_ALT);
	}

	public static String getTag(Color color) {
		return "[#" + color + "]";
	}

	public static Color invert(Color color) {
		float alpha = color.a;
		var inverted = new Color(Color.WHITE).sub(color);
		inverted.a = alpha;
		return inverted;
	}
}
