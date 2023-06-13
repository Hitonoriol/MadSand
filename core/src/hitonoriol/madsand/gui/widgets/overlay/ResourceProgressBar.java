package hitonoriol.madsand.gui.widgets.overlay;

import java.util.ArrayDeque;
import java.util.Queue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.skill.SkillContainer;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.TimedProgressBar;
import hitonoriol.madsand.input.Mouse;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.TimeUtils;
import hitonoriol.madsand.util.Utils;

public class ResourceProgressBar extends Table {
	private static float HEIGHT = 20, WIDTH = 175;
	private static float YPADDING = 30, LABEL_PADDING = 2.5f;
	private final static float WAKE_TIME = 0.019f; // Request rendering once per WAKE_TIME
	private static ProgressBarStyle style = GuiSkin.createProgressBarStyle(WIDTH, HEIGHT, Color.DARK_GRAY);

	private TimedProgressBar bar = new TimedProgressBar();
	private Label progressLabel = Widgets.label("");

	private MapObject object;
	private int totalHits;
	private Timer.Task wakeTask = TimeUtils.createTask(() -> Gdx.graphics.requestRendering());
	private Queue<Integer> damageQueue = new ArrayDeque<>();

	public ResourceProgressBar(MapObject object) {
		this.object = object;
		bar.setStyle(style);
		bar.setAnimateDuration(0);
		bar.setValue(0);
		progressLabel.setAlignment(Align.center);
		progressLabel.setWidth(WIDTH + 150);

		defaults().align(Align.center);
		add(bar).padBottom(LABEL_PADDING).row();
		add(progressLabel);

		bar.setAction(() -> {
			Utils.dbg("Done gathering resources from %s\n", object);
			remove();
			progressLabel.remove();
			Gui.resumeGameFocus();
			Gui.refreshOverlay();
			Mouse.refreshTooltip();
			wakeTask.cancel();
		});
	}

	public void start() {
		Gui.overlay.addActor(this);
		Timer.instance().scheduleTask(wakeTask, WAKE_TIME, WAKE_TIME);
		var player = MadSand.player();
		var coords = new Vector3(player.x * Resources.TILESIZE, player.y * Resources.TILESIZE, 0);
		final float playerWidth = player.getSprite().getRegionWidth();
		MadSand.getCamera().project(coords);
		setPosition(Gui.relativeCenterX(coords.x, playerWidth, getWidth()), coords.y - YPADDING);
		Gui.unfocusGame();
		startGatheringAnimation();
	}

	private void startGatheringAnimation() {
		preCalculateGathering();
		bar.setRange(0, Math.max(1, hitsLeft() - 1));
		bar.setValue(hitsLeft());
	}

	private int hitsLeft() {
		return damageQueue.size();
	}

	public void preCalculateGathering() {
		if (damageQueue.isEmpty()) {
			int hp = object.harvestHp;
			int hitDmg;
			while (hp >= 0) {
				hitDmg = object.simulateHit(MadSand.player());
				damageQueue.add(hitDmg);

				if (hitDmg == -1)
					continue;

				hp -= hitDmg;
			}
		}
		float hitDuration = calcHitDuration(object), hitCoef = calcHitCoef();
		float duration = hitsLeft() * hitDuration * hitCoef;
		bar.setAnimateDuration(duration);
		totalHits = hitsLeft();
		var skills = MadSand.player().stats.skills;
		var skill = object.getInteractionSkill();
		Utils.dbg(
			"Hits to be made: %d / total duration: %f secs\n"
				+ "[hitDuration=%f, maxSkillEffect=%f, hpCoef=%f, skillBonus=%f, skillEffect%%=%f]",
			totalHits, duration, hitDuration, SkillContainer.maxSkillEffect(skill), hitCoef,
			skills.calcSkillBonusEffect(skill),
			skills.getSkillEffectPercent(skill)
		);
	}

	private float calcHitCoef() {
		float coef = (float) Math.sqrt(Math.log10(Math.max(1.3, object.harvestHp + object.maxHp)));
		return Math.max(0.5f, Math.min(3f, coef));
	}

	private static float calcHitDuration(MapObject object) {
		float effectiveness = (float) (1
			/ MadSand.player().stats.skills.getSkillEffectPercent(object.getInteractionSkill()));
		return (float) Math.sqrt(Math.log(effectiveness + 0.0125)) * 0.135f;
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		float visualValue = bar.getVisualValue();
		if ((int) visualValue == totalHits - hitsLeft())
			gatherResources();

		if (hitsLeft() == 0 || visualValue >= bar.getValue())
			bar.actionTriggered();
	}

	private void gatherResources() {
		MadSand.player().gatherResources(object, () -> damageQueue.poll());
		if (!damageQueue.isEmpty())
			progressLabel.setText(Gui.overlay.getGameLog().getLastPrintedLine());
	}
}
