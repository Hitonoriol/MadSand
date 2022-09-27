package hitonoriol.madsand.gui.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.resources.GameAssetManager;

public class LoadingStage extends Stage {
	private GameAssetManager manager;

	private ProgressBar progressBar = new ProgressBar(0, 1, 0.005f, false, Gui.skin);
	private Label statusLabel = Widgets.label(Align.center, "Doing stuff...");

	public LoadingStage(GameAssetManager manager) {
		super(Gui.viewport());
		this.manager = manager;
		progressBar.setAnimateDuration(0.1f);
		Table container = new Table();
		container.setFillParent(true);
		container.align(Align.center);
		container.add(Widgets.label("Loading...", Gui.FONT_XL)).align(Align.center).padBottom(20).row();
		container.add(progressBar).size(Gdx.graphics.getWidth() * 0.5f, 35).row();
		container.add(statusLabel).growX().row();
		addActor(container);
	}
	
	public void setStatusText(String text) {
		statusLabel.setText(text);
	}

	@Override
	public void act() {
		if (progressBar.getValue() != manager.getProgress())
			progressBar.setValue(manager.getProgress());
		if (manager.isFinished() && progressBar.getVisualPercent() == 1f)
			manager.completeVisualLoading();
		super.act();
	}
}
