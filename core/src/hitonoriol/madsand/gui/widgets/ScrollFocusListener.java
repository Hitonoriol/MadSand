package hitonoriol.madsand.gui.widgets;

import static hitonoriol.madsand.MadSand.getStage;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class ScrollFocusListener extends InputListener {
	private Actor actor;

	public ScrollFocusListener(Actor actor) {
		this.actor = actor;
	}

	@Override
	public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
		getStage().setScrollFocus(actor);
	}

	@Override
	public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
		if (getStage() != null)
			getStage().setScrollFocus(null);
	}
}
