package hitonoriol.madsand.containers;

public interface Container<T> {
	boolean add(T value);
	boolean remove(T value);
}
