package hitonoriol.madsand.screens;

import com.badlogic.gdx.Gdx;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gui.stages.DeathStage;
import hitonoriol.madsand.util.Utils;

public class DeathScreen extends AbstractScreen<DeathStage> {

	private WorldRenderer gameWorld;

	public DeathScreen(WorldRenderer gameWorld) {
		super(new DeathStage());
		this.gameWorld = gameWorld;
	}

	@Override
	public void show() {
		super.show();
		Gdx.graphics.setContinuousRendering(true);
		stage.setDeathMessage("You survived " + Utils.timeString(MadSand.player().getSurvivedTime()));
	}

	@Override
	public void render(float delta) {
		gameWorld.render(delta);
		super.render(delta);
	}
}
