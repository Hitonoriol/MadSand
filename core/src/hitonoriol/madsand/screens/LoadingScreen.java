package hitonoriol.madsand.screens;

import com.badlogic.gdx.Gdx;

import hitonoriol.madsand.gui.stages.LoadingStage;
import hitonoriol.madsand.resources.GameAssetManager;

public class LoadingScreen extends AbstractScreen<LoadingStage> {
	private GameAssetManager manager;

	public LoadingScreen(GameAssetManager manager) {
		super(new LoadingStage(manager));
		this.manager = manager;
	}

	public void setStatusText(String text) {
		getStage().setStatusText(text);
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		manager.update();
		Gdx.graphics.requestRendering();
	}
}
