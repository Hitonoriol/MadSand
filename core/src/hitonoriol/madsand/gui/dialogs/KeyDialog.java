package hitonoriol.madsand.gui.dialogs;

import java.util.function.Consumer;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;

public class KeyDialog extends GameDialog {

	private int key = 0;
	private Label keyLabel = Widgets.label("None");
	private Runnable removeBindingAction;

	public KeyDialog(Consumer<Integer> keyConsumer) {
		this(keyConsumer, -1);
	}

	public KeyDialog(Consumer<Integer> keyConsumer, int initKey) {
		super.setTitle("Set keybinding");
		super.centerTitle();
		super.skipLine();
		keyLabel.setAlignment(Align.center);
		super.add(Gui.setFontSize(keyLabel, Gui.FONT_XL)).size(200).row();

		addButton("Apply", () -> applySelectedKey(keyConsumer));
		addButton("Unbind", () -> sendKey(Keys.ESCAPE));
		addCloseButton();

		if (initKey > 0)
			keyLabel.setText(Keys.toString(initKey));

		addListener(new InputListener() {

			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				sendKey(keycode);
				return true;
			}
		});
	}

	private void sendKey(int keycode) {
		key = keycode;
		var keyText = keycode == Keys.ESCAPE ? "Remove binding" : Keys.toString(keycode);
		keyLabel.setText(keyText);
	}

	public KeyDialog setRemoveBindingAction(Runnable action) {
		removeBindingAction = action;
		return this;
	}

	private void applySelectedKey(Consumer<Integer> keyConsumer) {
		if (key == 0)
			return;

		if (key == Keys.ESCAPE && removeBindingAction != null)
			removeBindingAction.run();
		else
			keyConsumer.accept(key);
		remove();
	}

}
