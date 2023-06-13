package hitonoriol.madsand.containers;

import java.util.function.Consumer;

public class Storage<T> {
	private T value;

	public Storage(T value) {
		set(value);
	}

	public Storage() {
		this(null);
	}

	public T set(T value) {
		this.value = value;
		return value;
	}

	public T get() {
		return value;
	}

	public void ifPresent(Consumer<? super T> action) {
		if (value != null)
			action.accept(value);
	}

	public boolean isEmpty() {
		return value == null;
	}

	public void clear() {
		value = null;
	}

	public static <T> Storage<T> empty() {
		return new Storage<>();
	}
}
