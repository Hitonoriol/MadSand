package hitonoriol.madsand.entities.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Mouse;
import hitonoriol.madsand.enums.ItemType;
import hitonoriol.madsand.world.World;

public class InventoryUICell extends ItemUI {

	private static final float CONTEXT_BTN_WIDTH = 100F;
	private static final float CONTEXT_BTN_HEIGHT = 30F;
	public static final float CONTEXT_W_DENOMINATOR = 1.75f;

	private Table invCellContextContainer; // RMB context menu container and buttons

	private TextButton dropBtn;
	private TextButton useBtn;
	private TextButton equipBtn;

	public InventoryUICell(Item item) {
		super(item);
		initContextMenu(item);

		this.addListener(new ClickListener(Buttons.LEFT) {
			public void clicked(InputEvent event, float x, float y) {
				if (!Gui.inventoryActive)
					return;

				World.player.stats.setHand(item);
				MadSand.print("You take " + item.name + " in your hand");
				Gui.overlay.setHandDisplay(item);
				World.player.doAction();
				Gui.toggleInventory();
			}
		});

		this.addListener(new ClickListener(Buttons.RIGHT) {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (!Gui.inventoryActive)
					return;
				
				if (!invCellContextContainer.isVisible()) {
					invCellContextContainer.setVisible(true);
					Mouse.x = Gdx.input.getX();
					Mouse.y = Gdx.graphics.getHeight() - Gdx.input.getY();
					invCellContextContainer.setPosition(Mouse.x + CONTEXT_BTN_WIDTH / CONTEXT_W_DENOMINATOR,
							Mouse.y);

				} else {
					invCellContextContainer.setVisible(false);

				}
			}
		});
	}

	private void initContextMenu(Item item) {
		invCellContextContainer = new Table(Gui.skin);
		dropBtn = new TextButton("Drop", Gui.skin);
		addContextBtn(dropBtn);
		invCellContextContainer.setVisible(false);
		Gui.overlay.addActor(invCellContextContainer);

		dropBtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				hideContext();
				World.player.dropItem(item);
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
}
