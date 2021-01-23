package hitonoriol.madsand.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import hitonoriol.madsand.Gui;

public class CraftScreen implements Screen {

	private GameWorldRenderer gameWorld;

	public CraftScreen(GameWorldRenderer gameWorld) {
		this.gameWorld = gameWorld;
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(Gui.craftMenu);
	}

	@Override
	public void render(float delta) {
		gameWorld.render(delta);
		Gui.craftMenu.act();
		Gui.craftMenu.draw();
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
