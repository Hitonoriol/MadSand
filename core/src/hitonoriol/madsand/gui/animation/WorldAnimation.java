package hitonoriol.madsand.gui.animation;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class WorldAnimation extends SimpleAnimation {
	private float x, y;

	public WorldAnimation(TextureRegion[] strip) {
		super(strip);
	}

	public WorldAnimation setCoords(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public float x() {
		return x;
	}

	public float y() {
		return y;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof WorldAnimation))
			return false;

		if (obj == this)
			return true;

		var rhs = (WorldAnimation) obj;
		return x == rhs.x && y == rhs.y && getKeyFrames() == rhs.getKeyFrames();
	}
}
