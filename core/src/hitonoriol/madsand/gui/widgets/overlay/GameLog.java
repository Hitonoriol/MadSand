package hitonoriol.madsand.gui.widgets.overlay;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiColors;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.input.Mouse;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.world.World;

public class GameLog extends ScrollablePanel {
	private final static int LOG_LENGTH = 30;
	private final static float ENTRY_PAD = 3f;
	
	private Color noticeColor = GuiColors.NOTICE;
	private final static String NOTICE_TAG = GuiColors.getTag(GuiColors.NOTICE),
			NOTICE_ALT_TAG = GuiColors.getTag(GuiColors.NOTICE_ALT);
	private Label[] logLabels = new Label[LOG_LENGTH];
	private int lineRepeat = 1;
	private int lineNum;
	private String printedLine = "";

	private static final int CMD_BUF_CAP = 10;
	private int switchingIdx = -1;
	private List<String> prevConsoleInputs = new ArrayList<>();
	private TextField console = new TextField("", Gui.skin);

	public GameLog() {
		getContentTable().pad(3);
		for (int i = 0; i < LOG_LENGTH; ++i) {
			logLabels[i] = Widgets.label(" ");
			logLabels[i].setWrap(true);
			addContents(logLabels[i]).width(WIDTH).padTop(ENTRY_PAD).padBottom(ENTRY_PAD).row();
		}

		console.setWidth(WIDTH);
		console.setMessageText("");
		console.setFocusTraversal(true);
		console.debug();
		console.setVisible(false);
		super.add(console).width(WIDTH).align(Align.left).padTop(3).height(30);
		initConsoleListener();
	}

	private void switchConsoleInput(boolean previous) {
		if (prevConsoleInputs.isEmpty())
			return;

		String tmp = console.getText();
		if (!switchingInputs())
			startSwitching(tmp);

		switchingIdx = Math.floorMod(switchingIdx + (previous ? -1 : 1), prevConsoleInputs.size());
		console.setText(prevConsoleInputs.get(switchingIdx));
		console.setCursorPosition(console.getText().length());
	}

	private void initConsoleListener() {
		console.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				event.cancel();
				return true;
			}

			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if (keycode == Keys.GRAVE) {
					toggleConsole();
					return true;
				}

				else if (keycode == Keys.ENTER) {
					String cmd = console.getText().trim();
					try {
						Lua.execute(cmd);
					} catch (Exception e) {
						MadSand.print("Couldn't execute user input");
						e.printStackTrace();
					} finally {
						saveInput();
						console.setText("");
						toggleConsole();
						if (prevConsoleInputs.size() > CMD_BUF_CAP)
							prevConsoleInputs.remove(0);
					}
					return true;
				}

				else if (keycode == Keys.UP || keycode == Keys.DOWN) {
					switchConsoleInput(keycode == Keys.UP);
					return true;
				}

				event.cancel();
				return true;
			}
		});
	}

	private void startSwitching(String currentInput) {
		switchingIdx = prevConsoleInputs.size();
		prevConsoleInputs.add(currentInput);
	}

	private boolean switchingInputs() {
		return switchingIdx >= 0;
	}

	private void saveInput() {
		if (switchingInputs())
			prevConsoleInputs.set(prevConsoleInputs.size() - 1, console.getText());
		else
			prevConsoleInputs.add(console.getText());
	}

	public void toggleConsole() {
		Stage stage = MadSand.getStage();
		if (stage.getKeyboardFocus() != console) {
			Gui.unfocusGame();
			console.setVisible(true);
			stage.setKeyboardFocus(console);
		} else {
			Gui.resumeGameFocus();
			console.setVisible(false);
			stage.unfocus(console);
			switchingIdx = -1;
		}
	}

	public void clear() {
		for (Label label : logLabels)
			label.setText("");
	}

	private String getWorldTimeString() {
		World world = MadSand.world();
		return String.format("[LIGHT_GRAY][%02d:%02d][] ", world.getWorldTimeHour(), world.getWorldTimeMinute()); 
	}
	
	private void setLogText(int idx, String text, boolean specialMsg) {
		logLabels[idx].setText((specialMsg ? "" : getWorldTimeString()) + text);
	}
	
	public void print(String arg) {
		print(arg, false);
	}
	
	public void print(String arg, boolean specialMsg) {
		if (!printedLine.equals(arg)) {
			lineRepeat = 1;
			printedLine = arg;
			int i = logLabels.length - 1;
			while (i >= 0) {
				if (i != 0)
					logLabels[i].setText(logLabels[i - 1].getText());
				else {
					setLogText(i, arg, specialMsg);
					lineNum = i;
				}
				i--;
			}
		} else
			setLogText(lineNum, printedLine + " x" + (++lineRepeat), specialMsg);
	}

	public void print(String msg, String color) {
		print(color + msg + "[]", true);
		addNotification();
	}

	public void print(String msg, Color color) {
		print(msg, GuiColors.getTag(color));
	}

	public void notify(String msg) {
		if (printedLine.contains(NOTICE_TAG) || msg.contains(NOTICE_ALT_TAG))
			noticeColor = noticeColor == GuiColors.NOTICE ? GuiColors.NOTICE_ALT : GuiColors.NOTICE;
		else
			noticeColor = GuiColors.NOTICE;
		print("* " + msg, noticeColor);
	}

	public void warn(String msg) {
		print(msg, GuiColors.WARNING);
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
		return console;
	}
}
