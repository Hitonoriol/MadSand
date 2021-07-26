package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
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
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.TimeUtils;

public class ActionButton extends Table {
	public TextButton interactButton;
	private Runnable lAction, rAction;

	private static final float WIDTH = 325, HEIGHT = 4.75f * Gui.FONT_S;
	public static final float ACTION_TBL_YPOS = Gdx.graphics.getHeight() / 5.75f;

	public ActionButton() {
		interactButton = new TextButton("", Gui.skin);
		interactButton.getLabel().setWrap(true);
		interactButton.addListener(OverlayMouseoverListener.instance());
		super.setVisible(false);
		super.setPosition(Gui.horizontalCenter(this), ACTION_TBL_YPOS);
		super.defaults().size(WIDTH, HEIGHT);

		Gui.setClickAction(interactButton, Buttons.LEFT, createClickHandler(Buttons.LEFT));
		Gui.setClickAction(interactButton, Buttons.RIGHT, createClickHandler(Buttons.RIGHT));
	}

	public void hideButton() {
		super.removeActor(interactButton);
		super.setVisible(false);
	}

	private Runnable createClickHandler(int button) {
		return () -> {
			if (button == Buttons.LEFT)
				lAction.run();
			else
				rAction.run();
			hideButton();
			Gui.gameResumeFocus();
			TimeUtils.scheduleTask(() -> Gdx.graphics.requestRendering(), 0.125f);
		};
	}

	private void activateButton(String text, Runnable lmbAction, Runnable rmbAction) {
		super.setVisible(true);
		interactButton.setVisible(true);
		interactButton.setText(text);
		lAction = lmbAction;
		rAction = rmbAction;
	}

	private void activateButton(String text, Runnable lmbAction) {
		activateButton(text, lmbAction, () -> {});
	}

	private void initButton() {
		super.clear();
		super.add(interactButton).row();
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
		Pair inFront = coords.copy().addDirection(player.stats.look);

		Tile tile = loc.getTile(coords);
		MapObject objectInFront = loc.getObject(inFront), object = loc.getObject(coords);
		AbstractNpc npc = loc.getNpc(inFront);
		Item item = player.stats.hand();

		int tileItem = MapObject.rollTileResource(tile.id, player.stats.getEquippedToolType());
		String tileAction = TileProp.getOnInteract(tile.id);
		String objAction = ObjectProp.getOnInteract(objectInFront.id);
		boolean holdsShovel = player.stats.isToolEquipped(Tool.Type.Shovel);

		initButton();

		if (object.isEmpty() && item.is(CropSeeds.class) && tile.id == ItemProp.getCropSoil(item.id))
			activateButton("Plant " + item.name, () -> MadSand.player().useItem());

		else if (player.canTravel())
			activateButton("Travel to the next sector", () -> MadSand.world().travel());

		else if (!tile.equals(Map.nullTile) // Tile interaction
				&& (!tileAction.equals(Resources.emptyField)
						|| (tileItem != -1 && holdsShovel))) {
			String tileMsg = "Interact with ";
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
			activateButton(npcInteractionString(npc), () -> MadSand.player().interact(), () -> player.meleeAttack());

		else if (!objectInFront.equals(Map.nullObject) && !objAction.equals(Resources.emptyField))
			activateButton("Interact with " + objectInFront.name, () -> MadSand.player().interact());

		else
			hideButton();
	}
}
