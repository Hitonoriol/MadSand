package hitonoriol.madsand;

import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableFloat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;

public class TextureProcessor {

	private Texture texture;

	public TextureProcessor(Texture texture) {
		this.texture = texture;
	}

	public void foreachPixel(Consumer<Color> pixelAction) {
		Pixmap pixmap = getTextureData().consumePixmap();
		Color pixelColor = new Color();

		for (int x = 0; x < pixmap.getWidth(); x++)
			for (int y = 0; y < pixmap.getHeight(); y++) {
				pixelAction.accept(pixelColor.set(pixmap.getPixel(x, y)));
				pixmap.drawPixel(x, y, Color.rgba8888(pixelColor));
			}

		loadPixmap(pixmap);
	}

	public void invertColors() {
		Color white = new Color();
		MutableFloat alpha = new MutableFloat();
		foreachPixel(color -> {
			white.set(1, 1, 1, 1);
			alpha.setValue(color.a);
			color.set(white.sub(color));
			color.a = alpha.floatValue();
		});
	}

	private TextureData getTextureData() {
		return getTextureData(texture);
	}

	public void loadPixmap(Pixmap pixmap) {
		loadPixmap(texture, pixmap);
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
