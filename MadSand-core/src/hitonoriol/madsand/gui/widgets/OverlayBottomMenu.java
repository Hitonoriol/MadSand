package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gui.OverlayMouseoverListener;
import hitonoriol.madsand.gui.dialogs.BestiaryDialog;
import hitonoriol.madsand.gui.dialogs.BuildDialog;
import hitonoriol.madsand.gui.dialogs.LandDialog;
import hitonoriol.madsand.gui.dialogs.QuestJournal;
import hitonoriol.madsand.gui.stages.Overlay;
import hitonoriol.madsand.world.World;

/* TODO
 * Horizontal button table with shortcuts to:
 * Stat menu, inventory, ... 
 */

public class OverlayBottomMenu extends Table {

	static float WIDTH = 135;
	static float HEIGHT = 35;
	static float BUTTON_PADDING = 5;
	static float TABLE_PADDING_LEFT = 25;

	Skin skin;

	TextButton characterStatButton;
	TextButton inventoryButton;
	TextButton journalButton;
	TextButton buildButton;
	TextButton bestiaryButton;
	public TextButton landButton;

	NinePatchDrawable background;

	OverlayMouseoverListener mouseoverListener;
	Overlay overlay;

	public OverlayBottomMenu(Overlay overlay) {
		super();

		skin = Gui.skin;
		this.overlay = overlay;
		mouseoverListener = new OverlayMouseoverListener();

		addButton(characterStatButton = new TextButton("Character [Q]", skin));
		addButton(inventoryButton = new TextButton("Inventory [E]", skin));
		addButton(journalButton = new TextButton("Journal [J]", skin));
		addButton(buildButton = new TextButton("Build [B]", skin));
		addButton(bestiaryButton = new TextButton("Bestiary [X]", skin));
		addButton(landButton = new TextButton("Land [L]", skin));

		background = new NinePatchDrawable(Gui.darkBackgroundSizeable);

		super.setBackground(background);

		initButtonListeners();

		super.align(Align.bottomLeft);
		super.pack();
		super.padLeft(TABLE_PADDING_LEFT);
	}

	private void initButtonListeners() {
		bestiaryButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				new BestiaryDialog(World.player).show();

			}
		});

		characterStatButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				overlay.toggleStatsWindow();
			}

		});

		inventoryButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Gui.toggleInventory();
			}

		});

		journalButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				new QuestJournal(World.player.quests).show();

			}

		});

		buildButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				new BuildDialog().show();
			}

		});
		
		landButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				new LandDialog(MadSand.world.getLocation()).show();
			}
			
		});
	}

	private void addButton(TextButton button) {
		button.addListener(mouseoverListener);
		super.add(button).size(WIDTH, HEIGHT).pad(BUTTON_PADDING);
	}

}
