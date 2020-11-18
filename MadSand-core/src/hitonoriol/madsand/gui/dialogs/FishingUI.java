package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.enums.Skill;
import hitonoriol.madsand.gui.widgets.TimedProgressBar;
import hitonoriol.madsand.gui.widgets.TimedProgressBar.TimedAction;
import hitonoriol.madsand.map.FishingSpot;
import hitonoriol.madsand.world.World;
import me.xdrop.jrand.JRand;

public class FishingUI extends GameDialog {

	static float WIDTH = 525, HEIGHT = 475;
	static float BAR_WIDTH = WIDTH - 125, BAR_HEIGHT = 25;

	int FISH_OFFSET_MAX = 200;
	int FISH_OFFSET_MIN = 30;
	float SPAWNER_INTERVAL = 1.1f;

	Group gameContainer = new Group();
	Image bobber;
	TextButton closeButton = new TextButton("Close", Gui.skin);
	TimedProgressBar catchBar = new TimedProgressBar();

	float minSpawnTime = 1f;
	float maxSpawnTime = 6f;

	int maxFish = 4;
	int fishCount = 0;
	Fish activeFish; // Indicates the fish that's ready to be caught, only one at a time

	Timer timer = new Timer();
	FishingSpot spot;
	int baitId;

	private FishingUI(Stage stage) {
		super(stage);
	}

