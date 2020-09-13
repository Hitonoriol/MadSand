package hitonoriol.madsand.gui.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.GameState;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.world.World;

public class CraftMenu extends Stage {
	private static float CRAFT_BTN_WIDTH = 250;
	private static float CRAFT_ENTRY_PADDING = 30;

	Skin skin;
	public Table containerTable;
	AutoFocusScrollPane scroll;
	TextButton[] craftButtons;

	public CraftMenu() {
		containerTable = new Table();
		skin = Gui.skin;
	}

	public void refreshCraftMenu() {
		Utils.out("Refreshing craft menu...");
		containerTable.remove();
		containerTable = new Table();
		Player player = World.player;
		// player.refreshAvailableRecipes();
		int craftSz = player.craftRecipes.size();
		Utils.out("Total unlocked recipes: " + craftSz + " out of " + Resources.craftableItemCount);

		if (craftSz == 0) {
			containerTable.add(new Label("You don't know any craft recipes.", skin));
			Utils.out("No unlocked recipes.");
		}

		craftButtons = new TextButton[craftSz];

		int i = 0;
		int perRow = 3, id;
		int quantity;
		String craftString;

		while (i < craftSz) {
			craftString = "";
			id = player.craftRecipes.get(i);
			quantity = ItemProp.getCraftQuantity(id);
			if (quantity > 1)
				craftString = quantity + " ";
			craftString += ItemProp.getItemName(id);
			craftButtons[i] = new TextButton(craftString, skin);
			containerTable.add(craftButtons[i]).width(CRAFT_BTN_WIDTH);
			containerTable.add(new Label(" " + Item.queryToName(ItemProp.getCraftRecipe(id)), skin))
					.padRight(CRAFT_ENTRY_PADDING);

			if ((i + 1) % perRow == 0)
				containerTable.row();

			final int j = i, fid = id;
			Utils.out("Creating a button for item " + j + " craft recipe...");

			craftButtons[j].addListener(new ChangeListener() {
				public void changed(ChangeListener.ChangeEvent event, Actor actor) {
					World.player.craftItem(fid);
				}
			});

			i++;
		}
		containerTable.row();

		containerTable.setBackground(Gui.darkBackgroundSizeable);
		scroll = new AutoFocusScrollPane(containerTable);
		scroll.setSize(MadSand.XDEF, MadSand.YDEF);
		super.addActor(scroll);

		Table backTable = new Table();
		TextButton backBtn = new TextButton("Back", skin);

		backTable.align(Align.bottom);
		backTable.add(backBtn).fillY().expandY();
		backTable.setWidth(Gdx.graphics.getWidth());
		backBtn.align(Align.center);
		backBtn.setOrigin(Align.center);
		backBtn.pad(10);
		backBtn.setWidth(250);
		backBtn.setHeight(50);

		backBtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.switchStage(GameState.INVENTORY, Gui.overlay);
			}
		});

		super.addActor(backTable);
	}
}
