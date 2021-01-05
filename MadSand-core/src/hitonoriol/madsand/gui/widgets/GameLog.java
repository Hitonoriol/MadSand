package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.gui.OverlayMouseoverListener;

public class GameLog extends Table {
	final float WIDTH = 335;
	public final static float HEIGHT = 240;
	
	private static final int LOG_LENGTH = 20;

	public static final String WARNING_COLOR = "[#ff9185]";
	public static final String NOTICE_ALT_COLOR = "[#58FFB1]";
	public static String NOTICE_COLOR = "[#16E1EA]";

	boolean noticeColor = true; // flag to use alternating notice colors

	Skin skin;
	public TextField inputField;
	public Label[] logLabels;
	public Table logTable = new Table();
	public AutoFocusScrollPane scroll;

	public GameLog() {
		super();
		skin = Gui.skin;

		inputField = new TextField("", skin);
		inputField.setWidth(WIDTH);
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
		logTable.align(Align.topLeft);
		int tpm = 0;
		logLabels = new Label[LOG_LENGTH];
		int cxxc = 0;
		while (cxxc < LOG_LENGTH) {
			logLabels[cxxc] = new Label(" ", skin);
			cxxc++;
		}
		while (tpm < LOG_LENGTH) {
			logLabels[tpm].setWrap(true);
			logTable.add(logLabels[tpm]).width(WIDTH).pad(3);
			logTable.row();
			tpm++;
		}
		logTable.setBackground(Resources.loadNinePatch("misc/darkness75.png"));
		inputField.debug();
		inputField.setVisible(false);

		scroll = new AutoFocusScrollPane(logTable);
		super.add(scroll).row();
		super.add(inputField).width(WIDTH).align(Align.left).pad(3).height(30);
		super.addListener(new OverlayMouseoverListener());
	}

	public void clear() {
		for (Label label : logLabels)
			label.setText("");
	}

	private int lineRepeat = 1;
	private int lineNum;
	private String printedLine = "";

	public void print(String arg) {
		if (!printedLine.equals(arg)) {
			lineRepeat = 1;
			printedLine = arg;
			int i = logLabels.length - 1;
			while (i >= 0) {

				if (i != 0)
					logLabels[i].setText(logLabels[i - 1].getText());
				else {
					logLabels[i].setText(arg);
					lineNum = i;
				}

				i--;
			}
		} else
			logLabels[lineNum].setText(printedLine + " x" + (++lineRepeat));
	}

	public void print(String msg, String color) {
		print(color + msg + "[]");
	}

	public void notice(String msg) {
		if (printedLine.contains(NOTICE_COLOR) || printedLine.contains(NOTICE_ALT_COLOR))
			noticeColor = !noticeColor;
		else
			noticeColor = true;
		
		print("* " + msg, noticeColor ? NOTICE_COLOR : NOTICE_ALT_COLOR);
	}

	public void warn(String msg) {
		print(msg, WARNING_COLOR);
	}

	public String getLastPrintedLine() {
		return printedLine;
	}

}
