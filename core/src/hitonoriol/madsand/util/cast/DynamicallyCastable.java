package hitonoriol.madsand.util.cast;

import java.util.Optional;

public interface DynamicallyCastable<C> {
	default <T extends C> boolean is(Class<T> subtypeClass) {
		return subtypeClass.isInstance(this);
	}

	default <T extends C> Optional<T> as(Class<T> subtypeClass) {
		return Cast.to(this, subtypeClass);
	}
}
