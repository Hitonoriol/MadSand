package hitonoriol.madsand.entities.inventory.trade;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.TradeAction;
import hitonoriol.madsand.gui.ItemButton;
import hitonoriol.madsand.properties.ItemProp;

public class TradeInventoryButton extends ItemButton {
	TradeInventory trade;
	TradeAction action;
	TradeUIRefresher refresher;

	public TradeInventoryButton(TradeInventory trade, Item item, TradeAction action, TradeUIRefresher refresher) {
		super(item);

		this.trade = trade;
		this.action = action;
		this.refresher = refresher;

		super.setUpListeners();
	}

	@Override
	protected String createButtonText() {
		return buttonItem.quantity + " " + ItemProp.name.get(buttonItem.id) + ", " + buttonItem.getPrice() + " each";
	}

	@Override
	protected ClickListener setButtonPressListener() {
		return new ClickListener(Buttons.LEFT) {
			public void clicked(InputEvent event, float x, float y) {
				new TradeConfirmDialog(trade, buttonItem, action, refresher).show();
			}
		};
	}

}
