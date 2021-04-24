package hitonoriol.madsand.gui.dialogs;

import java.util.function.Consumer;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.util.Functional;

public class KeyDialog extends GameDialog {

	private int key = 0;
	private Label keyLabel = new Label("None", Gui.skin);

	public KeyDialog(Consumer<Integer> keyConsumer) {
		super();
		super.setTitle("Set keybinding");
		super.centerTitle();
		super.skipLine();
		keyLabel.setAlignment(Align.center);
		super.add(Gui.setFontSize(keyLabel, Gui.FONT_XL)).size(200).row();

		super.addButton(Functional.with(new TextButton("Apply", Gui.skin),
				button -> Gui.setAction(button, () -> applySelectedKey(keyConsumer))))
						.size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT);
		super.addCloseButton();

		super.addListener(new InputListener() {

			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				key = keycode;
				String keyText = keycode == Keys.ESCAPE ? "Remove binding" : Keys.toString(keycode);
				keyLabel.setText(keyText);
				return super.keyUp(event, keycode);
			}

		});
	}

	private void applySelectedKey(Consumer<Integer> keyConsumer) {
		if (key == 0)
			return;

		keyConsumer.accept(key);
		remove();
	}

}
