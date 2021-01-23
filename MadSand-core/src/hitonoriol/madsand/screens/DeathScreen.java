package hitonoriol.madsand.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.gui.stages.DeathStage;
import hitonoriol.madsand.world.World;

public class DeathScreen implements Screen {

	private GameWorldRenderer gameWorld;
	private DeathStage deathStage = new DeathStage();

	public DeathScreen(GameWorldRenderer gameWorld) {
		this.gameWorld = gameWorld;
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(deathStage);
		deathStage.setDeathMessage("You survived " + Utils.timeString(World.player.getSurvivedTime()));
	}

	@Override
	public void render(float delta) {
		gameWorld.render(delta);
		deathStage.act();
		deathStage.draw();
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
		deathStage.dispose();
	}
}
