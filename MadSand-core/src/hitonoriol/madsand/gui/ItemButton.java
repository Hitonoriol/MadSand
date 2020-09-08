package hitonoriol.madsand.gui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.inventory.Item;

public abstract class ItemButton extends Group {
	private final float WIDTH = 390;
	private final float HEIGHT = 100;

	private final float IMAGE_SIZE = 50;

	private Label itemLabel;
	private Image itemImage;
	private Table buttonTable;
	private Image highlight;

	protected Item buttonItem;

	public ItemButton(Item item) {
		buttonItem = item;
		String buttonText = createButtonText();
		itemLabel = new Label(buttonText, Gui.skin);
		itemImage = new Image(Resources.item[item.id]);
		itemImage.setSize(IMAGE_SIZE, IMAGE_SIZE);

		buttonTable = new Table();
		buttonTable.add(itemImage).align(Align.left);
		buttonTable.add(itemLabel).align(Align.right);
		buttonTable.row();

		highlight = new Image(Resources.noEquip);
		highlight.setVisible(false);
		highlight.setSize(WIDTH, HEIGHT);

		buttonTable.setSize(WIDTH, HEIGHT);
		super.addActor(buttonTable);
		super.addActor(highlight);
		super.setSize(WIDTH, HEIGHT);

	}
	
	protected void setUpListeners() {
		this.addListener(setMouseOverListener());
		this.addListener(setButtonPressListener());
	}

	protected String createButtonText() { // Must be overridden
		return null;
	}

	private InputListener setMouseOverListener() {
		return new InputListener() {
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				highlight.setVisible(true);
			}

			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				highlight.setVisible(false);
			}
		};
	}

	protected ClickListener setButtonPressListener() { // Must be overridden
		return null;
	}

}
