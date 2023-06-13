package hitonoriol.madsand.gui.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SimpleAnimation extends Animation<TextureRegion> {
	private float elapsedTime = 0;

	public SimpleAnimation(float frameDuration, TextureRegion[] strip) {
		super(frameDuration, strip);
	}

	public SimpleAnimation(TextureRegion[] strip) {
		this(Animations.ACTION_ANIM_DURATION, strip);
	}

	public boolean isAnimationFinished() {
		return super.isAnimationFinished(elapsedTime);
	}

	public TextureRegion getCurrentKeyFrame(boolean looping) {
		elapsedTime += Gdx.graphics.getDeltaTime();
		return super.getKeyFrame(elapsedTime, looping);
	}

	public TextureRegion getCurrentKeyFrame() {
		return getCurrentKeyFrame(false);
	}

	public float getElapsedTime() {
		return elapsedTime;
	}
}
