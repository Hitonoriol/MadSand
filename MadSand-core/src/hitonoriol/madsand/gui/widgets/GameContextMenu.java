package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Mouse;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.world.World;

public class GameContextMenu extends Table {
	Skin skin;
	float WIDTH = 125;

	int clickX, clickY;

	public TextButton[] contextMenuButtons;

	public GameContextMenu() {
		super();
		skin = Gui.skin;
	}

	private void refresh() {
		super.clear();
		Map map = MadSand.world.getCurLoc();
		Player player = World.player;
		Item hand = player.stats.hand();

		if (!map.getObject(clickX, clickY).equals(Map.nullObject))
			addButton("Interact", new ChangeListener() {
				public void changed(ChangeListener.ChangeEvent event, Actor actor) {
					player.lookAtMouse(clickX, clickY, true);
					player.interact();
				}
			});

		if (!map.getNpc(clickX, clickY).equals(Map.nullNpc))
			addButton("Attack", new ChangeListener() {
				public void changed(ChangeListener.ChangeEvent event, Actor actor) {
					player.lookAtMouse(clickX, clickY, true);
					player.attack();
					closeGameContextMenu();
				}
			});

		addButton("Turn", new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				World.player.lookAtMouse(clickX, clickY);
				closeGameContextMenu();
			}
		});

		if (!hand.equals(Item.nullItem))
			addButton("Unequip " + hand.name, new ChangeListener() {
				public void changed(ChangeListener.ChangeEvent event, Actor actor) {
					World.player.freeHands();
					closeGameContextMenu();
				}
			});

		super.setVisible(false);
	}

	private void addButton(String text, ChangeListener listener) {
		TextButton button = new TextButton(text, skin);
		button.addListener(listener);
		button.getLabel().setWrap(true);
		super.add(button).width(WIDTH).minHeight(20).row();
	}

	public void openGameContextMenu() {
		clickX = Mouse.wx;
		clickY = Mouse.wy;
		refresh();
		Gui.overlay.hideTooltip();
		super.setVisible(true);
		super.setPosition(Mouse.x + 50, Mouse.y - 30);
	}

	public void closeGameContextMenu() {
		Gui.overlay.showTooltip();
		super.setVisible(false);
		Gui.gameUnfocused = false;
	}
}
