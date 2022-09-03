package hitonoriol.madsand.gui;

import java.util.function.Consumer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class Widgets {
	public static Table table() {
		return new Table(Gui.skin);
	}

	public static Table table(Consumer<Cell<Actor>> forEachCell, Actor... actors) {
		Table table = table();
		for (Actor actor : actors) {
			Cell<Actor> cell = table.add(actor);
			if (forEachCell != null)
				forEachCell.accept(cell);
		}
		return table;
	}

	@SafeVarargs
	public static <T extends Actor> Table table(T... actors) {
		return table(null, actors);
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