	public FishingUI(FishingSpot spot) {
		this(Gui.overlay);
		this.spot = spot;
		baitId = World.player.stats.offHand().id;
		maxFish += World.player.stats.skills.getLvl(Skill.Fishing);
		super.setTitle("Fishing").centerTitle();
		catchBar.setTriggerMode(false);
		catchBar.setVisualInterpolation(Interpolation.smooth);
		catchBar.setStyle(barStyle);
		resetCatchBar();
		super.add(catchBar).size(BAR_WIDTH, BAR_HEIGHT).padTop(15).row();
		bobber = new Image(bobberTx);
		Table background = new Table();
		background.setBackground(backgroundTx);
		background.setFillParent(true);
		gameContainer.addActor(background);
		gameContainer.addActor(bobber);
		bobber.setPosition((WIDTH / 2) + bobber.getImageWidth() / 2, HEIGHT / 2);

		super.add(gameContainer).size(WIDTH, HEIGHT).row();
		super.add(closeButton).width(200).height(45).pad(10);

		closeButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				remove();
			}
		});

		if (fishR == null) {
			fishR = new Sprite(fishTx);
			fishL = new Sprite(fishTx);
			fishL.flip(true, false);
		}

		initBobberAnimation();
		initSpawner();
		initClickListener();
	}

	private void initClickListener() {
		super.addListener(new ClickListener(Buttons.LEFT) {
			public void clicked(InputEvent event, float x, float y) {
				catchFish();
			}
		});
	}

	private void catchFish() {
		if (activeFish == null)
			return;

		activeFish.remove();
		Player player = World.player;
		player.addItem(spot.catchFish());
		player.stats.skills.increaseSkill(Skill.Fishing);
		player.inventory.delItem(player.stats.offHand(), 1);

		if (!player.hasItem(baitId)) {
			MadSand.warn("You're out of bait!");
			remove();
		}
	}

	private boolean spawnRoll() {
		return Utils.percentRoll(World.player.stats.skills.getSkillRollPercent(Skill.Fishing) + 10);
	}

	private void initSpawner() {
		timer.scheduleTask(new Task() {
			@Override
			public void run() {
				if (fishCount < maxFish && spawnRoll())
					spawnFish();

			}
		}, 0, SPAWNER_INTERVAL);
	}

	private void spawnFish() {
		++fishCount;
		timer.scheduleTask(new Task() {
			@Override
			public void run() {
				gameContainer.addActor(new Fish());
			}
		}, JRand.flt().range(minSpawnTime, maxSpawnTime).gen());
	}

	private void initBobberAnimation() {
		int XY_RNG = 25;
		float ANGLE_RNG = 10f;
		float ANIM_TIME = 2.5f;

		timer.scheduleTask(new Task() {
			boolean forward = true;
			int x, y;
			float angle;

			@Override
			public void run() {
				if (forward) {
					x = Utils.rand(-XY_RNG, XY_RNG);
					y = Utils.rand(-XY_RNG, XY_RNG);
					angle = JRand.flt().range(-ANGLE_RNG, ANGLE_RNG).gen();
				} else {
					x *= -1;
					y *= -1;
					angle *= -1;
				}

				bobber.addAction(Actions.moveBy(x, y, ANIM_TIME, Interpolation.smooth));
				bobber.addAction(Actions.rotateBy(angle, ANIM_TIME));
				forward = !forward;
			}
		}, 0, ANIM_TIME + 0.01f);
	}

	private void resetCatchBar() {
		catchBar.clearActions();
		catchBar.setColor(Color.LIME);
		catchBar.reset();
		catchBar.setVisible(false);
	}

	public boolean remove() {
		timer.clear();
		timer.stop();
		return super.remove();
	}

	private class Fish extends Image {
		float ANIM_MAX = 2f, ANIM_MIN = 1.3f;
		float FADE_DUR = 0.4f;
		int SIZE_DELTA = 30;
		int SWIM_DELTA = 15;
		float MIN_CATCH_LIFETIME = 4.5f;
		float animDuration;
		float catchTime;
		double lifeTime = 0;
		float minCatchTime = 0.28f, maxCatchTime = 1.1f;
		private Task animation;

		Fish() {
			super(Utils.random.nextBoolean() ? fishL : fishR);
			super.sizeBy(-Utils.rand(SIZE_DELTA));
			super.addAction(Actions.color(Color.BLACK));
			super.addAction(Actions.alpha(0));
			super.setPosition(WIDTH / 2 + Utils.signRand(FISH_OFFSET_MIN, FISH_OFFSET_MAX),
					HEIGHT / 2 + Utils.signRand(FISH_OFFSET_MIN, FISH_OFFSET_MAX));

			animDuration = JRand.flt().range(ANIM_MIN, ANIM_MAX).gen();
			lifeTime = -animDuration;

			float skillTimeBonus = (float) World.player.stats.skills.getLvl(Skill.Fishing) / 25f;
			catchTime = JRand.flt().range(minCatchTime + skillTimeBonus, maxCatchTime + skillTimeBonus).gen();

			super.addAction(Actions.fadeIn(FADE_DUR));
			initAnimation();
		}

		private boolean catchRoll() {
			return Utils.percentRoll(World.player.stats.skills.getSkillRollPercent(Skill.Fishing) + 5);
		}

		private void initAnimation() {
			animation = new Task() {
				boolean forward = true;
				int y = 0;

				@Override
				public void run() {
					lifeTime += animDuration;

					if (forward)
						y = Utils.rand(-SWIM_DELTA, SWIM_DELTA);
					else
						y *= -1;

					addAction(Actions.moveBy(0, y, animDuration, Interpolation.smoother));
					forward = !forward;

					if (lifeTime > MIN_CATCH_LIFETIME && activeFish == null)
						if (catchRoll())
							ready();
				}
			};
			timer.scheduleTask(animation, 0, animDuration);
		}

		float ready() {
			activeFish = this;
			super.addAction(Actions.color(Color.RED, catchTime, Interpolation.smooth));
			catchBar.addAction(Actions.color(Color.RED, catchTime, Interpolation.smoother));
			catchBar.setVisible(true);
			catchBar.setDelay(catchTime);
			catchBar.start(new TimedAction() {
				@Override
				public void doAction() {
					MadSand.warn("The fish got away!");
					remove();
				}
			});
			return catchTime;
		}

		public boolean remove() {
			animation.cancel();
			resetCatchBar();
			--fishCount;
			if (activeFish == this)
				activeFish = null;
			return super.remove();
		}
	}

	private static String resPath = "misc/fishing/";
	static Sprite fishR, fishL;
	static Texture fishTx = Resources.loadTexture(resPath + "fish.png");
	static Texture bobberTx = Resources.loadTexture(resPath + "bobber.png");
	static NinePatchDrawable backgroundTx = Resources.loadNinePatch(resPath + "bg.png");
	static ProgressBar.ProgressBarStyle barStyle = Gui.createProgressBarStyle(BAR_WIDTH, BAR_HEIGHT, Color.LIME, true);
}
