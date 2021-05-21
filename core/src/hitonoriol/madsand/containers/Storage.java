package hitonoriol.madsand.containers;

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
}
