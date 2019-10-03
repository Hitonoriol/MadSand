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
	private Table invCellContextContainer;
	private TextButton[] invCellContextMenu;

	private final int CONTEXT_BUTTONS = 1;

	Group cell;

	public InventoryUICell(Item item) {
		btn = new ImageButton(new SpriteDrawable(new Sprite(Utils.item[item.id])));
		itemQuantityLabel = new Label(item.quantity + "", Gui.skin);

		cell = new Group();
		cell.addActor(btn);
		cell.addActor(itemQuantityLabel);
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
				MadSand.contextopened = false;
				World.player.dropItem(item.id, item.quantity);
			}

		});

		btn.addListener(new ClickListener(Buttons.LEFT) {
			public void clicked(InputEvent event, float x, float y) {
				World.player.stats.hand = item.id;
				Gui.setHandDisplay(item.id);
				World.player.doAction();
				Utils.toggleInventory();
			}
		});

		btn.addListener(new ClickListener(Buttons.RIGHT) {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (!invCellContextContainer.isVisible() && !MadSand.contextopened) {
					invCellContextContainer.setVisible(true);
					MadSand.mx = Gdx.input.getX();
					MadSand.my = Gdx.graphics.getHeight() - Gdx.input.getY();
					invCellContextContainer.setPosition(MadSand.mx, MadSand.my);
					MadSand.contextopened = true;
				} else {
					invCellContextContainer.setVisible(false);
					MadSand.contextopened = false;
				}
			}
		});
	}

	void setText(String str) {
		itemQuantityLabel.setText(str);
	}
}
