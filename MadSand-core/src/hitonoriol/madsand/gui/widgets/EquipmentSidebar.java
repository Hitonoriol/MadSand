package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.entities.inventory.ItemUI;
import hitonoriol.madsand.enums.EquipSlot;
import hitonoriol.madsand.gui.OverlayMouseoverListener;
import hitonoriol.madsand.world.World;

public class EquipmentSidebar extends Table {
	public static final int EQ_SLOTS = 6;

	static float ITEM_SIZE = 80;

	private static NinePatchDrawable tableBorder;
	ItemUI[] equip;

	public EquipmentSidebar() {
		super();
		super.addListener(new OverlayMouseoverListener());
		if (tableBorder == null) {
			tableBorder = Resources.loadNinePatch("misc/sidebar_border.png");
			tableBorder.setMinWidth(0);
			tableBorder.setMinHeight(0);
		}
		super.setBackground(tableBorder);

		super.setOrigin(Align.bottomRight);
		equip = new ItemUI[EQ_SLOTS];
		for (int i = 0; i < EQ_SLOTS; ++i) {
			equip[i] = new ItemUI();
			this.add(equip[i]).width(ITEM_SIZE).height(ITEM_SIZE).row();
		}

		super.setSize(ITEM_SIZE, ITEM_SIZE * EQ_SLOTS);
		super.pack();
	}

	public void init() {
		for (EquipSlot slot : EquipSlot.values())
			equipItem(slot, Item.nullItem);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setSlot(EquipSlot slot, ItemUI item) {
		item.setTopText(slot.text);
		
		equip[slot.number] = item;
		Array<Cell> cells = super.getCells();
		Cell<ItemUI> cell = cells.get(slot.number);
		cell.setActor(equip[slot.number]);
	}

	private ItemUI getItemDisplay(Item item) {
		return new ItemUI(item);
	}

	public void refreshSlot(EquipSlot slot) {
		equip[slot.number].refresh();
	}
	
	public void equipItem(EquipSlot slot, Item item) {
		ItemUI itemDisplay = getItemDisplay(item);

		switch (slot) {
		case MainHand:
			itemDisplay.addListener(new ClickListener(Buttons.LEFT) {
				public void clicked(InputEvent event, float x, float y) {
					World.player.useItem(item);
				}
			});
			break;

		default:
			break;
		}

		setSlot(slot, itemDisplay);
	}
}
