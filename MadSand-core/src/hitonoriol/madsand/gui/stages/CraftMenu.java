package hitonoriol.madsand.gui.stages;

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
import hitonoriol.madsand.gui.widgets.CraftButton;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.world.World;

public class CraftMenu extends Stage {
	private static float CRAFT_ENTRY_PADDING = 40;
	private static float BACK_BUTTON_HEIGHT = 50;
	private static float BACK_BUTTON_WIDTH = 100;
	private static float TITLE_PADDING = 30;

	private static String titleString = "Crafting";

	Skin skin;
	public Table containerTable;
	public Table craftTable;
	AutoFocusScrollPane scroll;
	CraftButton[] craftButtons;
	Label titleLabel;

	public CraftMenu() {
		craftTable = new Table();
		containerTable = new Table();
		skin = Gui.skin;
		titleLabel = new Label(titleString, skin);
		titleLabel.setFontScale(1.35f);
		titleLabel.setAlignment(Align.center);
	}

	public void refreshCraftMenu() {
		Utils.out("Refreshing craft menu...");
		craftTable.remove();
		craftTable = new Table();
		containerTable.remove();
		containerTable = new Table();
		Player player = World.player;
		// player.refreshAvailableRecipes();
		int craftSz = player.craftRecipes.size();
		Utils.out("Total unlocked recipes: " + craftSz + " out of " + Resources.craftableItemCount);

		if (craftSz == 0) {
			craftTable.add(new Label("You don't know any craft recipes.", skin));
			Utils.out("No unlocked recipes.");
		}

		craftButtons = new CraftButton[craftSz];

		int i = 0;
		int perRow = 2, id;
		int quantity;
		Label recipeLabel;

		while (i < craftSz) {

			id = player.craftRecipes.get(i);
			quantity = ItemProp.getCraftQuantity(id);

			craftButtons[i] = new CraftButton(ItemProp.getItem(id), quantity);
			recipeLabel = new Label(" " + Item.queryToName(ItemProp.getCraftRecipe(id)), skin);
			recipeLabel.setAlignment(Align.left);

			Utils.out("craftbtn width: " + craftButtons[i].getWidth());
			craftTable.add(craftButtons[i]).width(craftButtons[i].getWidth());
			craftTable.add(recipeLabel).align(Align.left).padRight(CRAFT_ENTRY_PADDING);

			if ((i + 1) % perRow == 0)
				craftTable.row();

			Utils.out("Creating a button for item " + i + " craft recipe...");

			i++;
		}
		craftTable.row();

		//craftTable.setBackground(Gui.darkBackgroundSizeable);
		craftTable.align(Align.center);
		scroll = new AutoFocusScrollPane(craftTable);
		scroll.setSize(MadSand.XDEF, MadSand.YDEF);

		containerTable.setBackground(Gui.darkBackgroundSizeable);
		containerTable.setFillParent(true);
		containerTable.add(titleLabel).align(Align.center).row();
		containerTable.add(scroll)
				.size(MadSand.XDEF, MadSand.YDEF - (BACK_BUTTON_HEIGHT + titleLabel.getHeight() + TITLE_PADDING)).row();

		TextButton backBtn = new TextButton("Back", skin);

		backBtn.align(Align.center);
		backBtn.setWidth(BACK_BUTTON_WIDTH);
		backBtn.setHeight(BACK_BUTTON_HEIGHT);

		containerTable.add(backBtn).align(Align.center).row();
		super.addActor(containerTable);

		backBtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.switchStage(GameState.INVENTORY, Gui.overlay);
			}
		});

	}
}