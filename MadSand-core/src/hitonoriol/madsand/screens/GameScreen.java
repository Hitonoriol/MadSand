package hitonoriol.madsand.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Keyboard;
import hitonoriol.madsand.Mouse;

public class GameScreen implements Screen {

	private GameWorldRenderer gameWorld;

	public GameScreen(GameWorldRenderer gameWorld) {
		super();
		this.gameWorld = gameWorld;
	}
	
	@Override
	public void show() {
		Gdx.graphics.setContinuousRendering(false);
		gameWorld.setCamFollowPlayer(true);
		Gdx.input.setInputProcessor(Gui.overlay);
		Gui.overlay.gameTooltip.setVisible(true);
	}

	@Override
	public void render(float delta) {
		gameWorld.render(delta);
		Mouse.mouseinworld.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		gameWorld.getCamera().unproject(Mouse.mouseinworld);
		Gui.overlay.pollGameConsole();
		if (Gui.overlay.getKeyboardFocus() != Gui.overlay.getConsoleField() && !Gui.isGameUnfocused()) {
			Mouse.updCoords();
			Mouse.pollMouseMovement();
			Keyboard.pollGameKeys();
		}
		Gui.overlay.act();
		Gui.overlay.draw();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
	}

}
