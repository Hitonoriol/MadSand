package hitonoriol.madsand.gui.widgets.itembutton;

import java.util.function.Consumer;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.CraftWorker;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.dialogs.ConfirmDialog;
import hitonoriol.madsand.gui.dialogs.SliderDialog;
import hitonoriol.madsand.gui.widgets.AutoSizeTooltip;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.TimeUtils;

public class CraftButton extends ItemButton {
	private float WIDTH_SIZEBY = -150;
	private float HEIGHT_SIZEBY = 0;

	private Runnable menuRefresher;
	private CraftWorker craftWorker;

	public CraftButton(Item item, Runnable menuRefresher) {
		super(item);
		craftWorker = new CraftWorker(MadSand.player(), buttonItem);
		this.menuRefresher = menuRefresher;
		super.buttonTable.align(Align.left);

		super.sizeBy(WIDTH_SIZEBY, HEIGHT_SIZEBY);
		super.buttonTable.sizeBy(WIDTH_SIZEBY, HEIGHT_SIZEBY);
		super.highlight.sizeBy(WIDTH_SIZEBY, HEIGHT_SIZEBY);

		super.buttonTable.pack();
		setUpListeners();
		AutoSizeTooltip tooltip;
		addListener(tooltip = new AutoSizeTooltip(() -> {
			boolean canBeCrafted = craftWorker.canBeCrafted();
			String text = (canBeCrafted ? "[LIME]Can be crafted" : "[RED]Can't be crafted") + "[]";
			text += Resources.LINEBREAK + Resources.LINEBREAK + Item.createReadableItemList(item.recipe, true);
			return text;
		}).setMaxWidth(250));
		Gui.setFontSize(tooltip.getLabel(), 19);
	}

	@Override
	protected String createButtonText() {
		String buttonString = buttonItem.name;

		Player player = MadSand.player();
		if (!player.knowsItem(buttonItem.id()))
			buttonString += " (New!)";
		else
			buttonString += Resources.LINEBREAK + "(You have " + player.inventory.countItems(buttonItem.id()) + ")";

		if (buttonItem.craftQuantity > 1)
			buttonString = buttonItem.craftQuantity + " " + buttonString;

		return buttonString;
	}

	@Override
	protected ClickListener setButtonPressListener() {
		Player player = MadSand.player();
		return Gui.setClickAction(this, () -> {
			if (!craftWorker.canBeCrafted()) {
				Gui.drawOkDialog("Not enough resources to craft " + buttonItem.name);
				return;
			}

			int maxQuantity = craftWorker.getMaxCraftQuantity();
			Consumer<Integer> craftAction = quantity -> {
				player.craftItem(craftWorker, quantity);
				TimeUtils.scheduleTask(menuRefresher);
			};

			if (maxQuantity == 1) {
				new ConfirmDialog(
						"Are you sure you want to craft " + buttonItem.craftQuantity + " " + buttonItem.name + "?",
						() -> craftAction.accept(maxQuantity)).show();
				return;
			}

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
