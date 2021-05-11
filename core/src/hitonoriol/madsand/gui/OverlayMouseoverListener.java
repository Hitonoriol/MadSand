package hitonoriol.madsand.gui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import hitonoriol.madsand.Gui;

/*
 * Mouseover listener that "pauses" the game when mouse enters an Actor
 */

public class OverlayMouseoverListener extends InputListener {

	private static final OverlayMouseoverListener instance = new OverlayMouseoverListener();
	private InputEvent.Type prevEvent = null;

	public OverlayMouseoverListener() {
		super();
	}

	@Override
	public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
		if (prevEvent == Type.touchDown)
			return;

		Gui.gameUnfocused = true;
		Gui.overlay.hideTooltip();
	}

	@Override
	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
		event.cancel();
		return false;
	}

	@Override
	public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
		if (!Gui.dialogActive) {
			Gui.gameUnfocused = false;
			Gui.overlay.showTooltip();
		}
	}

	@Override
	public boolean handle(Event e) {
		boolean result = super.handle(e);

		if (e instanceof InputEvent)
			prevEvent = ((InputEvent) e).getType();

		return result;
	}

	public static OverlayMouseoverListener instance() {
		return instance;
	}
}
