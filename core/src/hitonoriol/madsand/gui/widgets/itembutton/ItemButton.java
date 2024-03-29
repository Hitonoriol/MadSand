package hitonoriol.madsand.gui.widgets.itembutton;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.entities.inventory.ItemUI;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.Widgets;

public abstract class ItemButton extends Group {
	private final float WIDTH = 390;
	private final float HEIGHT = 100;

	private final float IMAGE_SIZE = 50;

	protected Label itemLabel;
	protected Image itemImage;
	protected Table buttonTable;
	protected Image highlight;

	protected Item buttonItem;

	public ItemButton(Item item) {
		buttonItem = item;
		var buttonText = createButtonText();
		itemLabel = Widgets.label(buttonText);
		itemLabel.setWrap(true);
		itemImage = new Image(item.getDrawable());
		itemImage.setSize(IMAGE_SIZE, IMAGE_SIZE);

		buttonTable = Widgets.table();
		buttonTable.add(itemImage).align(Align.left);
		buttonTable.add(itemLabel).width(150).align(Align.center);
		buttonTable.setFillParent(true);

		highlight = new Image(ItemUI.emptyItem);
		highlight.addAction(Actions.alpha(0.5f));
		highlight.setVisible(false);
		highlight.setSize(WIDTH, HEIGHT);

		buttonTable.setSize(WIDTH, HEIGHT);
		super.addActor(buttonTable);
		super.addActor(highlight);
		super.setSize(WIDTH, HEIGHT);
	}

	protected void setUpListeners() {
		addListener(setMouseOverListener());
		addListener(setButtonPressListener());
	}

	protected abstract String createButtonText();

	private InputListener setMouseOverListener() {
		return new InputListener() {
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				highlight.setVisible(true);
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				highlight.setVisible(false);
			}
		};
	}

	protected abstract ClickListener setButtonPressListener();

}
