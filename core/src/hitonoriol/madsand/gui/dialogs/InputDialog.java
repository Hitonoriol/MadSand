package hitonoriol.madsand.gui.dialogs;

import java.util.function.Consumer;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameDialog;

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
		add(textField).size(250, Gui.BTN_HEIGHT).padBottom(PAD).row();
		TextButton okBtn = new TextButton("Confirm", Gui.skin);
		Gui.setAction(okBtn, () -> {
			inputConsumer.accept(textField.getText());
			remove();
		});
		Table btnTable = getButtonTable();
		btnTable.add(okBtn).padRight(5);
		btnTable.add(createCloseButton());
		add(btnTable);
	}

	public InputDialog(String title, Consumer<String> inputConsumer) {
		this(title, null, inputConsumer);
	}

	public InputDialog(Consumer<String> inputConsumer) {
		this(DEF_TITLE, inputConsumer);
	}
}
