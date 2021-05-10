package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.input.Mouse;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.world.World;

public class GameContextMenu extends Table {
	Skin skin;
	float WIDTH = 155, HEIGHT = Gui.FONT_S * 3;

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

		super.defaults().size(WIDTH, HEIGHT);

		if (!map.getObject(clickX, clickY).equals(Map.nullObject))
			addButton("Interact", () -> {
				player.lookAtMouse(clickX, clickY, true);
				player.interact();
			});

		if (!map.getNpc(clickX, clickY).equals(Map.nullNpc))
			addButton("Attack", () -> {
				player.lookAtMouse(clickX, clickY, true);
				if (player.canPerformRangedAttack())
					player.rangedAttack(map.getNpc(clickX, clickY));
				else
					player.meleeAttack();
			});

		if (clickX == player.x && clickY == player.y)
			addButton("Rest fully", () -> player.restFully());
		else
			addButton("Turn", () -> World.player.lookAtMouse(clickX, clickY));

		if (!hand.equals(Item.nullItem))
			addButton("Unequip " + hand.name, () -> player.freeHands());

		super.setVisible(false);
	}

	private void addButton(String text, Runnable action) {
		TextButton button = new TextButton(text, skin);
		button.getLabel().setWrap(true);

		super.add(button).row();

		Gui.setAction(button, () -> {
			action.run();
			closeGameContextMenu();
		});
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
