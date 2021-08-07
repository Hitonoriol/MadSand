package hitonoriol.madsand.gui.widgets.overlay;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.input.Mouse;

public class GameLog extends ScrollablePanel {
	private static final String WARNING_COLOR = "[#ff9185]";
	public static final String NOTICE_ALT_COLOR = "[#58FFB1]";
	public static String NOTICE_COLOR = "[#16E1EA]";
	private static final int LOG_LENGTH = 20;
	

	boolean noticeColor = true; // flag to use alternating notice colors
	private TextField inputField = new TextField("", Gui.skin);
	private Label[] logLabels = new Label[LOG_LENGTH];

	public GameLog() {
		getContentTable().pad(3);
		for (int i = 0; i < LOG_LENGTH; ++i) {
			logLabels[i] = new Label(" ", Gui.skin);
			logLabels[i].setWrap(true);
			addContents(logLabels[i]).width(WIDTH).row();
		}

		inputField.setWidth(WIDTH);
		inputField.setMessageText("");
		inputField.setFocusTraversal(true);
		inputField.debug();
		inputField.setVisible(false);

		super.add(inputField).width(WIDTH).align(Align.left).padTop(3).height(30);
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
		addNotification();
	}

	public void notify(String msg) {
		if (printedLine.contains(NOTICE_COLOR) || printedLine.contains(NOTICE_ALT_COLOR))
			noticeColor = !noticeColor;
		else
			noticeColor = true;

		print("* " + msg, noticeColor ? NOTICE_COLOR : NOTICE_ALT_COLOR);
	}

	public void warn(String msg) {
		print(msg, WARNING_COLOR);
	}

	private void addNotification() {
		Mouse.getNotificator().notify(printedLine);
	}

	public String getLastPrintedLine() {
		return printedLine;
	}

	public Label[] getLabels() {
		return logLabels;
	}

	public TextField getConsoleField() {
		return inputField;
	}
}
