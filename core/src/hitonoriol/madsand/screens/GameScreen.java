package hitonoriol.madsand.screens;

import com.badlogic.gdx.Gdx;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.gui.stages.Overlay;
import hitonoriol.madsand.input.Keyboard;
import hitonoriol.madsand.input.Mouse;

public class GameScreen extends AbstractScreen<Overlay> {

	private WorldRenderer gameWorld;

	public GameScreen(WorldRenderer gameWorld) {
		super(Gui.overlay); // TODO move it here
		this.gameWorld = gameWorld;
	}

	@Override
	public void show() {
		super.show();
		Gdx.graphics.setContinuousRendering(false);
		gameWorld.setCamFollowPlayer(true);
		stage.gameTooltip.setVisible(true);
	}

	@Override
	public void render(float delta) {
		gameWorld.render(delta);
		Mouse.mouseWorldCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		gameWorld.getCamera().unproject(Mouse.mouseWorldCoords);
		stage.pollGameConsole();
		if (!stage.isConsoleFocused() && !Gui.isGameUnfocused()) {
			Mouse.updCoords();
			Mouse.pollMouseMovement();
			Keyboard.pollGameKeys();
		}
		super.render(delta);
	}
}