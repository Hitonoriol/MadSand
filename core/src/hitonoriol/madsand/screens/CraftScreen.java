package hitonoriol.madsand.screens;

import hitonoriol.madsand.gui.stages.CraftStage;

public class CraftScreen extends AbstractScreen<CraftStage> {

	private WorldRenderer gameWorld;

	public CraftScreen(WorldRenderer gameWorld) {
		super(new CraftStage());
		this.gameWorld = gameWorld;
	}

	@Override
	public void render(float delta) {
		gameWorld.render(delta);
		super.render(delta);
	}
}
