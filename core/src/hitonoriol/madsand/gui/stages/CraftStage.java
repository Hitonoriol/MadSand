package hitonoriol.madsand.gui.stages;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.MadSand.Screens;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.gui.widgets.itembutton.CraftButton;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.util.TimeUtils;
import hitonoriol.madsand.util.Utils;

public class CraftStage extends Stage {
	private static float FADE_DELAY = 0.15f;
	private static float ENTRY_XPAD = 50, ENTRY_YPAD = 5;
	private static float BACK_BUTTON_HEIGHT = 50, BACK_BUTTON_WIDTH = 250;
	private static float TITLE_PADDING = 30;
	private static String titleString = "Crafting";
	static final int ENTRIES_PER_ROW = 2;

	private Table containerTable = new Table();
	private Table craftTable = new Table();
	private AutoFocusScrollPane scroll = new AutoFocusScrollPane(craftTable);
	private Label titleLabel = new Label(titleString, Gui.skin);
	private float scrollX, scrollY;
	private int craftStationId;

	public CraftStage() {
		super(Gui.viewport());
		titleLabel.setAlignment(Align.center);
		Gui.setFontSize(titleLabel, Gui.FONT_M);

		addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				scrollX = scroll.getScrollX();
				scrollY = scroll.getScrollY();
				Utils.dbg("Saving position %f %f", scrollX, scrollY);
				super.clicked(event, x, y);
			}
		});
	}

	public void refreshCraftMenu() {
		refreshCraftMenu(0);
	}

	public void refreshCraftMenu(int craftStationId) {
		this.craftStationId = craftStationId;
		Player player = MadSand.player();
		List<Integer> itemList;
		String stationName = titleString;
		Label unlockProgressLabel = null;

		Utils.out("Refreshing craft menu id: " + craftStationId);
		craftTable.clear();
		containerTable.clear();

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
		Utils.out("Total unlocked recipes: " + craftSz + " out of " + ItemProp.craftReq.size());

		if (craftSz == 0)
			craftTable.add(new Label("You don't know any craft recipes.", Gui.skin));

		int i = 0;
		for (int id : itemList) {
			boolean lastInRow = (i + 1) % ENTRIES_PER_ROW == 0;
			craftTable.add(createEntry(id))
					.padRight(!lastInRow ? ENTRY_XPAD : 0)
					.padBottom(ENTRY_YPAD);

			if (lastInRow)
				craftTable.row();
			++i;
		}
		craftTable.row();

		craftTable.align(Align.center);
		craftTable.pack();
		scroll.addAction(Actions.alpha(0));
		scroll.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		containerTable.setBackground(GuiSkin.darkBackgroundSizeable);
		containerTable.setFillParent(true);
		containerTable.add(titleLabel).align(Align.center).row();

		if (craftStationId == 0)
			containerTable.add(unlockProgressLabel).padTop(TITLE_PADDING / 3).row();

		containerTable.add(scroll)
				.size(Gdx.graphics.getWidth(),
						Gdx.graphics.getHeight() - (BACK_BUTTON_HEIGHT + titleLabel.getHeight() + TITLE_PADDING))
				.row();

		TextButton backBtn = new TextButton("Back", Gui.skin);
		backBtn.align(Align.center);

		containerTable.add(backBtn).size(BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT).align(Align.center).row();
		super.addActor(containerTable);
		TimeUtils.scheduleTask(() -> {
			Utils.dbg("Restoring position %f %f", scrollX, scrollY);
			scroll.setScrollY(scrollY);
			scroll.setScrollX(scrollX);
			scroll.updateVisualScroll();
			scroll.addAction(Actions.fadeIn(FADE_DELAY));
		});

		Gui.setAction(backBtn, () -> {
			if (craftStationId == 0)
				MadSand.switchScreen(Screens.Game);
			else
				MadSand.reset();
		});
	}

	private final static Drawable entryBg = GuiSkin.getColorDrawable(new Color(0, 0, 0, 0.2f));

	private Table createEntry(int id) {
		Table entry = new Table();
		CraftButton craftButton = new CraftButton(ItemProp.getItem(id), () -> afterCrafting());
		Label recipeLabel = new Label(Item.createReadableItemList(ItemProp.getCraftRecipe(id)), Gui.skin);
		recipeLabel.setAlignment(Align.left);
		recipeLabel.setWrap(true);
		final float width = craftButton.getWidth();
		entry.add(craftButton).width(width).padRight(10);
		entry.add(recipeLabel).align(Align.left).width(width);
		entry.setBackground(entryBg);
		entry.pack();
		return entry;
	}

	public void afterCrafting() {
		scroll.addAction(Actions.sequence(Actions.fadeOut(FADE_DELAY),
				Actions.run(() -> refreshCraftMenu(craftStationId))));
	}
}
