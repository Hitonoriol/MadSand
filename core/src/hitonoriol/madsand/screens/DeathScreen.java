package hitonoriol.madsand.screens;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gui.stages.DeathStage;
import hitonoriol.madsand.util.Utils;

public class DeathScreen extends AbstractScreen<DeathStage> {

	private GameWorldRenderer gameWorld;

	public DeathScreen(GameWorldRenderer gameWorld) {
		super(new DeathStage());
		this.gameWorld = gameWorld;
	}

	@Override
	public void show() {
		super.show();
		stage.setDeathMessage("You survived " + Utils.timeString(MadSand.player().getSurvivedTime()));
	}

	@Override
	public void render(float delta) {
		gameWorld.render(delta);
		super.render(delta);
	}
}
