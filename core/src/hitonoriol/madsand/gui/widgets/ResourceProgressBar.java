package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.skill.SkillContainer;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.world.World;

/*
 * Progress bar that is displayed on interaction with objects (when gathering resources)
 * It should call player.interact() with short delays (~BASE - <D_MUL...> * skill lvl) until the object's harvestHp is < 0
 * The progressbar displays range [0; harvestHp]
 */

public class ResourceProgressBar extends TimedProgressBar {

	static ProgressBarStyle style;

	static float BASE_DELAY = 0.29f;
	static float D_MULTIPLIER = 0.8f * (BASE_DELAY / (float) SkillContainer.MAX_SKILL_ROLL_PERCENT);

	float HEIGHT = 20;
	float WIDTH = 175;
	float YPADDING = 45;
	float LABEL_PADDING = 2.5f;

	Label progressLabel;

	MapObject object;
	int initialObjectHp;
	int damage;
	float nextValue;
	float valueStep;

	Timer.Task skipTask;
	Timer.Task wakeTask;

	boolean skip = false;

	float ANIMATION_MULTIPLIER = 1.3f;

	public ResourceProgressBar(MapObject object) {
		super(BASE_DELAY - getDelayDelta(object));
		super.setAnimateDuration(delay);
		nextValue = delay;

		done = true;
		setStyle();

		int rangeMax = object.harvestHp;

		if (rangeMax < 1)
			rangeMax = 1;

		setRange(0, rangeMax);
		this.object = object;
		initialObjectHp = object.hp;

		progressLabel = new Label("", Gui.skin);
		progressLabel.setAlignment(Align.center);
		progressLabel.setWidth(WIDTH + 150);

		super.setAction(new TimedProgressBar.TimedAction() {
			@Override
			public void doAction() {
				remove();
				Gui.gameResumeFocus();
				Gui.refreshOverlay();
				Timer.instance().scheduleTask(new Timer.Task() {
					@Override
					public void run() {
						progressLabel.remove();
					}
				}, 0.5f);
			}
		});

		skipTask = new Timer.Task() {
			@Override
			public void run() {
				skip = false;
				this.cancel();
			}
		};

		wakeTask = new Timer.Task() {
			@Override
			public void run() {
				Gdx.graphics.requestRendering();

				if (!Gui.gameUnfocused)
					this.cancel();
			}
		};
	}

	private static float getDelayDelta(MapObject object) {
		return (D_MULTIPLIER * (float) SkillContainer.skillLvlPercent(
				MadSand.player().stats.skills.getLvl(object.getInteractionSkill()))) / 5f;
	}

	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		if (skip)
			return;

		if (getVisualValue() >= getValue()) {
			if (gatherResources() < -1)
				action.doAction();

			if (object.hp <= 0 || initialObjectHp != object.hp)
				action.doAction();

			if (damage > 0) {
				nextValue = delay / (float) damage;
				setAnimateDuration(nextValue);
				setValue(getValue() + damage);
			} else {
				skip = true;
				Timer.instance().scheduleTask(skipTask, nextValue);
			}

		}
	}

	public int gatherResources() {
		damage = World.player.gatherResources(object);
		progressLabel.setText(Gui.overlay.gameLog.getLastPrintedLine());
		return damage;
	}

	float WAKE_TIME = 0.019f; // Request rendering once per WAKE_TIME

	public void start() {
		Gui.overlay.addActor(this);
		Gui.overlay.addActor(progressLabel);

		Timer.instance().scheduleTask(wakeTask, WAKE_TIME, WAKE_TIME);

		Player player = World.player;
		Vector3 coords = new Vector3(player.x * MadSand.TILESIZE, player.y * MadSand.TILESIZE, 0);

		MadSand.getCamera().project(coords);
		coords.y -= YPADDING;
		super.setPosition(centerRelative(coords.x, getWidth(), player.getSpriteWidth()), coords.y);
		progressLabel.setPosition(centerRelative(coords.x, progressLabel.getWidth(), player.getSpriteWidth()),
				coords.y - LABEL_PADDING);
		Gui.gameUnfocus();
	}

	private float centerRelative(float x, float width, float objectWidth) {
		return (x + (objectWidth * 0.75f)) - (width / 2);
	}

	private void setStyle() {
		if (style == null)
			style = Gui.createProgressBarStyle(WIDTH, HEIGHT, Color.DARK_GRAY);

		setStyle(style);
	}
}
