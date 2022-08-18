package hitonoriol.madsand.gui.animation;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import hitonoriol.madsand.resources.Resources;

public class Animations {
	private static final int DEFAULT_FRAME_SIZE = 32;
	public static final float ACTION_ANIM_DURATION = 0.15f;

	public static final TextureRegion[]
			attack = loadAnimationStrip("anim/hit"),
			objectHit = loadAnimationStrip("anim/obj_hit"),
			heal = loadAnimationStrip("anim/heal"),
			detect = loadAnimationStrip("anim/detect");

	private static TextureRegion[] loadAnimationStrip(String file) {
		return Animations.loadAnimationStrip(file, DEFAULT_FRAME_SIZE);
	}

	private static TextureRegion[] loadAnimationStrip(String file, int frameSize) { // load 1xN animation strip from file
		TextureRegion[][] animStrip = Resources.getTexture(file).split(frameSize, frameSize);
		return getAnimationStrip(animStrip, 0, animStrip[0].length);
	}

	private static TextureRegion[] getAnimationStrip(TextureRegion[][] region, int row, int frames) { // convert [][] strip to []
		TextureRegion[] strip = new TextureRegion[frames];

		for (int i = 0; i < frames; ++i)
			strip[i] = region[row][i];

		return strip;
	}
}
