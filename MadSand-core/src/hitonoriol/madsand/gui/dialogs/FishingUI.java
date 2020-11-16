package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.map.FishingSpot;

public class FishingUI extends GameDialog {

	float WIDTH = 500;
	float HEIGHT = 450;

	Group gameContainer = new Group();
	TextButton closeButton = new TextButton("Close", Gui.skin);

	private FishingUI(Stage stage) {
		super(stage);
	}

	public FishingUI(FishingSpot spot) {
		this(Gui.overlay);
		super.setTitle("Fishing").centerTitle();
		super.add().padTop(15).row();
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
	}

	private static String resPath = "misc/fishing/";
	static Texture fishTx = Resources.loadTexture(resPath + "fish.png");
	static Image bobber = new Image(Resources.loadTexture(resPath + "bobber.png"));
	static NinePatchDrawable backgroundTx = Resources.loadNinePatch(resPath + "bg.png");
}
