package hitonoriol.madsand.gui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import hitonoriol.madsand.Gui;

/*
 * Mouseover listener that "pauses" the game when mouse enters an Actor
 */

public class OverlayMouseoverListener extends InputListener {
	
	private static final OverlayMouseoverListener instance = new OverlayMouseoverListener();
	
	public OverlayMouseoverListener() {
		super();
	}

	@Override
	public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
		Gui.gameUnfocused = true;
		Gui.overlay.hideTooltip();
	}

	@Override
	public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
		if (!Gui.dialogActive) {
			Gui.gameUnfocused = false;
			Gui.overlay.showTooltip();
		}
	}
	
	public static OverlayMouseoverListener instance() {
		return instance;
	}
}
