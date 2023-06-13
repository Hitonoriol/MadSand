package hitonoriol.madsand.gui.widgets.overlay;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static hitonoriol.madsand.MadSand.getRenderer;
import static hitonoriol.madsand.MadSand.toScreen;
import static hitonoriol.madsand.resources.Resources.TILESIZE;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.map.MapEntity;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;
import me.xdrop.jrand.JRand;

public class AnimatedWorldText extends Group {
	private Label label = Widgets.label();
	private Runnable coordUpdater;
	private Vector3 screenPosition = new Vector3();
	private Vector3 offset = new Vector3();
	private float objectWidth = Resources.TILESIZE;
	private final float verticalSpeed = JRand.flt().range(0.25f, 0.85f).gen();

	private final float MAX_OFFSET = toScreen(2.5f * TILESIZE);

	private AnimatedWorldText(String text) {
		addActor(label);
		label.setText(text);
		Gui.setFontSize(label, Utils.rand(17, 20));
		label.setWidth(Gui.getTextWidth(text));
		setOrigin(Align.center);
		offset.x = JRand.flt().range(-TILESIZE / 8f, TILESIZE / 8f).gen();

		final float animTime = JRand.flt().range(1f, 1.85f).gen();
		addAction(
			sequence(
				alpha(0),
				fadeIn(0.1f, Interpolation.fade),
				parallel(
					scaleTo(0.3f, 0.3f, animTime),
					fadeOut(animTime, Interpolation.fade)
				),
				run(this::remove)
			)
		);
	}

	public AnimatedWorldText(MapEntity entity, String text) {
		this(text);
		var coords = entity.getVisualPosition();
		objectWidth = entity.getSprite().getRegionWidth();
		offset.y = toScreen(0.25f * entity.getSprite().getRegionHeight());
		setCoordUpdater(() -> screenPosition.set(coords.x, coords.y, 0));
	}

	private void setCoordUpdater(Runnable updater) {
		coordUpdater = updater;
	}

	@Override
	public void act(float delta) {
		updateScreenPosition();
		setPosition(screenPosition.x, screenPosition.y);
		super.act(delta);
	}

	private void updateScreenPosition() {
		if (offset.y < MAX_OFFSET)
			offset.y += verticalSpeed;

		coordUpdater.run();
		getRenderer().getCamera().project(screenPosition);
		screenPosition.x = Gui.relativeCenterX(screenPosition.x, objectWidth, label.getWidth() * getScaleX());
		screenPosition.add(offset);
	}
}
