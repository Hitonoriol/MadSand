package hitonoriol.madsand.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.gui.stages.TravelStage;

public class TravelScreen extends InputAdapter implements Screen {

	Game game;
	TravelStage travelStage;

	public TravelScreen(Game game) {
		super();
		this.game = game;
		travelStage = new TravelStage();
	}

	@Override
	public void show() {
		Gui.gameUnfocused = true;
		Gdx.input.setInputProcessor(this);
		travelStage.travel();
	}

	@Override
	public void render(float delta) {
		
		Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		travelStage.act();
		travelStage.draw();

	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		Gui.gameUnfocused = false;
	}

	@Override
	public void dispose() {
		Utils.out("TravelScreen has been disposed!!111");
		travelStage.dispose();
	}

}
