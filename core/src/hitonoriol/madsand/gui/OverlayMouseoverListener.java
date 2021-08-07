package hitonoriol.madsand.gui;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.util.Utils;

/*
 * Mouseover listener that unfocuses the game when mouse enters an Actor
 */

public class OverlayMouseoverListener extends InputListener {
	private InputEvent.Type prevEvent = null;

	@Override
	public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
		if (filterEvent(event, fromActor))
			return;

		log(event, pointer, fromActor);
		Gui.gameUnfocused = true;
		Gui.overlay.hideTooltip();
	}

	@Override
	public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
		if (filterEvent(event, toActor))
			return;

		log(event, pointer, toActor);
		if (!Gui.dialogActive) {
			Gui.gameUnfocused = false;
			Gui.overlay.showTooltip();
		}
	}

	private boolean filterEvent(InputEvent event, Actor actor) {
		InputEvent.Type curEvent = event.getType();
		boolean validEventPair = (prevEvent == Type.enter && curEvent == Type.exit)
				|| (prevEvent == Type.mouseMoved && curEvent == Type.enter)
				|| (prevEvent == Type.mouseMoved && curEvent == Type.exit);

		boolean filter = !validEventPair || actor != null;
		if (filter && prevEvent != Type.touchDown)
			event.handle();

		return filter;
	}

	private void log(InputEvent event, int pointer, Actor actor) {
		Utils.dbg("{%X} [%s->%s] pointer=%d actor=%s",
				hashCode(),
				prevEvent, event.getType(), pointer,
				actor != null ? actor.getClass().getSimpleName() : "null");
	}

	@Override
	public boolean handle(Event e) {
		boolean result = super.handle(e);

		if (e instanceof InputEvent) {
			InputEvent event = (InputEvent) e;
			prevEvent = event.getType();

			if (event.getType() == Type.touchDown && event.getButton() == Buttons.RIGHT) {
				event.cancel();
				return true;
			}
		}

		return result;
	}
}
