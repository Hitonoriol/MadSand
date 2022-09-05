package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import hitonoriol.madsand.gui.Gui;

public class TimedProgressBar extends ProgressBar {

	static float MIN_VALUE = 0;
	static float MAX_VALUE = 100;
	static float STEP = 0.01f;

	float triggerValue = 100;
	float defaultValue = 0;

	protected float delay;
	protected Runnable action;
	protected boolean done;

	private TimedProgressBar(float min, float max, float stepSize, boolean vertical, Skin skin) {
		super(min, max, stepSize, vertical, skin);
	}

	public TimedProgressBar(float delay) {
		this(MIN_VALUE, MAX_VALUE, STEP, false, Gui.skin);
		setDelay(delay);
	}

	public TimedProgressBar() {
		this(1);
	}

	public void setDelay(float seconds) {
		this.delay = seconds;
		reset();
	}

	// true - animate from 0 to 100
	// false - animate from 100 to 0
	public void setTriggerMode(boolean triggerWhenFull) {
		triggerValue = triggerWhenFull ? MAX_VALUE : MIN_VALUE;
		defaultValue = triggerWhenFull ? MIN_VALUE : MAX_VALUE;
		reset();
	}

	protected void finish() {
		done = true;
	}

	public void actionTriggered() {
		action.run();
		finish();
	}

	public void reset() {
		done = false;
		super.setAnimateDuration(0);
		super.setValue(defaultValue);
		super.setAnimateDuration(delay);
	}

	public void start(Runnable action) {
		reset();
		setAction(action);
		super.setValue(triggerValue);
	}

	public void setAction(Runnable action) {
		this.action = action;
	}

	public Runnable getAction() {
		return action;
	}

	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		if (!done && super.getVisualValue() == triggerValue)
			actionTriggered();
	}
}
