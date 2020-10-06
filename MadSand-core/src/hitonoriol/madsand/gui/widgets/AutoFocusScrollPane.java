package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;

public class AutoFocusScrollPane extends ScrollPane {

	public AutoFocusScrollPane(Actor actor) {
		super(actor);
		addListener(new InputListener() {
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				getStage().setScrollFocus(AutoFocusScrollPane.this);
			}

			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				if (getStage() != null)
					getStage().setScrollFocus(null);
			}
		});
	}
}