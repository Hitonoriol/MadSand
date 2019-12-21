package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class InventoryUICell {
	private final int size = 80;
	private ImageButton btn;
	private Label itemQuantityLabel;
	private Label toolHpLabel;
	private Table invCellContextContainer;
	private TextButton[] invCellContextMenu;

	private final int CONTEXT_BUTTONS = 1;

	Group cell;

	public InventoryUICell(Item item) {
		toolHpLabel = new Label("", Gui.skin);
		btn = new ImageButton(new SpriteDrawable(new Sprite(Resources.item[item.id])));
		itemQuantityLabel = new Label(item.quantity + "", Gui.skin);

		if (item.type.isTool())
			setHp(item.hp);

		toolHpLabel.setPosition(itemQuantityLabel.getX() + size / 1.6f, itemQuantityLabel.getY() + 6);

		cell = new Group();
		cell.addActor(btn);
		cell.addActor(itemQuantityLabel);
		cell.addActor(toolHpLabel);
		cell.setSize(size, size);

		invCellContextMenu = new TextButton[CONTEXT_BUTTONS];
		invCellContextMenu[0] = new TextButton("Drop", Gui.skin);

		invCellContextContainer = new Table(Gui.skin);
		invCellContextContainer.add(invCellContextMenu[0]).width(100.0F).height(50.0F).row();
		invCellContextContainer.setVisible(false);
		Gui.overlay.addActor(invCellContextContainer);

		invCellContextMenu[0].addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				invCellContextContainer.setVisible(false);
				Gui.contextMenuActive = false;
				World.player.dropItem(item.id, item.quantity);
			}

		});

		btn.addListener(new ClickListener(Buttons.LEFT) {
			public void clicked(InputEvent event, float x, float y) {
				World.player.stats.hand = item;
				Gui.setHandDisplay(item.id);
				World.player.doAction();
				Utils.toggleInventory();
			}
		});

		btn.addListener(new ClickListener(Buttons.RIGHT) {
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
