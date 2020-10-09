package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.world.World;

/*
 * Progress bar that *will be* displayed on interaction with objects (when gathering resources)
 * It should call player.interact() with short delays (~100ms?) until the object's harvestHp is > 0
 * The progressbar displays range [0; harvestHp]
 */

public class ResourceProgressBar extends TimedProgressBar {

	static ProgressBarStyle style;

	static float BASE_DELAY = 1f;
	static float D_MULTIPLIER = 0.014f;

	float HEIGHT = 20;
	float WIDTH = 175;

	MapObject object;
	int initialObjectHp;
	int damage;
	float nextValue;
	float valueStep;

	Timer.Task skipTask;
	Timer.Task wakeTask;

	boolean skip = false;

	float ANIMATION_MULTIPLIER = 1.3f;

	private ResourceProgressBar(float delay) {
		super(delay);
	}

	public ResourceProgressBar(MapObject object) {
		this(BASE_DELAY - (D_MULTIPLIER * (float) World.player.stats.skills.getSkillRollPercent(object.skill)));
		super.setAnimateDuration(delay);
		nextValue = delay;
		Utils.out("Skill delay: " + delay);

		super.setOrigin(Align.center);
		done = true;
		setStyle();

		int rangeMax = object.harvestHp;

		if (rangeMax < 1)
			rangeMax = 1;
		else
			--rangeMax;

		setRange(0, rangeMax);
		Utils.out("Setting range to [0," + object.harvestHp + "]");
		this.object = object;
		initialObjectHp = object.hp;

		super.setAction(new TimedProgressBar.TimedAction() {
			@Override
			public void doAction() {
				remove();
				Gui.gameResumeFocus();
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

	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		if (skip)
			return;

		if (getVisualValue() >= getValue()) {
			gatherResources();
			Utils.out("Object damage: " + damage);

			if (object.hp <= 0 || initialObjectHp != object.hp)
				action.doAction();

			if (damage > 0) {
				nextValue = delay / (float) damage;
				setAnimateDuration(nextValue);
				setValue(getValue() + damage);
				Utils.out("setValue to: " + getValue() + " | animating for: " + nextValue);
			} else {
				skip = true;
				Timer.instance().scheduleTask(skipTask, nextValue);
			}

		}
	}

	public int gatherResources() {
		return damage = World.player.gatherResources(object);
	}

	float WAKE_TIME = 0.019f; // Request rendering once per WAKE_TIME

	public void start() {
		Gui.overlay.addActor(this);

		Timer.instance().scheduleTask(wakeTask, WAKE_TIME, WAKE_TIME);

		Player player = World.player;
		Vector3 coords = new Vector3(player.x * MadSand.TILESIZE, player.y * MadSand.TILESIZE, 0);

		MadSand.camera.project(coords);
		super.setPosition(((coords.x + (coords.x - super.getWidth())) / 2) + MadSand.TILESIZE, coords.y - 40);
		Gui.gameUnfocus();
	}

	private void setStyle() {
		if (style == null) {
			style = new ProgressBarStyle(super.getStyle());
			style.background.setMinHeight(HEIGHT);
			style.knob.setMinHeight(HEIGHT);
			style.knobBefore.setMinHeight(HEIGHT);
			style.background.setMinWidth(WIDTH);
		}

		setStyle(style);

	}
}
