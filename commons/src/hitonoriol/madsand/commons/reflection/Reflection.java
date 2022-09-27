package hitonoriol.madsand.commons.reflection;

import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;

public class Reflection {
	/* To be used only as a last resort */
	@SuppressWarnings("unchecked")
	public static <T> T readField(Object obj, String fieldName) {
		try {
			return (T) FieldUtils.readField(obj, fieldName, true);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends Map<K, V>, K, V> Class<T> mapClass(Class<? extends Map> type) {
		return (Class<T>) type;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getClass(T object) {
		return (Class<T>) object.getClass();
	}
}
