package hitonoriol.madsand.entities.inventory;

import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;

public class InventoryUI {

	Table invContainer;
	Table invTable;
	AutoFocusScrollPane invScroll;
	Table invScrollTable;
	Label header;
	public TextButton craftMenuButton;

	int stacks = 0;

	int ITEMS_PER_ROW = 5;
	int WIDTH = 400;
	int HEIGHT = 500;
	int OFFSET = 5;

	float BUTTON_PADDING = 5;
	float BUTTON_WIDTH = 250;
	float BUTTON_HEIGHT = 50;

	public InventoryUI() {
		setUpInventory();
	}

	void setUpInventory() {
		invContainer = new Table();
		invScrollTable = new Table();
		invTable = new Table();
		craftMenuButton = new TextButton("Crafting", Gui.skin);
		invTable.row();

		header = new Label("[0/0 kg]", Gui.skin);
		header.setFontScale(1.5f);

		invTable.setBackground(Gui.darkBackground);
		invTable.align(Align.topLeft);
		invTable.setWidth(WIDTH);
		invScrollTable.add(invTable);

		invScroll = new AutoFocusScrollPane(invScrollTable);
		invScroll.setHeight(HEIGHT);
		invScroll.setWidth(WIDTH + OFFSET);
		invScroll.setOverscroll(false, false);
		invScroll.setScrollingDisabled(true, false);

		invContainer.add(header).pad(10).fillY().align(Align.center).row();
		invContainer.setSize(WIDTH + OFFSET, HEIGHT);
		invContainer.setPosition(Gdx.graphics.getWidth() / 2 - WIDTH / 2, Gdx.graphics.getHeight() / 2 - HEIGHT / 2);
		invContainer.add(invScroll).size(WIDTH, HEIGHT).row();
		invContainer.add(craftMenuButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).align(Align.center).pad(BUTTON_PADDING)
				.row();
		invContainer.setVisible(false);

		Gui.overlay.addActor(invContainer);
		stacks = 0;

		craftMenuButton.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				Gui.openCraftMenu(0);
			}
		});
	}

	void remove() {
		invContainer.remove();
	}

	void refresh(Set<Entry<Item, InventoryUICell>> set) {
		stacks = 0;
		invTable.clear();
		for (Entry<Item, InventoryUICell> pair : set) {
			putNewItem(pair.getValue());
		}
	}

	void putNewItem(InventoryUICell cell) {
		cell.refreshEquippedStatus();
		++stacks;
		if (stacks % ITEMS_PER_ROW == 1)
			invTable.row();
		invTable.add(cell);
	}

	public void hide() {
		invContainer.setVisible(false);
	}

	public void toggleVisible() {
		invContainer.setVisible(!invContainer.isVisible());
	}

	boolean isVisible() {
		return invContainer.isVisible();
	}

	void setMass(double curWeight, double maxWeight) {
		setHeader("[" + Utils.round(curWeight) + "/" + maxWeight + "] kg");
	}

	void setHeader(String str) {
		header.setText(str);
	}
}
