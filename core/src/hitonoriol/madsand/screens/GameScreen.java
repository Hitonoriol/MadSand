package hitonoriol.madsand.screens;

import com.badlogic.gdx.Gdx;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.gui.stages.Overlay;
import hitonoriol.madsand.input.Keyboard;
import hitonoriol.madsand.input.Mouse;

public class GameScreen extends AbstractScreen<Overlay> {

	private WorldRenderer gameWorld;

	public GameScreen(WorldRenderer gameWorld) {
		super(Gui.overlay = new Overlay());
		this.gameWorld = gameWorld;
	}

	@Override
	public void show() {
		super.show();
		Gdx.graphics.setContinuousRendering(false);
		gameWorld.setCamFollowPlayer(true);
		if (!Gui.isGameUnfocused())
			stage.showTooltip();
	}

	@Override
	public void render(float delta) {
		gameWorld.render(delta);
		Mouse.update();
		if (!Gui.isGameUnfocused()) {
			Mouse.pollMouseMovement();
			Keyboard.pollGameKeys();
		}
		super.render(delta);
	}
}