package hitonoriol.madsand.input;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import hitonoriol.madsand.input.KeyBindManager.Keystroke;
import hitonoriol.madsand.util.Utils;

public class GameInputListener extends InputListener {
	private static final long KEYSTROKE_DELAY = 125;

	private KeyBindManager keyBinds = new KeyBindManager();
	private Set<Integer> heldKeys = new HashSet<>();
	private long firstKeystrokeKeyTime = 0;
	private Set<Integer> currentKeystroke = new HashSet<>();

	@Override
	public boolean keyUp(InputEvent event, int keycode) {
		heldKeys.remove(keycode);
		/* Execute an action bound to a single key */
		if (!keystrokeActive())
			keyBinds.runBoundAction(keycode);

		else if (keystrokeFinished()) {
			/* The keystroke has at least 2 keys & all keys have been released */
			if (keystrokeValid())
				keyBinds.runBoundAction(currentKeystroke);
			Utils.dbg(
				"Executed an action bound to: [%s]", currentKeystroke.stream()
					.map(Keys::toString).collect(Collectors.joining(", "))
			);
			consumeKeystroke();
		}
		return true;
	}

	@Override
	public boolean keyDown(InputEvent event, int keycode) {
		addToKeystroke(keycode);
		heldKeys.add(keycode);
		return super.keyDown(event, keycode);
	}

	public boolean keysHeld() {
		return !heldKeys.isEmpty();
	}

	public boolean isKeyPressed(int key) {
		return heldKeys.contains(key);
	}

	public void resetState() {
		heldKeys.clear();
	}

	private boolean keystrokeValid() {
		return currentKeystroke.size() > 1;
	}

	private boolean keystrokeActive() {
		return !currentKeystroke.isEmpty();
	}

	private boolean keystrokeFinished() {
		return (heldKeys.isEmpty() || keyBinds.getPolledKeys().containsAll(heldKeys));
	}

	private void consumeKeystroke() {
		currentKeystroke.clear();
	}

	private void addToKeystroke(int key) {
		/* Check if current keystroke expired */
		if (!currentKeystroke.isEmpty() && sinceFirstPressedKey() > KEYSTROKE_DELAY)
			currentKeystroke.clear();

		/* Ignore keys listened to by pollers */
		if (keyBinds.getPolledKeys().contains(key))
			return;

		currentKeystroke.removeIf(k -> !isKeyPressed(k));

		/* Begin a new keystroke */
		if (currentKeystroke.isEmpty()) {
			if (Keystroke.isModifier(key))
				firstKeystrokeKeyTime = System.currentTimeMillis();
			else
				return;
		}

		currentKeystroke.add(key);
	}

	private long sinceFirstPressedKey() {
		return System.currentTimeMillis() - firstKeystrokeKeyTime;
	}

	public KeyBindManager getKeyBindManager() {
		return keyBinds;
	}
}
