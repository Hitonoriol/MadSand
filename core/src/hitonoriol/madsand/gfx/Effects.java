package hitonoriol.madsand.gfx;

import java.io.Serializable;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableFloat;

import com.badlogic.gdx.graphics.Color;

public class Effects {
	public static interface TextureEffect extends Consumer<TextureProcessor>, Serializable {
	}

	public static TextureEffect colorInversion = processor -> {
		Color white = new Color();
		MutableFloat alpha = new MutableFloat();
		processor.foreachPixel(color -> {
			white.set(1, 1, 1, 1);
			alpha.setValue(color.a);
			color.set(white.sub(color));
			color.a = alpha.floatValue();
		});
	};

	public static TextureEffect colorize(Color color) {
		return processor -> {
			processor.foreachPixel(pixel -> {
				pixel.mul(color);
			});
		};
	}
}
