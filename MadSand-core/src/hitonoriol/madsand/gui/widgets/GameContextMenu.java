package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Mouse;
import hitonoriol.madsand.world.World;

public class GameContextMenu extends Table {
	Skin skin;

	public TextButton[] contextMenuButtons;

	public GameContextMenu() {
		super();
		skin = Gui.skin;
		contextMenuButtons = new TextButton[5];
		float width = Gui.DEFWIDTH;

		int cc = 0;
		contextMenuButtons[0] = new TextButton("Interact", skin);
		contextMenuButtons[0].addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				World.player.lookAtMouse(Mouse.wclickx, Mouse.wclicky);
				World.player.interact();
			}

		});
		contextMenuButtons[3] = new TextButton("Use item", skin);
		contextMenuButtons[3].addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				World.player.useItem();
			}

		});
		contextMenuButtons[4] = new TextButton("Put held item to backpack", skin);
		contextMenuButtons[4].addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				World.player.freeHands();
				closeGameContextMenu();
			}

		});
		contextMenuButtons[1] = new TextButton("Attack", skin);
		contextMenuButtons[1].addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				World.player.lookAtMouse(Mouse.wx, Mouse.wy);
				World.player.attack();
			}
		});
		contextMenuButtons[2] = new TextButton("Turn", skin);
		contextMenuButtons[2].addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				World.player.lookAtMouse(Mouse.wx, Mouse.wy);
			}
		});

		while (cc < 5) {
			super.add(contextMenuButtons[cc]).width(width).height(20.0F);
			super.row();
			cc++;
		}
		super.setVisible(false);
	}

	private final static int CONTEXT_USE_BTN = 3;

	public void openGameContextMenu() {
		contextMenuButtons[CONTEXT_USE_BTN].setText("Use " + World.player.stats.hand().name);
		Gui.overlay.hideTooltip();
		super.setVisible(true);
		super.setPosition(Mouse.x + 50, Mouse.y - 30);
		Mouse.wclickx = Mouse.wx;
		Mouse.wclicky = Mouse.wy;
	}

	public void closeGameContextMenu() {
		Gui.overlay.showTooltip();
		super.setVisible(false);
		Gui.gameUnfocused = false;
	}
}
