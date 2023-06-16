package hitonoriol.madsand.vfx;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.effects.ShaderVfxEffect;

import hitonoriol.madsand.util.BooleanTally;

public class ShaderManager implements Disposable {
	private VfxManager vfxManager = new VfxManager(Format.RGBA8888);
	private Batch batch;
	private BooleanTally effectTally = new BooleanTally();
	private Map<String, ChainVfxEffect> effects = new HashMap<>();
	private boolean enabledByDefault = false;

	public ShaderManager(Batch batch) {
		this.batch = batch;
	}

	public void addEffect(String name, ChainVfxEffect effect, boolean enabled) {
		effect.setDisabled(!enabled);
		effects.put(name, effect);
		vfxManager.addEffect(effect);
	}
	
	public void addEffect(String name, ChainVfxEffect effect) {
		addEffect(name, effect, enabledByDefault);
	}
	
	public void addEffect(ChainVfxEffect effect, boolean enabled) {
		addEffect(effect.getClass().getSimpleName(), effect, enabled);
	}
	
	public void addEffect(ChainVfxEffect effect) {
		addEffect(effect, enabledByDefault);
	}
	
	public void setEnabledByDefault(boolean enabled) {
		enabledByDefault = enabled;
	}

	public void removeEffect(String name) {
		var effect = effects.remove(name);
		vfxManager.removeEffect(effect);
		effect.dispose();
	}
	
	public void removeEffect(ChainVfxEffect effect) {
		effects.values().removeIf(effect::equals);
		vfxManager.removeEffect(effect);
		effect.dispose();
	}

	public void setEffectEnabled(String name, boolean enabled) {
		effects.get(name).setDisabled(!enabled);
	}
	
	public void enableEffect(String name) {
		setEffectEnabled(name, true);
	}
	
	public void disableEffect(String name) {
		setEffectEnabled(name, false);
	}
	
	public boolean isEffectEnabled(String name) {
		return !effects.get(name).isDisabled();
	}
	
	public void setAllEffectsEnabled(boolean enabled) {
		effects.forEach((name, effect) -> effect.setDisabled(!enabled));
	}
	
	public void enableAll() {
		setAllEffectsEnabled(true);
	}
	
	public void disableAll() {
		setAllEffectsEnabled(false);
	}
	
	public ShaderVfxEffect getEffect(String name) {
		return (ShaderVfxEffect) effects.get(name);
	}
	
	public void setEnabled(boolean enabled) {
		vfxManager.setDisabled(!enabled);
	}

	public void disable() {
		setEnabled(false);
	}

	public void enable() {
		setEnabled(true);
	}

	public void clear() {
		effects.forEach((name, effect) -> {
			var shaderEffect = (ShaderVfxEffect) effect;
			shaderEffect.dispose();
		});
		effects.clear();
	}

	// Begin capturing input
	public void beginEffects() {
		effectTally.action();
		if (vfxManager.isCapturing())
			return;

		vfxManager.update(Gdx.graphics.getDeltaTime());
		vfxManager.cleanUpBuffers();
		vfxManager.beginInputCapture();
	}

	// End capturing input, apply effects and render the resulting frame
	public void endEffects() {
		if (!vfxManager.isCapturing() || !effectTally.reverseAction())
			return;

		vfxManager.endInputCapture();
		vfxManager.applyEffects();
		vfxManager.renderToScreen();
	}

	public void begin() {
		beginEffects();
		batch.begin();
	}

	public void end() {
		batch.end();
		endEffects();
	}

	@Override
	public void dispose() {
		vfxManager.dispose();
		clear();
	}
}
