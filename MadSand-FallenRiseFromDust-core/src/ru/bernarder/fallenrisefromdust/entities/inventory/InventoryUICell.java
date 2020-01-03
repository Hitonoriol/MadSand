package ru.bernarder.fallenrisefromdust.entities.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Tooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import ru.bernarder.fallenrisefromdust.Gui;
import ru.bernarder.fallenrisefromdust.MadSand;
import ru.bernarder.fallenrisefromdust.Mouse;
import ru.bernarder.fallenrisefromdust.Resources;
import ru.bernarder.fallenrisefromdust.Utils;
import ru.bernarder.fallenrisefromdust.enums.ItemType;
import ru.bernarder.fallenrisefromdust.world.World;

public class InventoryUICell {
	private final int size = 80;

	private static final float CONTEXT_BTN_WIDTH = 100F;
	private static final float CONTEXT_BTN_HEIGHT = 30F;
	public static final float CONTEXT_W_DENOMINATOR = 1.75f;

	private float TOOLTIP_WIDTH = 200F;
	private float TOOLTIP_HEIGHT = 50F;

	private Table tooltipTbl;
	private Label itemInfoLbl;
	private Tooltip<Table> tooltip;

	private ImageButton itemBtn; // button with item image

	private Label itemQuantityLabel;
	private Label toolHpLabel;

	private Table invCellContextContainer; // RMB context menu container and buttons

	private TextButton dropBtn;
	private TextButton useBtn;
	private TextButton equipBtn;

	private Image highlight; // for mouseover highlighting of items

	Group cell;

	public InventoryUICell(Item item) {
		highlight = new Image(Resources.noEquip);
		toolHpLabel = new Label("", Gui.skin);
		itemBtn = new ImageButton(new SpriteDrawable(new Sprite(Resources.item[item.id])));
		itemQuantityLabel = new Label(item.quantity + "", Gui.skin);

		if (item.type.isTool())
			setHp(item.hp);

		toolHpLabel.setPosition(itemQuantityLabel.getX() + size / 1.6f, itemQuantityLabel.getY() + 6);

		cell = new Group();
		cell.addActor(itemBtn);
		cell.addActor(itemQuantityLabel);
		cell.addActor(toolHpLabel);
		cell.addActor(highlight);
		cell.setSize(size, size);

		highlight.setVisible(false);

		dropBtn = new TextButton("Drop", Gui.skin);

		invCellContextContainer = new Table(Gui.skin);
		addContextBtn(dropBtn);
		invCellContextContainer.setVisible(false);
		Gui.overlay.addActor(invCellContextContainer);

		dropBtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				hideContext();
				World.player.dropItem(item.id, item.quantity);
			}

		});

		if (item.type == ItemType.Consumable || item.type == ItemType.PlaceableObject
				|| item.type == ItemType.PlaceableTile) {
			useBtn = new TextButton("Use", Gui.skin);
			useBtn.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					World.player.useItem(item);
					hideContext();
				}
			});
			addContextBtn(useBtn);
		}

		if (item.type.isArmor()) {
			equipBtn = new TextButton("Equip", Gui.skin);
			equipBtn.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					World.player.equip(item);
					World.player.freeHands(true);
					hideContext();
				}
			});
			addContextBtn(equipBtn);
		}

		cell.addListener(new InputListener() {
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				highlight.setVisible(true);
			}

			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				highlight.setVisible(false);
			}
		});

		cell.addListener(new ClickListener(Buttons.LEFT) {
			public void clicked(InputEvent event, float x, float y) {
				World.player.stats.hand = item;
				MadSand.print("You take " + item.name + " to your hand");
				Gui.setHandDisplay(item.id);
				World.player.doAction();
				Utils.toggleInventory();
			}
		});

		tooltipTbl = new Table();
		itemInfoLbl = new Label(item.getInfoString(), Gui.skin);

		tooltipTbl.add(itemInfoLbl).width(TOOLTIP_WIDTH);
		tooltipTbl.row();

		tooltipTbl.setBackground(Gui.darkBackgroundSizeable);
		tooltipTbl.setSize(TOOLTIP_WIDTH, TOOLTIP_HEIGHT);

		tooltip = new Tooltip<Table>(tooltipTbl);
		tooltip.setInstant(true);

		cell.addListener(tooltip);

		cell.addListener(new ClickListener(Buttons.RIGHT) {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (!invCellContextContainer.isVisible() && !Gui.gameUnfocused) {
					invCellContextContainer.setVisible(true);
					Mouse.x = Gdx.input.getX();
					Mouse.y = Gdx.graphics.getHeight() - Gdx.input.getY();
					invCellContextContainer.setPosition(Mouse.x + CONTEXT_BTN_WIDTH / CONTEXT_W_DENOMINATOR,
							Mouse.y);
					Gui.gameUnfocused = true;
				} else {
					invCellContextContainer.setVisible(false);
					Gui.gameUnfocused = false;
				}
			}
		});
	}

	private void closeContextMenu() {
		invCellContextContainer.setVisible(false);
	}

	private void addContextBtn(TextButton btn) {
		invCellContextContainer.add(btn).width(CONTEXT_BTN_WIDTH).height(CONTEXT_BTN_HEIGHT).row();
	}

	boolean contextActive() {
		return invCellContextContainer.isVisible();
	}

	void hideContext() {
		Gui.gameUnfocused = false;
		closeContextMenu();
	}

	void setText(String str) {
		itemQuantityLabel.setText(str);
	}

	void setHp(int hp) {
		toolHpLabel.setText("[GREEN]" + hp);
	}
}
