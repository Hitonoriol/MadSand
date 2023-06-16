package hitonoriol.madsand.screens;

import com.badlogic.gdx.Gdx;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gui.stages.MainMenuStage;

public class MainMenu extends AbstractScreen<MainMenuStage> {

	static float ymid;
	static float xmid = ymid = 0;
	private static float menuXStep = 0.8f, menuYStep = 0f;
	private static float menuOffset = 250;
	private boolean freshStart = true;

	private WorldRenderer gameWorld;

	public MainMenu(WorldRenderer gameWorld) {
		super(new MainMenuStage());
		this.gameWorld = gameWorld;
	}

	@Override
	public void show() {
		super.show();
		gameWorld.setCamFollowPlayer(false);
		initMenuAnimation();

		if (freshStart)
			freshStart = false;
		else
			stage.showResumeTable();
	}

	private void initMenuAnimation() {
		var playerPos = MadSand.player().screenPosition;
		gameWorld.setCamPosition(xmid = playerPos.x, ymid = playerPos.y);
		gameWorld.updateCamPosition();
	}

	private float cameraBounce(float n) {
		return -(n);
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
		var manager = getStage().getShaderManager();
		manager.beginEffects();
		gameWorld.render(delta);
		animateMenuBackground();
		super.render(delta);
		manager.endEffects();
	}
}
