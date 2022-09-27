package hitonoriol.madsand.screens;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.stages.Overlay;
import hitonoriol.madsand.input.Keyboard;
import hitonoriol.madsand.input.Mouse;

public class GameScreen extends AbstractScreen<Overlay> {

	private WorldRenderer gameWorld;

	public GameScreen(WorldRenderer gameWorld) {
		super(Gui.overlay = new Overlay());
		this.gameWorld = gameWorld;
	}

	@Override
	public void show() {
		super.show();
		gameWorld.setCamFollowPlayer(true);
		if (!Gui.isGameUnfocused())
			stage.showTooltip();
	}
	
	@Override
	public void hide() {
		getStage().getContextMenu().close();
		super.hide();
	}

	@Override
	public void render(float delta) {
		gameWorld.render(delta);
		Mouse.update();
		if (!Gui.isGameUnfocused()) {
			Mouse.pollMouseMovement();
			Keyboard.pollGameKeys();
		}
		super.render(delta);
	}
	
	@Override
	public void resize(int width, int height) {
		gameWorld.updateViewport();
		getStage().getViewport().update(width, height, true);
		getStage().updateWidgetPositions();
		MadSand.player().setFov();
		super.resize(width, height);
	}
}