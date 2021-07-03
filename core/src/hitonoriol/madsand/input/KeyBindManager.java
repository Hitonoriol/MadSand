package hitonoriol.madsand.input;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.util.Utils;

public class KeyBindManager {
	private Map<Integer, KeyBind> bindings = new LinkedHashMap<>();
	private List<Consumer<Integer>> listeners = new ArrayList<>(1);
	private static final KeyBind noAction = new KeyBind(() -> {});

	public void runBoundAction(int key) {
		Utils.tryTo(() -> {
			bindings.getOrDefault(key, noAction).run();
			if (!listeners.isEmpty() && !Gui.isGameUnfocused())
				listeners.forEach(listener -> listener.accept(key));
		});
	}

	public KeyBindManager bind(int key, boolean execAlways, Runnable action) {
		bindings.put(key, new KeyBind(action, execAlways));
		return this;
	}
	
	public KeyBindManager bind(int key, Runnable action) {
		return bind(key, false, action);
	}

	public KeyBindManager bind(Consumer<Integer> keyListener) {
		listeners.add(keyListener);
		return this;
	}

	public boolean unbind(int key) {
		return bindings.remove(key) != null;
	}

	public void bindAll(Map<Integer, Runnable> bindings) {
		bindings.forEach((key, action) -> bind(key, action));
	}

	public boolean bindingExists(int key) {
		return bindings.containsKey(key);
	}
}
