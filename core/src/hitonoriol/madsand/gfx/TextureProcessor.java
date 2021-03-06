package hitonoriol.madsand.gfx;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;

import hitonoriol.madsand.gfx.Effects.TextureEffect;

public class TextureProcessor {

	private Texture texture;
	private Pixmap pixmap;
	private ArrayList<TextureEffect> effects;
	private boolean effectsApplied = false;

	public TextureProcessor(Texture texture) {
		this.texture = texture;
	}

	public TextureProcessor foreachPixel(Consumer<Color> pixelAction) {
		Pixmap pixmap = getPixmap();
		Color pixelColor = new Color();

		for (int x = 0; x < pixmap.getWidth(); x++)
			for (int y = 0; y < pixmap.getHeight(); y++) {
				if (pixelColor.set(pixmap.getPixel(x, y)).a == 0)
					continue;

				pixelAction.accept(pixelColor);
				pixmap.drawPixel(x, y, Color.rgba8888(pixelColor));
			}
		return this;
	}

	public TextureProcessor addEffect(TextureEffect effect) {
		if (noEffects())
			effects = new ArrayList<>(1);

		effects.add(effect);
		return this;
	}

	public TextureProcessor addEffects(ArrayList<TextureEffect> effects) {
		this.effects = effects;
		return this;
	}

	public void applyEffects() {
		effectsApplied = true;
		if (noEffects())
			return;

		effects.stream().forEach(effect -> effect.accept(this));
		loadPixmap(pixmap);
	}

	public boolean noEffects() {
		return effects == null;
	}

	public boolean isDone() {
		return effectsApplied;
	}

	public ArrayList<TextureEffect> getEffects() {
		return effects;
	}

	private TextureData getTextureData() {
		return getTextureData(texture);
	}

	public void loadPixmap(Pixmap pixmap) {
		loadPixmap(texture, pixmap);
	}

	public Pixmap getPixmap() {
		if (pixmap == null)
			pixmap = getTextureData().consumePixmap();
		return pixmap;
	}

	public static void loadPixmap(Texture texture, Pixmap pixmap) {
		texture.load(new PixmapTextureData(pixmap, null, false, false));
	}

	public static Texture copyTexture(Texture src) {
		Texture copy = new Texture(src.getWidth(), src.getHeight(), Pixmap.Format.RGBA8888);
		loadPixmap(copy, getTextureData(src).consumePixmap());
		return copy;
	}

	public static TextureData getTextureData(Texture texture) {
		TextureData data = texture.getTextureData();

		if (!data.isPrepared())
			data.prepare();

		return data;
	}
}
