package hitonoriol.madsand.containers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimationContainer extends Animation<TextureRegion> {

	public float elapsedTime = 0;

	public AnimationContainer(float frameDuration, TextureRegion[] strip) {
		super(frameDuration, strip);
	}

	public boolean isAnimationFinished() {
		return super.isAnimationFinished(elapsedTime);
	}

	public TextureRegion getCurrentKeyFrame() {
		elapsedTime += Gdx.graphics.getDeltaTime();
		return (TextureRegion) super.getKeyFrame(elapsedTime, false);
	}
}
