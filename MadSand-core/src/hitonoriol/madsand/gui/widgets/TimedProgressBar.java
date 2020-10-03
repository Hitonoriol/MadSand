package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import hitonoriol.madsand.Gui;

public class TimedProgressBar extends ProgressBar {

	static float MIN_VALUE = 0;
	static float MAX_VALUE = 100;
	static float STEP = 0.02f;

	float delay;
	TimedAction action;
	boolean done;

	private TimedProgressBar(float min, float max, float stepSize, boolean vertical, Skin skin) {
		super(min, max, stepSize, vertical, skin);
	}

	public TimedProgressBar(float delay) {
		this(MIN_VALUE, MAX_VALUE, STEP, false, Gui.skin);
		this.delay = delay;
		reset();
	}

	private void reset() {
		done = false;
		super.setAnimateDuration(0);
		super.setValue(0);
		super.setAnimateDuration(delay);
	}

	public void start(TimedAction action) {
		reset();
		this.action = action;
		super.setValue(super.getMaxValue());
	}

	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		if (!done && super.getVisualValue() == MAX_VALUE) {
			action.doAction();
			done = true;
		}
	}

	public interface TimedAction {
		public void doAction();
	}

}
