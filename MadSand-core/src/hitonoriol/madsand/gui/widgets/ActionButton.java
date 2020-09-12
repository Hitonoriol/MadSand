package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.enums.ItemType;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.world.World;

public class ActionButton {
	public Skin skin;

	public InputListener inGameBtnListener;
	public ChangeListener npcInteractListener;
	public ChangeListener objInteractListener;

	public Table actionTbl;
	public TextButton interactBtn;

	private static float WIDTH = 300;

	public ActionButton() {
		skin = Gui.skin;
	}

	public void hideActionBtn() {
		actionTbl.removeActor(interactBtn);
		actionTbl.setVisible(false);
	}

	private void activateInteractBtn(TextButton btn, String text, ChangeListener listener) {
		actionTbl.setVisible(true);
		btn.setVisible(true);
		btn.setText(text);
		btn.addListener(listener);
	}

	public void processActionMenu() {
		Map loc = MadSand.world.getCurLoc();

		Player player = World.player;
		Pair coords = new Pair(player.x, player.y);

		Tile tile = loc.getTile(coords.x, coords.y);
		coords.addDirection(player.stats.look);

		int tileItem = MapObject.getTileAltItem(tile.id, player.stats.hand.type.get());
		MapObject object = loc.getObject(coords.x, coords.y);
		Npc npc = loc.getNpc(coords.x, coords.y);
		String tileAction = TileProp.getOnInteract(tile.id);
		String objAction = ObjectProp.getOnInteract(object.id);

		if (tileAction.contentEquals(Resources.emptyField) && npc == Map.nullNpc && object == Map.nullObject
				&& tileItem == -1) {
			actionTbl.setVisible(false);
			return;
		}

		actionTbl.removeActor(interactBtn);
		interactBtn = new TextButton("", skin);
		actionTbl.add(interactBtn).width(WIDTH).row();
		actionTbl.addListener(inGameBtnListener);
		boolean holdsShovel = player.stats.hand.type == ItemType.Shovel;

		String tileMsg = "Interact with ";

		if (!tile.equals(Map.nullTile) // Tile interaction button
				&& (!tileAction.equals(Resources.emptyField)
						|| (tileItem != -1 && holdsShovel))) {

			if (tileItem != -1 && holdsShovel)
				tileMsg = "Dig ";

			activateInteractBtn(interactBtn, tileMsg + TileProp.getName(tile.id), new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if (!holdsShovel)
						LuaUtils.execute(tileAction);
					else
						player.useItem();
					Gui.gameUnfocused = false;
					Gui.mouselabel.setVisible(true);
				}
			});

		} else if (!npc.equals(Map.nullNpc) && !Gui.dialogActive) //NPC interaction button
			activateInteractBtn(interactBtn, "Talk to " + npc.stats.name, npcInteractListener);

		else if (!object.equals(Map.nullObject) && !objAction.equals(Resources.emptyField)) // Map object interaction button
			activateInteractBtn(interactBtn, "Interact with " + object.name, objInteractListener);

		else
			hideActionBtn();
	}
}
