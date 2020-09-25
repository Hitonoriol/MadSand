package hitonoriol.madsand.gui.stages;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gui.OverlayMouseoverListener;
import hitonoriol.madsand.gui.widgets.TimedProgressBar;

public class TravelStage extends Stage {

	static float WIDTH = 500;
	static float ENTRY_HEIGHT = 50;

	float travelDelay = 2.75f;
	Label travelLabel;
	TimedProgressBar travelProgressBar;
	Table travelContainer;

	Screen gameScreen;

	static String travelString = "You are travelling to the next sector...";

	public TravelStage() {
		super();
		
		travelLabel = new Label(travelString, Gui.skin);
		travelProgressBar = new TimedProgressBar(travelDelay);
		travelContainer = new Table();
		travelContainer.addListener(new OverlayMouseoverListener());

		travelProgressBar.setSize(WIDTH, ENTRY_HEIGHT);

		travelLabel.setAlignment(Align.center);

		travelContainer.add(travelLabel).align(Align.center).size(WIDTH, ENTRY_HEIGHT).row();
		travelContainer.add(travelProgressBar).align(Align.center).size(WIDTH, ENTRY_HEIGHT).row();

		travelContainer.setSize(WIDTH, ENTRY_HEIGHT * 2);
		travelContainer.setBackground(Gui.darkBackgroundSizeable);

		travelContainer.setFillParent(true);

		super.addActor(travelContainer);
	}

	public void travel() {
		travelProgressBar.start(new TimedProgressBar.TimedAction() {

			@Override
			public void doAction() {
				MadSand.reset();
			}
			
		});
	}
}
