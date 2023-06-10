package hitonoriol.madsand.gui.stages;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.MadSand.Screens;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.MouseoverListener;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.TimedProgressBar;

public class TravelStage extends Stage {

	static float WIDTH = 500;
	static float ENTRY_HEIGHT = 50;

	float travelDelay = 1.5f;
	Label travelLabel;
	TimedProgressBar travelProgressBar;
	Table travelContainer;

	Screen gameScreen;

	static String travelString = "You are travelling to the next sector...";

	public TravelStage() {
		super(Gui.viewport());
		travelLabel = Widgets.label(travelString);
		travelProgressBar = new TimedProgressBar(travelDelay);
		travelContainer = Widgets.table();
		MouseoverListener.setUp(travelContainer);

		travelProgressBar.setSize(WIDTH, ENTRY_HEIGHT);

		travelLabel.setAlignment(Align.center);

		travelContainer.add(travelLabel).align(Align.center).size(WIDTH, ENTRY_HEIGHT).row();
		travelContainer.add(travelProgressBar).align(Align.center).size(WIDTH, ENTRY_HEIGHT).row();

		travelContainer.setBackground(GuiSkin.darkBackground());

		travelContainer.setFillParent(true);

		super.addActor(travelContainer);
	}

	public void travel() {
		travelProgressBar.start(() -> MadSand.switchScreen(Screens.Game));
	}
}
