package hitonoriol.madsand.gui.widgets.overlay;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.MouseoverListener;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.resources.Resources;

public class ScrollablePanel extends Table {
	public static final float WIDTH = 335, HEIGHT = 240;
	private final static float ENTRY_PAD = 6, HEADER_BPAD = -Gui.FONT_S / 2, HEADER_TPAD = -HEADER_BPAD * 2.5f;

	private Table contentTable = Widgets.table();
	private AutoFocusScrollPane scroll = new AutoFocusScrollPane(contentTable);

	public ScrollablePanel() {
		contentTable.align(Align.topLeft);
		contentTable.setBackground(Gui.setMinSize(Resources.loadNinePatch("misc/darkness75"), 0));
		contentTable.defaults().align(Align.left).padBottom(ENTRY_PAD).width(WIDTH);
		add(scroll).size(WIDTH, HEIGHT).row();
		MouseoverListener.setUp(scroll);
	}

	public Cell<Label> addHeader(String text) {
		Label header = Widgets.label(text);
		Gui.setFontSize(header, Gui.FONT_M);
		Cell<Label> cell = addContents(header).padTop(HEADER_TPAD);
		cell.row();
		Gui.skipLine(contentTable).padBottom(HEADER_BPAD);
		return cell;
	}

	public <T extends Actor> Cell<T> addContents(T actor) {
		return contentTable.add(actor);
	}

	protected Table getContentTable() {
		return contentTable;
	}

	protected AutoFocusScrollPane getScroll() {
		return scroll;
	}
}
