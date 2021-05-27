package hitonoriol.madsand.entities.inventory;

import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.item.AbstractEquipment;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;

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

	private Inventory inventory;
	private HashMap<Item, InventoryUICell> itemUI = new HashMap<>();

	boolean dialogActive;
	int stacks = 0;

	public InventoryUI(Inventory inventory) {
		super(Gui.overlay);
		this.inventory = inventory;
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

	void refresh() {
		stacks = 0;
		invTable.clear();
		itemUI.forEach((item, itemUICell) -> putNewItem(itemUICell));
	}

	void putNewItem(InventoryUICell cell) {
		cell.refreshEquippedStatus();
		++stacks;
		if (stacks % ITEMS_PER_ROW == 1)
			invTable.row();
		invTable.add(cell);
	}

	void refreshRemoveItem(Item item) {
		if (item.id == 0)
			return;
		if (itemExists(item)) {
			inventory.refreshUITitle();
			Group rcell = itemUI.get(item);
			Cell<Group> cell = invTable.getCell(rcell);
			clearContextMenus();
			rcell.remove();
			// remove cell from table
			invTable.getCells().removeValue(cell, true);
			invTable.invalidate();
			// itemUI.get(item).cell.remove();
			itemUI.remove(item);
			stacks -= 1;

			refresh();
		}
	}

	public void refreshItem(Item item) {
		if (item.id == 0)
			return;
		inventory.refreshUITitle();
		if (itemUI.containsKey(item)) {
			itemUI.get(item).setText(item.quantity + "");
			if (item instanceof AbstractEquipment)
				itemUI.get(item).refreshHp();
			itemUI.get(item).refreshEquippedStatus();
		} else {
			InventoryUICell cell = new InventoryUICell(item);
			itemUI.put(item, cell);
			putNewItem(cell);
			refresh();
		}
	}

	public InventoryUICell getUICell(Item item) {
		return itemUI.get(item);
	}

	public void clearContextMenus() {
		if (itemUI == null)
			return;
		for (Entry<Item, InventoryUICell> pair : itemUI.entrySet()) {
			InventoryUICell cell = pair.getValue();
			if (cell.contextActive())
				cell.hideContext();
		}
	}

	/* Equip/Unequip for equippable & usable items */
	public void equipItem(Item item) {
		Player player = MadSand.player();
		InventoryUICell itemCell = getUICell(item);
		if (player.stats.equipment.itemEquipped(item)) {
			if (player.unEquip(item))
				itemCell.unEquipItem();
		} else {
			Item prev = player.stats.equipment.previousEquipment(item);
			if (player.unEquip(prev))
				getUICell(prev).unEquipItem();

			player.equip(item);
			refreshItem(prev);
			itemCell.equipItem();
		}
	}

	public void hide() {
		super.hide();
		clearContextMenus();
	}

	void setHeader(String str) {
		header.setText(str);
	}

	boolean itemExists(Item item) {
		return itemUI.containsKey(item);
	}
}
