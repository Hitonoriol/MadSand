package hitonoriol.madsand.util.cast;

import java.util.Optional;

public class Cast {
	@SuppressWarnings("unchecked")
	public static <P, T extends P> Optional<T> to(P value, Class<T> castTo) {
		if (castTo.isInstance(value))
			return Optional.of((T) value);

		return Optional.empty();
	}
}
