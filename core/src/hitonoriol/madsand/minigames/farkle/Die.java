package hitonoriol.madsand.minigames.farkle;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.util.Utils;

public class Die implements Comparable<Die> {
	public static final int WIDTH = 41, HEIGHT = 50;
	private static TextureRegion[][] dice = TextureRegion.split(Resources.loadTexture("misc/dice.png"), WIDTH, HEIGHT);
	public final static int MIN_VALUE = 1, MAX_VALUE = 6;
	private int value;

	public Die() {
		roll();
	}

	public Die(int value) {
		setValue(value);
	}

	public void roll() {
		setValue(Utils.rand(MIN_VALUE, MAX_VALUE));
	}

	public void setValue(int value) {
		this.value = Math.min(MAX_VALUE, Math.max(MIN_VALUE, value));
	}

	public int getValue() {
		return value;
	}

	public TextureRegion getTexture() {
		return dice[0][getValue() - 1];
	}

	@Override
	public int compareTo(Die o) {
		return Integer.compare(value, o.value);
	}
}
