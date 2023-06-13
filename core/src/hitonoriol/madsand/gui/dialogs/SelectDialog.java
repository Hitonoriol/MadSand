package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;

public class SelectDialog extends GameDialog {
	private static float XPAD = 35;

	private Table entryTable = Widgets.table();

	public SelectDialog(String title, String description) {
		makeBordered();
		centerTitle();
		setTitle(title == null ? "Select an option" : title);
		entryTable.defaults().size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT).padBottom(5).padLeft(XPAD).padRight(XPAD);

		if (description != null)
			entryTable.add(description).row();

		add(new AutoFocusScrollPane(entryTable)).height(defaultHeight()).row();
		addCloseButton();
		pack();
	}

	public SelectDialog(String title) {
		this(title, null);
	}

	public SelectDialog() {
		this(null);
	}

	public SelectDialog addOption(String text, Runnable action) {
		Button entryBtn = Widgets.button(text);
		Gui.setAction(entryBtn, () -> {
			remove();
			action.run();
		});
		entryTable.add(entryBtn).row();
		return this;
	}
}
