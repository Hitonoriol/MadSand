package hitonoriol.madsand.gui.dialogs;

import java.util.function.Consumer;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;

public class InputDialog extends GameDialog {
	private static final float PAD = Gui.BTN_HEIGHT * 0.65f;
	private static final String DEF_TITLE = "Input";
	private TextField textField = new TextField("", Gui.skin);

	public InputDialog(String title, String prompt, Consumer<String> inputConsumer) {
		makeBordered();
		centerTitle();
		setTitle(title);
		if (prompt != null)
			add(prompt).height(Gui.FONT_S).padBottom(PAD).row();
		else
			skipLine();
		add(textField).size(250, Gui.BTN_HEIGHT).padBottom(PAD).row();
		TextButton okBtn = Widgets.button("Confirm");
		Gui.setAction(okBtn, () -> {
			String text = textField.getText().trim();
			if (text.length() == 0)
				return;
			inputConsumer.accept(text);
			remove();
		});
		Table btnTable = getButtonTable();
		btnTable.add(okBtn).padRight(5);
		btnTable.add(createCloseButton());
		add(btnTable);

		addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (keycode == Keys.ENTER)
					okBtn.toggle();
				return super.keyDown(event, keycode);
			}
		});
	}

	public InputDialog(String title, Consumer<String> inputConsumer) {
		this(title, null, inputConsumer);
	}

	public InputDialog(Consumer<String> inputConsumer) {
		this(DEF_TITLE, inputConsumer);
	}

	public InputDialog setInitialText(String text) {
		textField.setText(text);
		return this;
	}
}
