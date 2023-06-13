package hitonoriol.madsand.input;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Input.Keys;

import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.util.Utils;

public class KeyBindManager {
	private Map<Integer, KeyBind> keyBindings = new LinkedHashMap<>();
	private Map<Keystroke, KeyBind> keystrokeBindings = new LinkedHashMap<>();
	private Map<Integer, KeyBind> pollingBindings = new LinkedHashMap<>();

	public void runBoundAction(int key) {
		if (!keyBindings.containsKey(key))
			return;

		Utils.tryTo(keyBindings.get(key)::run);
	}

	public boolean runBoundAction(Set<Integer> keystroke) {
		if (!keystrokeBindings.containsKey(keystroke))
			return false;

		Utils.tryTo(keystrokeBindings.get(keystroke)::run);
		return true;
	}

	public void pollKeys() {
		if (Gui.isGameUnfocused())
			return;

		pollingBindings.forEach((key, bind) -> {
			if (Keyboard.isKeyPressed(key))
				bind.run();
		});
	}

	public KeyBindManager poll(int key, Runnable action) {
		pollingBindings.put(key, new KeyBind(action));
		return this;
	}

	public KeyBindManager bind(Runnable action, boolean execAlways, int key, int... modifiers) {
		var bind = new KeyBind(action, execAlways);
		if (modifiers.length == 0)
			keyBindings.put(key, bind);
		else
			keystrokeBindings.put(new Keystroke(key, modifiers), bind);
		return this;
	}

	public KeyBindManager bind(Runnable action, int key, int... modifiers) {
		return bind(action, false, key, modifiers);
	}

	public boolean unbind(int key) {
		return keyBindings.remove(key) != null;
	}

	public void bindAll(Map<Integer, Runnable> bindings) {
		bindings.forEach((key, action) -> bind(action, key));
	}

	public boolean bindingExists(Set<Integer> keystroke) {
		return keystrokeBindings.containsKey(keystroke);
	}

	public boolean bindingExists(int key) {
		return keyBindings.containsKey(key);
	}

	public Set<Integer> getPolledKeys() {
		return pollingBindings.keySet();
	}

	public void clear() {
		keystrokeBindings.clear();
		pollingBindings.clear();
	}

	public static class Keystroke extends HashSet<Integer> {
		private final static int MAX_KEYS = 4, MAX_MODIFIERS = MAX_KEYS - 1;

		public Keystroke(int key, int... modifiers) {
			super(MAX_KEYS);
			set(key, modifiers);
		}

		public void set(int key, int... modifiers) {
			if (!isEmpty())
				clear();

			add(key);
			if (modifiers.length > 0)
				for (int i = 0; i < modifiers.length && i < MAX_MODIFIERS; ++i)
					add(modifiers[i]);
		}

		public static Keystroke of(int key, int... modifiers) {
			return new Keystroke(key, modifiers);
		}

		public static boolean isModifier(int key) {
			switch (key) {
			case Keys.SHIFT_LEFT:
			case Keys.SHIFT_RIGHT:
			case Keys.ALT_LEFT:
			case Keys.ALT_RIGHT:
			case Keys.CONTROL_LEFT:
			case Keys.CONTROL_RIGHT:
				return true;
			default:
				return false;
			}
		}
	}
}
