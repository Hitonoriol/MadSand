package hitonoriol.madsand.screens;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;

public abstract class AbstractScreen<T extends Stage> implements Screen {

	protected final T stage;
	private List<Runnable> onShowTasks = new ArrayList<>();

	public AbstractScreen(T screenStage) {
		stage = screenStage;
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(stage);
		onShowTasks.removeIf(task -> {
			task.run();
			return true;
		});
	}

	@Override
	public void render(float delta) {
		stage.act();
		stage.draw();
	}

	public T getStage() {
		return stage;
	}

	@Override
	public void resize(int width, int height) {}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void hide() {}

	@Override
	public void dispose() {
		stage.dispose();
	}
	
	public void onShow(Runnable task) {
		onShowTasks.add(task);
	}
}
