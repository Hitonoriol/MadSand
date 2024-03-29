package hitonoriol.madsand.gui.widgets.overlay;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.input.Mouse;

public class GameContextMenu extends Table {
	private final static float WIDTH = 155, HEIGHT = Gui.FONT_S * 3;
	private Pair clickPos = new Pair();

	public GameContextMenu() {
		setVisible(false);
	}

	private void refresh() {
		super.clear();
		var map = MadSand.world().getCurLoc();
		var player = MadSand.player();
		var hand = player.stats.hand();

		defaults().size(WIDTH, HEIGHT);

		if (!map.getObject(clickPos).isEmpty())
			addButton("Interact", () -> {
				player.lookAtMouse(clickPos.x, clickPos.y, true);
				player.interact();
			});

		if (!map.getNpc(clickPos).isEmpty())
			addButton("Attack", () -> {
				player.lookAtMouse(clickPos.x, clickPos.y, true);
				if (player.canPerformRangedAttack())
					player.rangedAttack(clickPos);
				else
					player.meleeAttack();
			});

		if (player.at(clickPos))
			addButton("Rest fully", () -> player.restFully());
		else
			addButton("Turn", () -> player.lookAtMouse(clickPos.x, clickPos.y));

		if (!hand.equals(Item.nullItem))
			addButton("Unequip " + hand.name, () -> player.freeHands());

		setVisible(false);
	}

	public void addButton(String text, Runnable action) {
		var button = Widgets.button(text);
		button.getLabel().setWrap(true);

		super.add(button).row();

		Gui.setAction(button, () -> {
			action.run();
			close();
		});
	}

	public void open() {
		if (isVisible())
			return;

		Gui.unfocusGame();
		clickPos.set(Mouse.wx, Mouse.wy);
		refresh();
		setVisible(true);
		setPosition(Mouse.x + WIDTH / 3, Mouse.y - 30);
	}

	public void close() {
		if (!isVisible())
			return;

		Gui.resumeGameFocus();
		setVisible(false);
	}
}
