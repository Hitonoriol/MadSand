package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.entities.inventory.Item;
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
		return new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				World.player.craftItem(buttonItem.id);
			}
		};
	}

}
