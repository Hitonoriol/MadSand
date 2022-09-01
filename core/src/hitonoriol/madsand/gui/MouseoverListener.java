package hitonoriol.madsand.gui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;

import hitonoriol.madsand.util.Utils;

/*
 * Mouseover listener that unfocuses the game when mouse enters an Actor
 */

public class MouseoverListener extends InputListener {
	private InputEvent.Type prevEvent = null;

	private MouseoverListener() {}

	@Override
	public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
		log(event, pointer, fromActor);
		Gui.unfocusGame();
	}

	@Override
	public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
		log(event, pointer, toActor);
		Gui.resumeGameFocus();
	}

	private static final boolean debug = false;

	private void log(InputEvent event, int pointer, Actor actor) {
		if (debug)
			Utils.dbg("{%X} [%s->%s] pointer=%d actor=%s",
					hashCode(),
					prevEvent, event.getType(), pointer,
					actor != null ? actor.getClass().getSimpleName() : "null");
	}

	public static void setUp(Actor actor) {
		actor.setTouchable(Touchable.enabled);
		actor.addListener(new MouseoverListener());
	}
}
