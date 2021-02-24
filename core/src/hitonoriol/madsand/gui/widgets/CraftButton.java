package hitonoriol.madsand.gui.widgets;

import java.util.function.Consumer;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.CraftWorker;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.dialogs.SliderDialog;
import hitonoriol.madsand.world.World;

public class CraftButton extends ItemButton {

	private float WIDTH_SIZEBY = -150;
	private float HEIGHT_SIZEBY = 0;

	public CraftButton(Item item) {
		super(item);

		super.buttonTable.align(Align.left);

		super.sizeBy(WIDTH_SIZEBY, HEIGHT_SIZEBY);
		super.buttonTable.sizeBy(WIDTH_SIZEBY, HEIGHT_SIZEBY);
		super.highlight.sizeBy(WIDTH_SIZEBY, HEIGHT_SIZEBY);

		super.buttonTable.pack();
		setUpListeners();
	}

	@Override
	protected String createButtonText() {
		String buttonString = buttonItem.name;

		if (buttonItem.craftQuantity > 1)
			buttonString = buttonItem.craftQuantity + " " + buttonString;

		return buttonString;
	}

	@Override
	protected ClickListener setButtonPressListener() {
		Player player = World.player;
		return Gui.setClickAction(this, () -> {
			CraftWorker craftWorker = new CraftWorker(player, buttonItem);

			if (!craftWorker.canBeCrafted()) {
				Gui.drawOkDialog("Not enough resources to craft " + buttonItem.name);
				return;
			}

			int maxQuantity = craftWorker.getMaxCraftQuantity();
			Consumer<Integer> craftAction = quantity -> player.craftItem(craftWorker, quantity);

			if (maxQuantity == 1)
				craftAction.accept(maxQuantity);

			SliderDialog craftDialog = new SliderDialog(maxQuantity);
			craftDialog.setTitle("Crafting " + buttonItem.name)
					.setSliderTitle("Quantity of " + buttonItem.name + " to craft:")
					.setConfirmAction(craftAction)
					.setSliderAction(quantity -> craftDialog
							.setSliderText((quantity * buttonItem.craftQuantity) + " " + buttonItem.name))
					.show(MadSand.getStage());
		});
	}

}
