package hitonoriol.madsand.gui.stages;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

import hitonoriol.madsand.vfx.ShaderManager;

public class VFXStage extends Stage {
	private ShaderManager shaderManager;

	public VFXStage(Viewport viewport) {
		super(viewport);
		shaderManager = new ShaderManager(getBatch());
		shaderManager.setEnabledByDefault(true);
	}

	@Override
	public void draw() {
		Camera camera = getViewport().getCamera();
		camera.update();

		var root = getRoot();
		if (!root.isVisible())
			return;

		var batch = getBatch();
		batch.setProjectionMatrix(camera.combined);
		shaderManager.begin();
		root.draw(batch, 1);
		shaderManager.end();
	}
	
	public ShaderManager getShaderManager() {
		return shaderManager;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		shaderManager.dispose();
	}
}
