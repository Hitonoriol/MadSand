package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.gui.OverlayMouseoverListener;
import hitonoriol.madsand.gui.stages.Overlay;

/* TODO
 * Horizontal button table with shortcuts to:
 * Stat menu, inventory, ... 
 */

public class OverlayBottomMenu extends Table {

	static float WIDTH = 135;
	static float HEIGHT = 35;
	static float BUTTON_PADDING = 5;
	static float TABLE_PADDING_LEFT = 25;

	static String characterStatString = "Character [Q]";
	static String inventoryString = "Inventory [E]";

	Skin skin;
	TextButton characterStatButton;
	TextButton inventoryButton;
	NinePatchDrawable background;

	OverlayMouseoverListener mouseoverListener;
	Overlay overlay;

	public OverlayBottomMenu(Overlay overlay) {
		super();
		

		skin = Gui.skin;
		this.overlay = overlay;
		mouseoverListener = new OverlayMouseoverListener();

		characterStatButton = new TextButton(characterStatString, skin);
		inventoryButton = new TextButton(inventoryString, skin);

		addButton(characterStatButton);
		addButton(inventoryButton);
		
		background = new NinePatchDrawable(Gui.darkBackgroundSizeable);

		super.setBackground(background);

		initButtonListeners();

		super.align(Align.bottomLeft);
		super.pack();
		super.padLeft(TABLE_PADDING_LEFT);
	}

	private void initButtonListeners() {
		characterStatButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				overlay.showStatsWindow();
			}

		});

		inventoryButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Utils.toggleInventory();
			}

		});
	}

	private void addButton(TextButton button) {
		button.addListener(mouseoverListener);
		super.add(button).size(WIDTH, HEIGHT).pad(BUTTON_PADDING);
	}

}
