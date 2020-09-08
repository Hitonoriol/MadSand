package hitonoriol.madsand.entities.inventory;

import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.gui.AutoFocusScrollPane;

public class InventoryUI {
	AutoFocusScrollPane invScroll;
	Table invTable;
	Table invContainer;
	Label header;

	int stacks = 0;

	int ITEMS_PER_ROW = 5;
	int WIDTH = 400;
	int HEIGHT = 500;
	int OFFSET = 5;

	public InventoryUI() {
		setUpInventory();
	}

	void setUpInventory() {
		invContainer = new Table();
		invTable = new Table();
		invTable.row();
		// invTable.setDebug(true);
		
		invTable.setBackground(Gui.darkBackground);
		invTable.align(Align.topLeft);
		
		header = new Label("[0/0 kg]", Gui.skin);
		
		invContainer.add(header).pad(10).fillY().align(Align.center);
		invContainer.row();
		invContainer.add(invTable);
		
		invScroll = new AutoFocusScrollPane(invContainer);
		invScroll.setVisible(false);
		
		invTable.setWidth(WIDTH);
		invScroll.setHeight(HEIGHT);
		invScroll.setWidth(WIDTH + OFFSET);
		header.setFontScale(1.5f);
		invScroll.setOverscroll(false, false);
		invScroll.setScrollingDisabled(true, false);
		invScroll.setPosition(Gdx.graphics.getWidth() / 2 - WIDTH / 2, Gdx.graphics.getHeight() / 2 - HEIGHT / 2);
		Gui.overlay.addActor(invScroll);
		stacks = 0;
	}

	void remove() {
		invContainer.remove();
		invTable.remove();
		invScroll.remove();
	}

	void refresh(Set<Entry<Item, InventoryUICell>> set) {
		stacks = 0;
		invTable.clear();
		for (Entry<Item, InventoryUICell> pair : set) {
			putNewItem(pair.getValue());
		}
	}

	void putNewItem(Group cell) {
		++stacks;
		if (stacks % ITEMS_PER_ROW == 1)
			invTable.row();
		invTable.add(cell);
	}

	public void hide() {
		invScroll.setVisible(false);
	}

	public void toggleVisible() {
		invScroll.setVisible(!invScroll.isVisible());
	}

	boolean isVisible() {
		return invScroll.isVisible();
	}

	void setMass(double curWeight, double maxWeight) {
		setHeader("[" + Utils.round(curWeight) + "/" + maxWeight + "] kg");
	}

	void setHeader(String str) {
		header.setText(str);
	}
}
