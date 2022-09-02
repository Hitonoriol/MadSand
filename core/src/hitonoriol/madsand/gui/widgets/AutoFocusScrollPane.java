package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;

public class AutoFocusScrollPane extends ScrollPane {

	public AutoFocusScrollPane(Actor actor) {
		super(actor);
		addListener(new ScrollFocusListener(this));
	}
}