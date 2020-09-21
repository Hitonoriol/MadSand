package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.enums.ItemType;
import hitonoriol.madsand.enums.NpcType;
import hitonoriol.madsand.gui.OverlayMouseoverListener;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.world.World;

public class ActionButton extends Table {
	public Skin skin;
	public static final float ACTION_TBL_YPOS = Gdx.graphics.getHeight() / 6f;

	public OverlayMouseoverListener inGameBtnListener;
	public ChangeListener npcInteractListener;
	public ChangeListener objInteractListener;

	public TextButton interactButton;

	private static float WIDTH = 300;

	public ActionButton() {
		super();

		skin = Gui.skin;

		interactButton = new TextButton("", skin);
		super.setVisible(false);
		super.setPosition(Gui.horizontalCenter(this), ACTION_TBL_YPOS);
		super.add(interactButton).width(Gui.DEFWIDTH).row();

		inGameBtnListener = new OverlayMouseoverListener();

		npcInteractListener = new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				World.player.interact(World.player.stats.look);
				setVisible(false);
			}
		};

		objInteractListener = new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				World.player.interact(World.player.stats.look);
				Gui.gameUnfocused = true;
				processActionMenu();
			}
		};
	}

	public void hideActionBtn() {
		super.removeActor(interactButton);
		super.setVisible(false);
	}

	private void activateInteractBtn(TextButton btn, String text, ChangeListener listener) {
		super.setVisible(true);
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

		int tileItem = MapObject.getTileAltItem(tile.id, player.stats.hand().type.get());
		MapObject object = loc.getObject(coords.x, coords.y);
		Npc npc = loc.getNpc(coords.x, coords.y);
		String tileAction = TileProp.getOnInteract(tile.id);
		String objAction = ObjectProp.getOnInteract(object.id);

		if (tileAction.contentEquals(Resources.emptyField) && npc == Map.nullNpc && object == Map.nullObject
				&& tileItem == -1) {
			super.setVisible(false);
			return;
		}

		super.removeActor(interactButton);
		interactButton = new TextButton("", skin);
		super.add(interactButton).width(WIDTH).row();
		super.addListener(inGameBtnListener);
		boolean holdsShovel = player.stats.hand().type == ItemType.Shovel;

		String tileMsg = "Interact with ";

		if (!tile.equals(Map.nullTile) // Tile interaction button
				&& (!tileAction.equals(Resources.emptyField)
						|| (tileItem != -1 && holdsShovel))) {

			if (tileItem != -1 && holdsShovel)
				tileMsg = "Dig ";

			activateInteractBtn(interactButton, tileMsg + TileProp.getName(tile.id), new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if (!holdsShovel)
						LuaUtils.execute(tileAction);
					else
						player.useItem();
					Gui.gameUnfocused = false;
					Gui.overlay.getTooltip().setVisible(true);
					hideActionBtn();
				}
			});

		} else if (!npc.equals(Map.nullNpc) && !Gui.dialogActive && npc.friendly) { //NPC interaction button
			String npcString = npc.stats.name;

			if (!npc.type.equals(NpcType.Trader))
				npcString = "Talk to " + npcString;
			else
				npcString = "Trade with " + npcString;

			activateInteractBtn(interactButton, npcString, npcInteractListener);

		} else if (!object.equals(Map.nullObject) && !objAction.equals(Resources.emptyField)) // Map object interaction button
			activateInteractBtn(interactButton, "Interact with " + object.name, objInteractListener);

		else
			hideActionBtn();
	}
}
