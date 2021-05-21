package hitonoriol.madsand.input;

import hitonoriol.madsand.Gui;

public class KeyBind {
	Runnable action;
	boolean execAlways;

	public KeyBind(Runnable action, boolean execAlways) {
		this.action = action;
		this.execAlways = execAlways;
	}

	public KeyBind(Runnable action) {
		this(action, false);
	}

	public void run() {
		if (!execAlways && (Gui.isGameUnfocused() || Keyboard.inputIgnored()))
			return;

		action.run();
	}
}
