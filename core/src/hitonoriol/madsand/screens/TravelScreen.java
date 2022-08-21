package hitonoriol.madsand.screens;

import hitonoriol.madsand.gui.stages.TravelStage;

public class TravelScreen extends AbstractScreen<TravelStage> {
	public TravelScreen() {
		super(new TravelStage());
	}

	@Override
	public void show() {
		super.show();
		getStage().travel();
	}
}
