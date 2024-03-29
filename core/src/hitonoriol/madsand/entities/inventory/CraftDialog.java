package hitonoriol.madsand.entities.inventory;

import static hitonoriol.madsand.MadSand.player;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gamecontent.Items;
import hitonoriol.madsand.gamecontent.Objects;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.gui.widgets.itembutton.CraftButton;
import hitonoriol.madsand.map.object.CraftingStation;
import hitonoriol.madsand.util.Utils;

public class CraftDialog extends GameDialog {
	private static final float FADE_DELAY = 0.15f;
	private static final float ENTRY_XPAD = 50, ENTRY_YPAD = 5;
	private static final float BACK_BUTTON_HEIGHT = 50, BACK_BUTTON_WIDTH = 250;
	private static final float TITLE_PADDING = 10;
	private static final String titleString = "Crafting";
	private static final int ENTRIES_PER_ROW = 2;

	private Table container = Widgets.table();
	private Table craftTable = Widgets.table();
	private AutoFocusScrollPane scroll = new AutoFocusScrollPane(craftTable);
	private Label titleLabel = Widgets.label(titleString);
	private Label unlockProgressLabel = Widgets.label();
	private TextButton backBtn = createCloseButton();
	private ItemSearchPanel searchPanel = new CraftItemSearchPanel(this);
	private float scrollX, scrollY;
	private int entries = 0;

	private int craftStationId;
	private List<Item> craftStationItems;

	public CraftDialog(int craftStationId) {
		clear();
		this.craftStationId = craftStationId;
		setBackground(GuiSkin.transparency());
		setFillParent(true);
		titleLabel.setAlignment(Align.center);
		Gui.setFontSize(titleLabel, Gui.FONT_M);
		container.setBackground(GuiSkin.darkBackground());
		craftTable.align(Align.center);
		titleLabel.setText(isPlayerCraftMenu() ? titleString : Objects.all().getName(craftStationId));
		backBtn.align(Align.center);
		unlockProgressLabel.setAlignment(Align.center);

		container.defaults().padBottom(TITLE_PADDING);
		container.add(titleLabel).padTop(TITLE_PADDING).align(Align.top).row();
		if (isPlayerCraftMenu())
			container.add(unlockProgressLabel).row();
		container.add(searchPanel).growX().row();
		container.add(scroll).grow().row();
		container.add(backBtn).size(BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT).align(Align.center).row();
		add(container).grow().row();

		createItemList();
		refreshCraftMenu();

		searchPanel.onChange(this::refresh);
		addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				scrollX = scroll.getScrollX();
				scrollY = scroll.getScrollY();
				Utils.dbg("Saving position %f %f", scrollX, scrollY);
				super.clicked(event, x, y);
			}
		});

		addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				event.cancel();
				return true;
			}

			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				event.cancel();
				return true;
			}
		});
	}

	private void createItemList() {
		craftStationItems = (isPlayerCraftMenu()
			? player().getCraftRecipes()
			: Items.all().craftStationRecipes().get(craftStationId))
				.stream().map(Items.all()::get).collect(Collectors.toList());

		/* If this dialog is opened from the InventoryUI, search for crafting stations
		 * on all tiles adjacent to player and append recipes from their recipe lists to
		 * the dialog's recipe list */
		if (isPlayerCraftMenu()) {
			player().getAdjacentObjects(CraftingStation.class).forEach(station -> {
				Items.all().craftStationRecipes().get(station.id())
					.stream()
					.map(Items.all()::get)
					.forEach(craftStationItems::add);
			});
		}
	}

	private List<Item> getItemList() {
		return craftStationItems;
	}

	public CraftDialog(InventoryUI inventoryUi) {
		this(0);
		Gui.setAction(backBtn, () -> {
			inventoryUi.refresh();
			inventoryUi.addAction(Actions.fadeIn(FADE_DURATION));
		});
	}

	@Override
	public void show() {
		super.show();
	}

	private boolean isPlayerCraftMenu() {
		return craftStationId == 0;
	}

	private Stream<Item> getCraftableItems() {
		return searchPanel.search(getItemList()).distinct();
	}

	private void refresh() {
		Gui.seamlessRefresh(craftTable, this::refreshCraftMenu, FADE_DELAY);
	}

	private void refreshCraftMenu() {
		entries = 0;
		Utils.out("Refreshing craft menu id: " + craftStationId);

		if (isPlayerCraftMenu())
			unlockProgressLabel.setText("Crafting recipes unlocked: " + player().craftRecipeProgress());

		if (getItemList().isEmpty())
			craftTable.add(Widgets.label("You don't know any crafting recipes."));

		getCraftableItems().forEach(item -> {
			boolean lastInRow = (entries + 1) % ENTRIES_PER_ROW == 0;
			craftTable.add(createEntry(item))
				.padRight(!lastInRow ? ENTRY_XPAD : 0)
				.padBottom(ENTRY_YPAD);

			if (lastInRow)
				craftTable.row();
			++entries;
		});

		craftTable.row();
		craftTable.pack();

		Utils.dbg("Restoring position %f %f", scrollX, scrollY);
		scroll.setScrollY(scrollY);
		scroll.setScrollX(scrollX);
		scroll.updateVisualScroll();
	}

	private final static Drawable entryBg = GuiSkin.getColorDrawable(new Color(0.25f, 0.25f, 0.25f, 0.5f));

	private Table createEntry(Item item) {
		var entry = Widgets.table();
		var craftButton = new CraftButton(item, this::refresh);
		var recipeLabel = Widgets.label(Item.createReadableItemList(Items.all().getCraftRecipe(item.id())));
		recipeLabel.setAlignment(Align.left);
		recipeLabel.setWrap(true);
		final float width = craftButton.getWidth();
		entry.add(craftButton).width(width).padRight(10);
		entry.add(recipeLabel).align(Align.left).width(width);
		entry.setBackground(entryBg);
		entry.pack();
		return entry;
	}
}
