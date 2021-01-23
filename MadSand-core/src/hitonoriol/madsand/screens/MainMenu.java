package hitonoriol.madsand.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.containers.PairFloat;
import hitonoriol.madsand.world.World;

public class MainMenu implements Screen {

	static float ymid;
	static float xmid = ymid = 0;
	private static float menuXStep = 0.8f, menuYStep = 0f;
	private static float menuOffset = 250;

	private GameWorldRenderer gameWorld;

	public MainMenu(GameWorldRenderer gameWorld) {
		this.gameWorld = gameWorld;
		initMenuAnimation();
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(Gui.mainMenu);
		gameWorld.setCamFollowPlayer(false);
		initMenuAnimation();
	}

	private void initMenuAnimation() {
		PairFloat playerPos = World.player.globalPos;
		gameWorld.setCamPosition(xmid = playerPos.x, ymid = playerPos.y);
		gameWorld.updateCamPosition();
	}

	private float cameraBounce(float n) {
		float ret = (float) -(n);
		return ret;
	}

	private void animateMenuBackground() {
		float x = gameWorld.getCamX(), y = gameWorld.getCamY();
		if (x > (xmid + menuOffset))
			menuXStep = cameraBounce(menuXStep);

		if (y > (ymid + menuOffset))
			menuYStep = cameraBounce(menuYStep);

		if (x < (xmid - menuOffset))
			menuXStep = cameraBounce(menuXStep);

		if (y < (ymid - menuOffset))
			menuYStep = cameraBounce(menuYStep);

		gameWorld.moveCamera(menuXStep, menuYStep);

		Gdx.graphics.requestRendering();
	}

	@Override
	public void render(float delta) {
		gameWorld.render(delta);
		animateMenuBackground();
		Gui.mainMenu.act();
		Gui.mainMenu.draw();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
