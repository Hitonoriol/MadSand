package hitonoriol.madsand.gui.widgets.stats;

import java.util.function.Supplier;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.entities.skill.SkillValue;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.util.Utils;

public class StatProgressBar extends Group {

	public static float WIDTH = 185;
	public static float HEIGHT = 22;

	String stat;
	ProgressBar progressBar;
	Label statLabel = Widgets.label("");
	boolean round = true;
	private Supplier<Float> valueSupplier, maxValueSupplier, progressSupplier;

	public StatProgressBar(String stat) {
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

	public StatProgressBar setValueUpdater(Supplier<Float> updater) {
		valueSupplier = updater;
		return this;
	}

	public StatProgressBar setMaxValueUpdater(Supplier<Float> updater) {
		maxValueSupplier = updater;
		return this;
	}

	public StatProgressBar setProgressUpdater(Supplier<Float> updater) {
		progressSupplier = updater;
		return this;
	}

	public StatProgressBar setSkill(SkillValue skill) {
		setValueUpdater(() -> (float) skill.exp);
		setMaxValueUpdater(() -> (float) skill.requiredExp);
		setProgressUpdater(() -> skill.getProgress());
		update();
		return this;
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

	public StatProgressBar setValue(String text, float value, float maxValue) {
		statLabel.setText((stat.isEmpty() ? "" : (stat + ": ")) + text);
		progressBar.setValue(value);
		return this;
	}

	public StatProgressBar setValue(float value) {
		float maxValue = progressBar.getMaxValue();
		String valStr;
		if (round)
			valStr = Math.round(value) + "/" + Math.round(maxValue);
		else
			valStr = Utils.round(value) + "/" + Utils.round(maxValue);
		return setValue(valStr, value, maxValue);
	}

	public void update() {
		setValue(
			valueSupplier.get().intValue() + "/" + maxValueSupplier.get().intValue(),
			progressSupplier.get(),
			progressBar.getMaxValue()
		);
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
		var style = GuiSkin.createProgressBarStyle(WIDTH, HEIGHT - 5, color);
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
		return new StatProgressBar("LVL")
			.setStyle(Color.GOLDENROD);
	}
}
