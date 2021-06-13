package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.item.CropSeeds;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.Tool;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.gui.OverlayMouseoverListener;
import hitonoriol.madsand.input.Keyboard;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.util.Utils;

public class ActionButton extends Table {
	public Skin skin;

	public OverlayMouseoverListener inGameBtnListener;
	public Runnable npcInteractAction;
	public Runnable objInteractAction;
	public Runnable travelAction;
	public Runnable useItemAction;

	public TextButton interactButton;

	private static final float WIDTH = 325, HEIGHT = 4.75f * Gui.FONT_S;
	public static final float ACTION_TBL_YPOS = Gdx.graphics.getHeight() / 5.75f;

	public ActionButton() {
		super();

		skin = Gui.skin;

		interactButton = new TextButton("", skin);
		interactButton.getLabel().setWrap(true);
		super.setVisible(false);
		super.setPosition(Gui.horizontalCenter(this), ACTION_TBL_YPOS);
		super.defaults().size(WIDTH, HEIGHT);

		inGameBtnListener = new OverlayMouseoverListener();

		useItemAction = () -> MadSand.player().useItem();
		npcInteractAction = () -> MadSand.player().interact();

		objInteractAction = () -> {
			MadSand.player().interact();
			Gui.gameUnfocused = true;
			refresh();
		};

		travelAction = () -> MadSand.world().travel();
	}

	public void hideButton() {
		super.removeActor(interactButton);
		super.setVisible(false);
	}

	private void setAction(int button, Runnable action) {
		Gui.setClickAction(interactButton, button, () -> {
			action.run();
			hideButton();
			Gui.gameResumeFocus();
			Utils.scheduleTask(() -> Gdx.graphics.requestRendering(), 0.125f);
		});
	}

	private void activateButton(String text, Runnable lmbAction, Runnable rmbAction) {
		super.setVisible(true);
		interactButton.setVisible(true);
		interactButton.setText(text);

		setAction(Buttons.LEFT, lmbAction);
		setAction(Buttons.RIGHT, rmbAction);
	}

	private void activateButton(String text, Runnable lmbAction) {
		activateButton(text, lmbAction, () -> {});
	}

	private void initButton() {
		super.clear();
		super.add(interactButton).row();
		super.addListener(inGameBtnListener);
	}

	private String npcInteractionString(AbstractNpc npc) {
		String str = "";

		if (npc.isNeutral())
			str = "[LMB] " + npc.interactButtonString() + npc.getName()
					+ Resources.LINEBREAK + Resources.LINEBREAK
					+ "[RMB] Attack";

		return str;
	}

	public void refresh() {
		if (Gui.isGameUnfocused() || Keyboard.inputIgnored()) {
			hideButton();
			return;
		}

		Map loc = MadSand.world().getCurLoc();
		Player player = MadSand.player();
		Pair coords = new Pair(player.x, player.y);

		Tile tile = loc.getTile(coords.x, coords.y);
		coords.addDirection(player.stats.look);

		int tileItem = MapObject.rollTileResource(tile.id, player.stats.getEquippedToolType());
		MapObject object = loc.getObject(coords.x, coords.y);
		AbstractNpc npc = loc.getNpc(coords.x, coords.y);
		Item item = player.stats.hand();
		String tileAction = TileProp.getOnInteract(tile.id);
		String objAction = ObjectProp.getOnInteract(object.id);
		boolean holdsShovel = player.stats.isToolEquipped(Tool.Type.Shovel);
		String tileMsg = "Interact with ";

		initButton();

		if (item.is(CropSeeds.class) && tile.id == ItemProp.getCropSoil(item.id))
			activateButton("Plant " + item.name, useItemAction);

		else if (player.canTravel())
			activateButton("Travel to the next sector", travelAction);

		else if (!tile.equals(Map.nullTile) // Tile interaction
				&& (!tileAction.equals(Resources.emptyField)
						|| (tileItem != -1 && holdsShovel))) {

			if (tileItem != -1 && holdsShovel)
				tileMsg = "Dig ";

			activateButton(tileMsg + TileProp.getName(tile.id), () -> {
				if (!holdsShovel)
					Lua.execute(tileAction);
				else
					player.useItem();
				Gui.gameUnfocused = false;
				Gui.overlay.showTooltip();
				hideButton();
			});

		} else if (!npc.equals(Map.nullNpc) && npc.isNeutral())
			activateButton(npcInteractionString(npc), npcInteractAction, () -> player.meleeAttack());

		else if (!object.equals(Map.nullObject) && !objAction.equals(Resources.emptyField)) // Map object interaction button
			activateButton("Interact with " + object.name, objInteractAction);

		else
			hideButton();
	}
}
