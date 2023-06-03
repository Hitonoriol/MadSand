package hitonoriol.madsand.gui.stages;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleBy;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.resources.GameAssetManager;

public class LoadingStage extends Stage {
	private GameAssetManager manager;

	private ProgressBar progressBar = new ProgressBar(0, 1, 0.005f, false, Gui.skin);
	private Label statusLabel = Widgets.label(Align.center, "Doing stuff...");
	private Table container = new Table();
	
	public LoadingStage(GameAssetManager manager) {
		super(Gui.viewport());
		this.manager = manager;
		progressBar.setAnimateDuration(1f);
		container.setFillParent(true);
		container.align(Align.center);
		container.add(Widgets.label("Loading...", Gui.FONT_XL)).align(Align.center).padBottom(20).row();
		container.add(progressBar).size(Gdx.graphics.getWidth() * 0.5f, 35).row();
		container.add(statusLabel).growX().row();
		
		// Darken the container's background
		var darkBg = new Color(Color.BLACK);
		darkBg.a = 0.75f;
		container.setBackground(GuiSkin.getColorDrawable(darkBg));
		
		addActor(container);
	}
	
	public void setStatusText(String text) {
		statusLabel.setText(text);
	}
	
	public void setBackground(Texture background) { 
		var bg = new Image(background);
		bg.setFillParent(true);
		addActor(bg);
		bg.toBack();
		bg.setOrigin(Align.center);
		float scale = 0.35f;
		float duration = 0.5f;
		bg.addAction(scaleBy(scale, scale));
		bg.addAction(
			forever(sequence(
					scaleBy(-scale, -scale, duration, Interpolation.smooth),
					scaleBy(scale, scale, duration, Interpolation.smooth)
			))
		);
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
