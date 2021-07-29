package hitonoriol.madsand.containers;

import java.util.HashMap;
import java.util.function.UnaryOperator;

public class HashMapFactory<K, V> {
	private HashMap<K, V> map;

	private HashMapFactory(HashMap<K, V> map) {
		this.map = map;
	}

	public HashMapFactory<K, V> put(K key, V value) {
		map.put(key, value);
		return this;
	}

	private HashMap<K, V> getMap() {
		return map;
	}

	public static <K, V> HashMap<K, V> create(UnaryOperator<HashMapFactory<K, V>> wrapper) {
		return wrapper.apply(new HashMapFactory<K, V>(new HashMap<>())).getMap();
	}
}
