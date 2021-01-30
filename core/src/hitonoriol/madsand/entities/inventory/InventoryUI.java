package hitonoriol.madsand.entities.inventory;

import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.world.World;

public class InventoryUI extends GameDialog {

	int ITEMS_PER_ROW = 5;
	int WIDTH = 400;
	int HEIGHT = 500;
	int OFFSET = 5;

	float BUTTON_PADDING = 5;
	float BUTTON_WIDTH = 250;
	float BUTTON_HEIGHT = 50;

	Table invContainer;
	Table invTable;
	AutoFocusScrollPane invScroll;
	Table invScrollTable;
	Label header;
	public TextButton craftMenuButton;

	boolean dialogActive;
	int stacks = 0;

	public InventoryUI() {
		super(Gui.overlay);
		setUpInventory();
	}

	void setUpInventory() {
		invContainer = new Table();
		invScrollTable = new Table();
		invTable = new Table();
		craftMenuButton = new TextButton("Crafting", Gui.skin);
		invTable.row();

		header = new Label("[0/0 kg]", Gui.skin);
		Gui.setFontSize(header, Gui.FONT_L);

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
		invContainer.add(invScroll).size(WIDTH, HEIGHT).row();
		invContainer.add(craftMenuButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).align(Align.center).pad(BUTTON_PADDING)
				.row();

		super.add(invContainer)
				.width(WIDTH + OFFSET)
				.height(HEIGHT + header.getHeight() + BUTTON_HEIGHT + BUTTON_PADDING * 3);
		super.setBackground(Gui.transparency);

		stacks = 0;

		craftMenuButton.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				Gui.openCraftMenu(0);
			}
		});
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
		super.remove();
		Gui.inventoryActive = false;
		World.player.inventory.clearContextMenus();
	}

	public void show() {
		super.show();
		Gui.inventoryActive = true;
		dialogActive = false;
	}

	void setHeader(String str) {
		header.setText(str);
	}
}
