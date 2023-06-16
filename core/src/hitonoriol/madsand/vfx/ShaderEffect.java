package hitonoriol.madsand.vfx;

import com.badlogic.gdx.Gdx;
import com.crashinvaders.vfx.VfxRenderContext;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.effects.ShaderVfxEffect;
import com.crashinvaders.vfx.framebuffer.VfxPingPongWrapper;
import com.crashinvaders.vfx.gl.VfxGLUtils;

public class ShaderEffect extends ShaderVfxEffect implements ChainVfxEffect {
	protected final static String U_TEXTURE0 = "u_texture0";

	public ShaderEffect(String fragShaderPath) {
		super(
			VfxGLUtils.compileShader(
				Gdx.files.classpath("gdxvfx/shaders/screenspace.vert"),
				Gdx.files.internal(fragShaderPath)
			)
		);
	}

	@Override
	public void rebind() {
		super.rebind();
		program.bind();
		program.setUniformi(U_TEXTURE0, TEXTURE_HANDLE0);
	}

	@Override
	public void render(VfxRenderContext context, VfxPingPongWrapper buffers) {
		buffers.getSrcBuffer().getTexture().bind(TEXTURE_HANDLE0);
		renderShader(context, buffers.getDstBuffer());
	}
}
