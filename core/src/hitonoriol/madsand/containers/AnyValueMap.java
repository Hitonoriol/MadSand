package hitonoriol.madsand.containers;

import java.util.HashMap;
import java.util.Map;

import hitonoriol.madsand.commons.reflection.Reflection;

public class AnyValueMap<K> {
	private Map<Class<?>, Map<K, Object>> values = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <V> V put(K key, V value) {
		var valueType = Reflection.getClass(value);
		var typedValues = values.get(valueType);
		if (typedValues == null)
			values.put(valueType, typedValues = new HashMap<>());
		return (V) typedValues.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public <V> V get(Class<V> valueType, K key) {
		return (V) values.get(valueType).get(key);
	}
}
