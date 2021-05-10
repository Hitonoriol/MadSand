package hitonoriol.madsand.input;

import java.util.LinkedHashMap;
import java.util.Map;

public class KeyBindManager {
	private Map<Integer, Runnable> bindings = new LinkedHashMap<>();
	private static final Runnable noAction = () -> {};

	public void runBoundAction(int key) {
		bindings.getOrDefault(key, noAction).run();
	}

	public KeyBindManager bind(int key, Runnable action) {
		bindings.put(key, action);
		return this;
	}

	public boolean unbind(int key) {
		return bindings.remove(key) != null;
	}

	public void bindAll(Map<Integer, Runnable> bindings) {
		this.bindings.putAll(bindings);
	}

	public boolean bindingExists(int key) {
		return bindings.containsKey(key);
	}
}
