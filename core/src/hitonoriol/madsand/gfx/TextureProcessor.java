package hitonoriol.madsand.gfx;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
		var pixmap = getPixmap();
		var pixelColor = new Color();

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

	public static TextureData getTextureData(Texture texture) {
		var data = texture.getTextureData();

		if (!data.isPrepared())
			data.prepare();

		return data;
	}

	public static void loadPixmap(Texture texture, Pixmap pixmap) {
		texture.load(new PixmapTextureData(pixmap, null, false, false));
	}

	public static Texture copyTexture(Texture src) {
		var copy = new Texture(src.getWidth(), src.getHeight(), Pixmap.Format.RGBA8888);
		loadPixmap(copy, getTextureData(src).consumePixmap());
		return copy;
	}

	public static Texture createTexture(TextureRegion region) {
		return new Texture(extractPixmap(region));
	}

	public static Pixmap extractPixmap(TextureRegion textureRegion) {
		return extractPixmap(textureRegion, textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
	}

	/* Resize the longest side of region to maxSideSize, keeping the aspect ratio */
	public static Pixmap extractPixmap(TextureRegion textureRegion, int maxSideSize) {
		int width = textureRegion.getRegionWidth(), height = textureRegion.getRegionHeight();
		float ratio = (float) maxSideSize / Math.max(width, height);
		return extractPixmap(textureRegion, (int) (width * ratio), (int) (height * ratio));
	}

	public static Pixmap extractPixmap(TextureRegion textureRegion, int newWidth, int newHeight) {
		var textureData = getTextureData(textureRegion.getTexture());
		var srcMap = textureData.consumePixmap();
		var resizedMap = new Pixmap(
			newWidth,
			newHeight,
			textureData.getFormat()
		);
		resizedMap.drawPixmap(
			srcMap,
			textureRegion.getRegionX(), textureRegion.getRegionY(),
			textureRegion.getRegionWidth(), textureRegion.getRegionHeight(),
			0, 0,
			resizedMap.getWidth(), resizedMap.getHeight()
		);
		return resizedMap;
	}
}
