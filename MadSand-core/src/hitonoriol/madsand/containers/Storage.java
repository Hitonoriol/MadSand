package hitonoriol.madsand.containers;

public class Storage<T> {
	private T value;

	public Storage(T value) {
		set(value);
	}

	public Storage() {
		this(null);
	}

	public void set(T value) {
		this.value = value;
	}

	public T get() {
		return value;
	}
}
