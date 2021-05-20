package hitonoriol.madsand.input;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import hitonoriol.madsand.util.Utils;

public class KeyBindManager {
	private Map<Integer, Runnable> bindings = new LinkedHashMap<>();
	private List<Consumer<Integer>> listeners = new ArrayList<>(1);
	private static final Runnable noAction = () -> {};

	public void runBoundAction(int key) {
		Utils.tryTo(() -> {
			bindings.getOrDefault(key, noAction).run();
			if (!listeners.isEmpty())
				listeners.forEach(listener -> listener.accept(key));
		});
	}

	public KeyBindManager bind(int key, Runnable action) {
		bindings.put(key, action);
		return this;
	}

	public KeyBindManager bind(Consumer<Integer> keyListener) {
		listeners.add(keyListener);
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
