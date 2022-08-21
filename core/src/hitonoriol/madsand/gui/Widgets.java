package hitonoriol.madsand.gui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import hitonoriol.madsand.Gui;

public class Widgets {
	public static Table table() {
		return new Table(Gui.skin);
	}

	public static TextButton button(String text) {
		return new TextButton(text, Gui.skin);
	}
	
	public static TextButton button() {
		return button("");
	}

	public static Label label(String text) {
		return new Label(text, Gui.skin);
	}
	
	public static Label label() {
		return label("");
	}

	public static Label label(String text, int fontSize) {
		return Gui.setFontSize(label(text), fontSize);
	}
}
