package hitonoriol.madsand.gui.stages;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.gui.widgets.itembutton.CraftButton;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;

public class CraftStage extends Stage {
	private static float CRAFT_ENTRY_PADDING = 40;
	private static float BACK_BUTTON_HEIGHT = 50;
	private static float BACK_BUTTON_WIDTH = 250;
	private static float TITLE_PADDING = 30;

	private static String titleString = "Crafting";
	static final int ENTRIES_PER_ROW = 2;

	Skin skin;
	public Table containerTable;
	public Table craftTable;
	AutoFocusScrollPane scroll;
	Label titleLabel;

	public CraftStage() {
		super(Gui.uiViewport);
		craftTable = new Table();
		containerTable = new Table();
		skin = Gui.skin;
		titleLabel = new Label(titleString, skin);
		Gui.setFontSize(titleLabel, Gui.FONT_M);
		titleLabel.setAlignment(Align.center);
	}

	public void refreshCraftMenu() {
		refreshCraftMenu(0);
	}

	public void refreshCraftMenu(int craftStationId) {
		Player player = MadSand.player();
		List<Integer> itemList;
		String stationName = titleString;
		Label unlockProgressLabel = null;

		Utils.out("Refreshing craft menu id: " + craftStationId);
		craftTable.remove();
		craftTable = new Table();
		containerTable.remove();
		containerTable = new Table();

		if (craftStationId == 0) {
			itemList = player.getCraftRecipes();
			unlockProgressLabel = new Label("Craft recipes unlocked: " + player.craftRecipeProgress(), Gui.skin);
			unlockProgressLabel.setAlignment(Align.center);
		} else {
			itemList = ItemProp.craftStationRecipes.get(craftStationId);
			stationName = ObjectProp.getName(craftStationId);
		}

		titleLabel.setText(stationName);

		int craftSz = itemList.size();
		Utils.out("Total unlocked recipes: " + craftSz + " out of " + Resources.craftableItemCount);

		if (craftSz == 0)
			craftTable.add(new Label("You don't know any craft recipes.", skin));

		int id;
		Label recipeLabel;
		CraftButton craftButton;
		for (int i = 0; i < craftSz; ++i) {
			id = itemList.get(i);

			craftButton = new CraftButton(ItemProp.getItem(id));
			recipeLabel = new Label(" " + Item.createReadableItemList(ItemProp.getCraftRecipe(id)), skin);
			recipeLabel.setAlignment(Align.left);

			craftTable.add(craftButton).width(craftButton.getWidth());
			craftTable.add(recipeLabel).align(Align.left);

			if ((i + 1) % ENTRIES_PER_ROW == 0)
				craftTable.row();
			else
				craftTable.padRight(CRAFT_ENTRY_PADDING);
		}
		craftTable.row();

		craftTable.align(Align.center);
		scroll = new AutoFocusScrollPane(craftTable);
		scroll.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		containerTable.setBackground(Gui.darkBackgroundSizeable);
		containerTable.setFillParent(true);
		containerTable.add(titleLabel).align(Align.center).row();

		if (craftStationId == 0)
			containerTable.add(unlockProgressLabel).padTop(TITLE_PADDING / 3).row();

		containerTable.add(scroll)
				.size(Gdx.graphics.getWidth(),
						Gdx.graphics.getHeight() - (BACK_BUTTON_HEIGHT + titleLabel.getHeight() + TITLE_PADDING))
				.row();

		TextButton backBtn = new TextButton("Back", skin);
		backBtn.align(Align.center);

		containerTable.add(backBtn).size(BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT).align(Align.center).row();
		super.addActor(containerTable);

		Gui.setAction(backBtn,
				craftStationId == 0 ? () -> MadSand.switchScreen(MadSand.gameScreen) : () -> MadSand.reset());
	}
}
