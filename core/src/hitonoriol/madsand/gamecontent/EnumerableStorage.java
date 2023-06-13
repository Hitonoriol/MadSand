package hitonoriol.madsand.gamecontent;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import hitonoriol.madsand.Enumerable;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public abstract class EnumerableStorage<K, T extends Enumerable> implements Loadable {
	private T defaultValue;
	private Map<K, T> content;

	protected EnumerableStorage(T defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void set(Map<K, T> content) {
		this.content = content;
	}

	public Map<K, T> get() {
		return content;
	}

	public T get(K id) {
		return content.getOrDefault(id, defaultValue);
	}

	protected void setDefaultValue(T value) {
		defaultValue = value;
	}

	public T defaultValue() {
		return defaultValue;
	}

	public String getName(K id) {
		return content.get(id).name();
	}
}
