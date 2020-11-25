package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Utils;

public class StatProgressBar extends Group {

	public static float WIDTH = 185;
	public static float HEIGHT = 22;

	String stat;
	ProgressBar progressBar;
	Label statLabel = new Label("", Gui.skin);
	boolean round = true;

	public StatProgressBar(String stat) {
		super();

		progressBar = new ProgressBar(0, 100, 0.02f, false, Gui.skin);
		progressBar.setAnimateDuration(0.1f);

		statLabel.setAlignment(Align.center);
		this.stat = stat;

		setProgressSize(WIDTH, HEIGHT);
		super.addActor(progressBar);
		super.addActor(statLabel);
	}

	public StatProgressBar() {
		this("");
	}

	public StatProgressBar setStatText(String stat) {
		this.stat = stat;
		return this;
	}

	public StatProgressBar setRange(float min, float max) {
		progressBar.setRange(min, max);
		return this;
	}

	public StatProgressBar roundValues(boolean round) {
		this.round = round;
		return this;
	}

	public StatProgressBar setValue(float value) {
		String valStr;

		if (round)
			valStr = Math.round(value) + "/" + Math.round(progressBar.getMaxValue());
		else
			valStr = Utils.round(value) + "/" + Utils.round(progressBar.getMaxValue());

		statLabel.setText((stat.isEmpty() ? "" : (stat + ": ")) + valStr);
		progressBar.setValue(value);
		return this;
	}

	public StatProgressBar setProgressSize(float width, float height) {
		progressBar.setSize(width, height);
		statLabel.setSize(width, height);
		super.setSize(width, height);
		return this;
	}

	public StatProgressBar setStyle(ProgressBarStyle style) {
		progressBar.setStyle(style);
		return this;
	}

	public StatProgressBar setStyle(Color color) {
		ProgressBar.ProgressBarStyle style = Gui.createProgressBarStyle(WIDTH, HEIGHT - 5, color);
		progressBar.setStyle(style);
		return this;
	}

	public static StatProgressBar createHpBar() {
		return new StatProgressBar("HP").setStyle(Color.LIME);
	}

	public static StatProgressBar createStaminaBar() {
		return new StatProgressBar("Stamina").setStyle(Color.SALMON);
	}

	public static StatProgressBar createLevelBar() {
		return new StatProgressBar("LVL").setStyle(Color.GOLDENROD);
	}
}
