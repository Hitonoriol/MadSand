package hitonoriol.madsand.gui.dialogs;

import java.util.function.Consumer;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.util.Functional;

public class KeyDialog extends GameDialog {

	private int key;
	private Label keyLabel = new Label("None", Gui.skin);

	public KeyDialog(Consumer<Integer> keyConsumer) {
		super();
		super.setTitle("Set keybinding");
		super.centerTitle();
		super.skipLine();
		super.add(Gui.setFontSize(keyLabel, Gui.FONT_M)).row();
		super.addButton(Functional.with(new TextButton("Apply", Gui.skin),
				button -> Gui.setAction(button, () -> keyConsumer.accept(key))));
		super.addCloseButton();
		
		super.addListener(new InputListener() {

			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				key = keycode;
				keyLabel.setText(Keys.toString(keycode));
				return super.keyUp(event, keycode);
			}
			
		});
	}

}
