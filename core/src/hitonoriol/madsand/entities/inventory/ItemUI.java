package hitonoriol.madsand.entities.inventory;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.inventory.item.AbstractEquipment;
import hitonoriol.madsand.entities.inventory.item.Item;

public class ItemUI extends Group {
	static float TOP_LABEL_YPADDING = 8;
	public static final int SIZE = 80;
	private ImageButton itemBtn; // button with item image

	private Label topLabel;
	protected Label itemQuantityLabel;
	private Label toolHpLabel;
	private Image highlight; // for mouseover highlighting of items

	protected Item item;

	protected ItemTooltip tooltip;

	public ItemUI(Item item) {
		super();
		this.item = item;
		topLabel = new Label("", Gui.skin);
		highlight = new Image(Resources.noEquip);
		toolHpLabel = new Label("", Gui.skin);
		itemBtn = new ImageButton(new SpriteDrawable(new Sprite(item.getTexture())));
		itemQuantityLabel = new Label(item.quantity + "", Gui.skin);
		tooltip = new ItemTooltip(item);

		refresh();

		toolHpLabel.setPosition(itemQuantityLabel.getX() + SIZE / 1.95f, itemQuantityLabel.getY() + 6);

		topLabel.setWidth(SIZE);
		topLabel.setAlignment(Align.center);
		topLabel.setPosition(SIZE, SIZE - TOP_LABEL_YPADDING, Align.topRight);

		super.addActor(itemBtn);
		super.addActor(itemQuantityLabel);
		super.addActor(toolHpLabel);
		super.addActor(topLabel);
		super.addActor(highlight);
		super.setSize(SIZE, SIZE);

		highlight.setVisible(false);

		if (!item.equals(Item.nullItem))
			super.addListener(tooltip);

		super.addListener(new InputListener() {
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				highlight.setVisible(true);
			}

			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				highlight.setVisible(false);
			}
		});
	}

	public Item getItem() {
		return item;
	}

	public ItemUI() {
		this(Item.nullItem);
	}

	public void setTopText(String str) {
		topLabel.setText(str);
	}

	void setText(String str) {
		itemQuantityLabel.setText(str);
	}

	private void setHp(int hp) {
		toolHpLabel.setText("[GREEN]" + hp + "%");
	}

	void refreshHp() {
		setHp((int) ((AbstractEquipment)item).getHpPercent());
	}

	public void refresh() {
		if (item.equals(Item.nullItem))
			setText("");
		else
			setText(item.quantity + "");

		if (item.isEquipment())
			refreshHp();

	}

	public static Table createItemList(Table itemTable, List<Item> items, int itemsPerRow) {
		int i = 1;
		for (Item item : items) {
			itemTable.add(new ItemUI(item)).size(ItemUI.SIZE);
			if (i % itemsPerRow == 0)
				itemTable.row();
			++i;
		}
		return itemTable;
	}

	public static Table createItemList(List<Item> items, int itemsPerRow) {
		return createItemList(new Table(Gui.skin), items, itemsPerRow);
	}

	public static Table createItemList(List<Item> items) {
		return createItemList(items, 3);
	}
}
