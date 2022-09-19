package hitonoriol.madsand.commons.reflection;

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
}
