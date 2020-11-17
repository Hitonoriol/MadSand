package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.map.FishingSpot;
import me.xdrop.jrand.JRand;

public class FishingUI extends GameDialog {

	float WIDTH = 500;
	float HEIGHT = 450;
	int FISH_OFFSET_MAX = 215;
	float SPAWNER_INTERVAL = 0.4f;

	Group gameContainer = new Group();
	Image bobber;
	TextButton closeButton = new TextButton("Close", Gui.skin);

	float minSpawnTime = 0.7f;
	float maxSpawnTime = 2.5f;

	int maxFish = 5;
	int fishCount = 0;
	Fish activeFish; // Indicates the fish that's ready to be caught, only one at a time

	Timer timer = new Timer();

	private FishingUI(Stage stage) {
		super(stage);
	}

	public FishingUI(FishingSpot spot) {
		this(Gui.overlay);
		super.setTitle("Fishing").centerTitle();
		super.add().padTop(15).row();
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

		initBobberAnimation();
		initSpawner();
	}

	private void initSpawner() {
		timer.scheduleTask(new Task() {
			@Override
			public void run() {
				if (fishCount < maxFish && Utils.random.nextBoolean()) // TODO: probability must depend on Fishing skill
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
		int BOBBER_RNG = 25;
		float ANGLE_RNG = 10f;
		float ANIM_TIME = 1.5f;

		timer.scheduleTask(new Task() {
			boolean forward = true;
			int x, y;
			float angle;

			@Override
			public void run() {
				if (forward) {
					bobber.addAction(Actions.moveBy(x = Utils.rand(-BOBBER_RNG, BOBBER_RNG),
							y = Utils.rand(-BOBBER_RNG, BOBBER_RNG), ANIM_TIME));
					bobber.addAction(
							Actions.rotateBy(angle = JRand.flt().range(-ANGLE_RNG, ANGLE_RNG).gen(), ANIM_TIME));
				} else {
					bobber.addAction(Actions.moveBy(-x, -y, ANIM_TIME));
					bobber.addAction(Actions.rotateBy(-angle, ANIM_TIME));
				}

				forward = !forward;
			}
		}, 0, ANIM_TIME + 0.01f);
	}

	private class Fish extends Image {
		float ANIM_MAX = 2f, ANIM_MIN = 1.3f;
		float FADE_DUR = 0.4f;
		int SIZE_DELTA = 30;
		float animDuration;

		Fish() {
			super(fishTx);
			super.sizeBy(-Utils.rand(SIZE_DELTA));
			super.addAction(Actions.alpha(0));
			super.setPosition(WIDTH / 2 + Utils.rand(-FISH_OFFSET_MAX, FISH_OFFSET_MAX),
					HEIGHT / 2 + Utils.rand(-FISH_OFFSET_MAX, FISH_OFFSET_MAX));
			animDuration = JRand.flt().range(ANIM_MIN, ANIM_MAX).gen();
			super.addAction(Actions.fadeIn(FADE_DUR));
			initAnimation();
		}

		private void initAnimation() {
			timer.scheduleTask(new Task() {

				@Override
				public void run() {

				}
			}, FADE_DUR, animDuration);
		}
	}

	private static String resPath = "misc/fishing/";
	static Texture fishTx = Resources.loadTexture(resPath + "fish.png");
	static Texture bobberTx = Resources.loadTexture(resPath + "bobber.png");
	static NinePatchDrawable backgroundTx = Resources.loadNinePatch(resPath + "bg.png");
}
