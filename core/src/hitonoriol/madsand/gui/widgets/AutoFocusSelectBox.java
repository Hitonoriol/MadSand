package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;

import hitonoriol.madsand.gui.Gui;

public class AutoFocusSelectBox<T> extends SelectBox<T> {

	public AutoFocusSelectBox() {
		super(Gui.skin);
		ScrollPane scroll = getScrollPane();
		scroll.addListener(new ScrollFocusListener(scroll));
	}
}
