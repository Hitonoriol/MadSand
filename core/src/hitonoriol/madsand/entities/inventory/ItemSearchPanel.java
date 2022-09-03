package hitonoriol.madsand.entities.inventory;

import java.util.Comparator;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.Gui;

public class ItemSearchPanel extends Table {
	private static final float BOX_WIDTH = 150, BOX_HEIGHT = 30;
	private static final float PAD = 15;

	private Runnable onChange;
	private SelectBox<ItemSort> sortBox = new SelectBox<>(Gui.skin);
	private SelectBox<ItemSort.Order> orderBox = new SelectBox<>(Gui.skin);
	private SelectBox<ItemFilter> filterBox = new SelectBox<>(Gui.skin);
	private boolean vertical = false;

	public ItemSearchPanel(boolean vertical) {
		super(Gui.skin);
		this.vertical = vertical;
		defaults().align(Align.right);
		if (vertical)
			defaults().padBottom(5);
		else
			defaults().padRight(PAD);

		addEntry("Sort: ", sortBox);
		if (vertical)
			addEntry("", orderBox);
		else
			add(orderBox).size(BOX_WIDTH / 2, BOX_HEIGHT);
		addEntry("Filter: ", filterBox);

		sortBox.setItems(ItemSort.getSortings());
		orderBox.setItems(ItemSort.Order.values());
		filterBox.setItems(ItemFilter.getFilters());
	}

	private void addEntry(String name, Actor entry) {
		add(name);
		add(entry).size(BOX_WIDTH, BOX_HEIGHT);
		if (vertical)
			row();
	}

	public Comparator<Item> getSort() {
		return sortBox.getSelected().applyOrder(orderBox.getSelected());
	}

	public ItemFilter getFilter() {
		return filterBox.getSelected();
	}

	public void onChange(Runnable action) {
		onChange = action;
		Gui.setAction(sortBox, onChange);
		Gui.setAction(orderBox, onChange);
		Gui.setAction(filterBox, onChange);
	}
}
