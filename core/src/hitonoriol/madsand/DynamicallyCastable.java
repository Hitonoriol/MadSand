package hitonoriol.madsand;

import java.util.Optional;

public interface DynamicallyCastable<C> {
	public default <T extends C> boolean is(Class<T> subtypeClass) {
		return subtypeClass.isInstance(this);
	}

	@SuppressWarnings("unchecked")
	public default <T extends C> Optional<T> as(Class<T> subtypeClass) {
		if (this.is(subtypeClass))
			return Optional.of((T) this);

		return Optional.empty();
	}
}
