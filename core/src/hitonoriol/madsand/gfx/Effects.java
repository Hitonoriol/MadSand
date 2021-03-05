package hitonoriol.madsand.gfx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableFloat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class Effects {
	@JsonSerialize(using = LambdaJsonSerializer.class)
	@JsonDeserialize(using = LambdaJsonDeserializer.class)
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

	public static class LambdaJsonSerializer extends JsonSerializer<TextureEffect> {

		@Override
		public void serialize(TextureEffect value, JsonGenerator gen, SerializerProvider serializers) {
			try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream)) {

				outputStream.writeObject(value);
				gen.writeBinary(byteArrayOutputStream.toByteArray());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class LambdaJsonDeserializer extends JsonDeserializer<TextureEffect> {

		@Override
		public TextureEffect deserialize(JsonParser p, DeserializationContext ctxt) {
			try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(p.getBinaryValue());
					ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream)) {

				return (TextureEffect) inputStream.readObject();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

}
