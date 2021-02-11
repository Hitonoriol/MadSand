package hitonoriol.madsand.gui.widgets;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gui.OverlayMouseoverListener;
import hitonoriol.madsand.gui.dialogs.AbilityDialog;
import hitonoriol.madsand.gui.dialogs.BestiaryDialog;
import hitonoriol.madsand.gui.dialogs.BuildDialog;
import hitonoriol.madsand.gui.dialogs.LandDialog;
import hitonoriol.madsand.gui.dialogs.QuestJournal;
import hitonoriol.madsand.gui.stages.Overlay;
import hitonoriol.madsand.world.World;

public class OverlayBottomMenu extends Table {

	static float WIDTH = 135;
	static float HEIGHT = 35;
	static float BUTTON_PADDING = 5;
	static float TABLE_PADDING_LEFT = 25;

	private Map<Integer, Button> keyBoundButtons = new HashMap<>();

	NinePatchDrawable background;

	OverlayMouseoverListener mouseoverListener;
	Overlay overlay;

	public OverlayBottomMenu(Overlay overlay) {
		super();

		this.overlay = overlay;
		mouseoverListener = new OverlayMouseoverListener();

		addButton("Character", Keys.Q, () -> overlay.toggleStatsWindow());
		addButton("Inventory", Keys.E, () -> Gui.toggleInventory());
		addButton("Abilities", Keys.R, () -> new AbilityDialog(World.player.abilities).show());
		addButton("Journal", Keys.J, () -> new QuestJournal(World.player.quests).show());
		addButton("Build", Keys.B, () -> new BuildDialog().show());
		addButton("Bestiary", Keys.X, () -> new BestiaryDialog(World.player).show());
		addButton("Land", Keys.L, () -> new LandDialog(MadSand.world.getLocation()).show());

		background = new NinePatchDrawable(Gui.darkBackgroundSizeable);

		super.setBackground(background);

		super.align(Align.bottomLeft);
		super.pack();
		super.padLeft(TABLE_PADDING_LEFT);
	}

	public boolean isKeyBoundToButton(int key) {
		return keyBoundButtons.containsKey(key);
	}

	public void toggleButton(int buttonKey) {
		if (keyBoundButtons.containsKey(buttonKey))
			keyBoundButtons.get(buttonKey).toggle();
	}

	private void addButton(String text, int key, Runnable action) {
		TextButton button = new TextButton(text + " [" + Keys.toString(key) + "]", Gui.skin);
		button.addListener(mouseoverListener);
		super.add(button).size(WIDTH, HEIGHT).pad(BUTTON_PADDING);
		Gui.setAction(button, action);
		keyBoundButtons.put(key, button);
	}

}
