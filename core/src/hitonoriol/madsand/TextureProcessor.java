package hitonoriol.madsand;

import java.util.function.Consumer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;

public class TextureProcessor {

	private Texture texture;
	private TextureData data;

	public TextureProcessor(Texture texture) {
		this.texture = texture;
		data = getTextureData(texture);
	}

	private TextureData getTextureData(Texture texture) {
		TextureData data = texture.getTextureData();

		if (!data.isPrepared())
			data.prepare();

		return data;
	}

	public void foreachPixel(Consumer<Color> pixelAction) {
		Pixmap pixmap = data.consumePixmap();
		Color pixelColor = new Color();

		for (int x = 0; x < pixmap.getWidth(); x++)
			for (int y = 0; y < pixmap.getHeight(); y++) {
				pixelAction.accept(pixelColor.set(pixmap.getPixel(x, y)));
				pixmap.drawPixel(x, y, Color.rgba8888(pixelColor));
			}

		texture.load(data);
	}
}
