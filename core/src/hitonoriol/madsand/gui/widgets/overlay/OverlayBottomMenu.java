package hitonoriol.madsand.gui.widgets.overlay;

import java.util.function.Supplier;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Storage;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.inventory.InventoryUI;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.MouseoverListener;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.dialogs.AbilityDialog;
import hitonoriol.madsand.gui.dialogs.BestiaryDialog;
import hitonoriol.madsand.gui.dialogs.BuildDialog;
import hitonoriol.madsand.gui.dialogs.CharacterInfoWindow;
import hitonoriol.madsand.gui.dialogs.LandDialog;
import hitonoriol.madsand.gui.dialogs.QuestJournal;
import hitonoriol.madsand.gui.dialogs.WaypointDialog;
import hitonoriol.madsand.gui.stages.Overlay;
import hitonoriol.madsand.input.Keyboard;

public class OverlayBottomMenu extends Table {

	static float WIDTH = 135;
	static float HEIGHT = 35;
	static float BUTTON_PADDING = 5;
	static float TABLE_PADDING_LEFT = 25;

	Overlay overlay;
	Table container = Widgets.table();

	public OverlayBottomMenu(Overlay overlay) {
		this.overlay = overlay;
		
		MouseoverListener.setUp(this);
		addButton("Character", Keys.Q, () -> new CharacterInfoWindow());
		addButton("Inventory", Keys.E, () -> new InventoryUI(MadSand.player().inventory));
		addButton("Abilities", Keys.R, () -> new AbilityDialog(MadSand.player().getAbilities()));
		addButton("Journal", Keys.J, () -> new QuestJournal(MadSand.player().getQuestWorker()));
		addButton("Build", Keys.B, () -> new BuildDialog());
		addButton("Bestiary", Keys.X, () -> new BestiaryDialog(MadSand.player()));
		addButton("Land", Keys.L, () -> new LandDialog(MadSand.world().getLocation()));
		addButton("Waypoints", Keys.O, () -> new WaypointDialog());

		container.setBackground(new NinePatchDrawable(GuiSkin.darkBackground()));

		super.add(container).size(Gdx.graphics.getWidth(), HEIGHT + BUTTON_PADDING * 2);
		container.align(Align.bottomLeft);
		super.pack();
		super.padLeft(TABLE_PADDING_LEFT);
	}

	private void addButton(String text, int key, Supplier<GameDialog> dialogCreator) {
		Storage<GameDialog> dialog = new Storage<>();
		addButton(text, key, () -> {
			if (Gui.overlay.getActors().contains(dialog.get(), true))
				dialog.get().hide();
			else if (!Gui.isDialogActive())
				dialog.set(dialogCreator.get()).show();
		});
	}

	private void addButton(String text, int key, Runnable action) {
		TextButton button = Widgets.button(text + " [" + Keys.toString(key) + "]");
		container.add(button).size(WIDTH, HEIGHT).pad(BUTTON_PADDING);
		Gui.setAction(button, action);
		Keyboard.getKeyBindManager().bind(action::run, true, key);
	}

}
