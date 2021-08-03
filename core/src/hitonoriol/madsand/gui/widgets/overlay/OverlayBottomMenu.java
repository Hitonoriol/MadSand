package hitonoriol.madsand.gui.widgets.overlay;

import java.util.function.Supplier;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Storage;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.OverlayMouseoverListener;
import hitonoriol.madsand.gui.dialogs.AbilityDialog;
import hitonoriol.madsand.gui.dialogs.BestiaryDialog;
import hitonoriol.madsand.gui.dialogs.BuildDialog;
import hitonoriol.madsand.gui.dialogs.CharacterInfoWindow;
import hitonoriol.madsand.gui.dialogs.LandDialog;
import hitonoriol.madsand.gui.dialogs.QuestJournal;
import hitonoriol.madsand.gui.stages.Overlay;
import hitonoriol.madsand.input.Keyboard;

public class OverlayBottomMenu extends Table {

	static float WIDTH = 135;
	static float HEIGHT = 35;
	static float BUTTON_PADDING = 5;
	static float TABLE_PADDING_LEFT = 25;

	Overlay overlay;
	Table container = new Table();

	public OverlayBottomMenu(Overlay overlay) {
		super();

		this.overlay = overlay;

		addButton("Character", Keys.Q, () -> new CharacterInfoWindow().getDialog());
		addButton("Inventory", Keys.E, () -> MadSand.player().inventory.getUI());
		addButton("Abilities", Keys.R, () -> new AbilityDialog(MadSand.player().getAbilities()));
		addButton("Journal", Keys.J, () -> new QuestJournal(MadSand.player().getQuestWorker()));
		addButton("Build", Keys.B, () -> new BuildDialog());
		addButton("Bestiary", Keys.X, () -> new BestiaryDialog(MadSand.player()));
		addButton("Land", Keys.L, () -> new LandDialog(MadSand.world().getLocation()));

		container.setBackground(new NinePatchDrawable(Gui.darkBackgroundSizeable));

		super.add(container).size(Gdx.graphics.getWidth(), HEIGHT + BUTTON_PADDING * 2);
		container.align(Align.bottomLeft);
		super.pack();
		super.padLeft(TABLE_PADDING_LEFT);
	}

	private void addButton(String text, int key, Supplier<GameDialog> dialogCreator) {
		Storage<GameDialog> dialog = new Storage<>();
		addButton(text, key, () -> {
			if (Gui.overlay.getActors().contains(dialog.get(), true))
				dialog.get().remove();
			else if (!Gui.dialogActive)
				dialog.set(dialogCreator.get()).show();
		});
	}

	private void addButton(String text, int key, Runnable action) {
		TextButton button = new TextButton(text + " [" + Keys.toString(key) + "]", Gui.skin);
		button.addListener(OverlayMouseoverListener.instance());
		container.add(button).size(WIDTH, HEIGHT).pad(BUTTON_PADDING);
		Gui.setAction(button, action);
		Keyboard.getKeyBindManager().bind(key, true, () -> action.run());
	}

}
