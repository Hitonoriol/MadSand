package ru.bernarder.fallenrisefromdust;

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

public class InventoryUICell {
	private final int size = 80;
	private int CONTEXT_BUTTONS = 1;
	
	private static final float CONTEXT_BTN_WIDTH = 100F;
	private static final float CONTEXT_BTN_HEIGHT = 30F;
	
	private float TOOLTIP_WIDTH = 200F;
	private float TOOLTIP_HEIGHT = 50F;

	private Table tooltipTbl;
	private Label itemInfoLbl;
	private Tooltip<Table> tooltip;

	private ImageButton itemBtn; // button with item image

	private Label itemQuantityLabel;
	private Label toolHpLabel;

	private Table invCellContextContainer; // RMB context menu container and buttons
	private TextButton[] invCellContextMenu;

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

		invCellContextMenu = new TextButton[CONTEXT_BUTTONS];
		invCellContextMenu[0] = new TextButton("Drop", Gui.skin);

		invCellContextContainer = new Table(Gui.skin);
		invCellContextContainer.add(invCellContextMenu[0]).width(CONTEXT_BTN_WIDTH).height(CONTEXT_BTN_HEIGHT).row();
		invCellContextContainer.setVisible(false);
		Gui.overlay.addActor(invCellContextContainer);

		invCellContextMenu[0].addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				invCellContextContainer.setVisible(false);
				Gui.contextMenuActive = false;
				World.player.dropItem(item.id, item.quantity);
			}

		});

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
				if (!invCellContextContainer.isVisible() && !Gui.contextMenuActive) {
					invCellContextContainer.setVisible(true);
					MadSand.mx = Gdx.input.getX();
					MadSand.my = Gdx.graphics.getHeight() - Gdx.input.getY();
					invCellContextContainer.setPosition(MadSand.mx, MadSand.my);
					Gui.contextMenuActive = true;
				} else {
					invCellContextContainer.setVisible(false);
					Gui.contextMenuActive = false;
				}
			}
		});
	}

	boolean contextActive() {
		return invCellContextContainer.isVisible();
	}

	void hideContext() {
		invCellContextContainer.setVisible(false);
	}

	void setText(String str) {
		itemQuantityLabel.setText(str);
	}

	void setHp(int hp) {
		toolHpLabel.setText("[GREEN]" + hp);
	}
}
