package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;

public class GameLog extends Table {
	private static float INPUT_FIELD_WIDTH = 300;
	private static final int LOG_LENGTH = 20;
	
	Skin skin;
	public TextField inputField;
	public Label[] logLabels;

	public GameLog() {
		super();
		skin = Gui.skin;
		inputField = new TextField("", skin);
		inputField.setWidth(INPUT_FIELD_WIDTH);
		inputField.setMessageText("");
		inputField.setFocusTraversal(true);
		inputField.setTextFieldListener(new TextField.TextFieldListener() {
			public void keyTyped(TextField textField, char key) {
				if (key == Keys.ESCAPE || key == Keys.GRAVE) {
					inputField.setText("");
					Gui.overlay.unfocus(inputField);
				}

			}
		});

		// Setting up game log
		Table logtbl = new Table(skin).align(Align.topLeft);
		logtbl.setFillParent(true);
		int tpm = 0;
		logLabels = new Label[LOG_LENGTH];
		int cxxc = 0;
		while (cxxc < LOG_LENGTH) {
			logLabels[cxxc] = new Label(" ", skin);
			cxxc++;
		}
		while (tpm < LOG_LENGTH) {
			logLabels[tpm].setWrap(true);
			logtbl.add(logLabels[tpm]).width(INPUT_FIELD_WIDTH).pad(3);
			logtbl.row();
			tpm++;
		}
		logtbl.add(inputField).width(INPUT_FIELD_WIDTH).align(Align.left).pad(3).height(30);
		inputField.debug();
		inputField.setVisible(false);

	}
	
}
