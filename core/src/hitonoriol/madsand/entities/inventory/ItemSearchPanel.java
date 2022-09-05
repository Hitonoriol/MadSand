package hitonoriol.madsand.entities.inventory;

import static hitonoriol.madsand.gui.Widgets.label;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.util.Functional;
import hitonoriol.madsand.util.TimeUtils;
import me.xdrop.fuzzywuzzy.FuzzySearch;

public class ItemSearchPanel extends Table {
	private static final float BOX_WIDTH = 150, BOX_HEIGHT = 30;
	private static final float PAD = 15, NAME_PAD = Gui.getTextWidth(" ");
	private static final int SEARCH_CUTOFF = 61;

	private Runnable onChange = () -> {};
	private TextField searchField = new TextField("", Gui.skin);
	private SelectBox<ItemSort> sortBox = new SelectBox<>(Gui.skin);
	private SelectBox<ItemSort.Order> orderBox = new SelectBox<>(Gui.skin);
	private SelectBox<ItemFilter> filterBox = new SelectBox<>(Gui.skin);
	private boolean vertical = false;

	public ItemSearchPanel(Actor parent, boolean vertical) {
		super(Gui.skin);
		this.vertical = vertical;
		searchField.setMessageText("Item name");
		defaults().align(Align.right);
		if (vertical)
			defaults().padBottom(5);
		else
			defaults().padRight(PAD);

		addEntry("Search:", searchField);
		Functional.with(addEntry("Sort:", sortBox), sort -> {
			if (!vertical)
				sort.padRight(0);
		});
		addEntry("", orderBox);
		addEntry("Filter:", filterBox);

		sortBox.setItems(ItemSort.getSortings());
		orderBox.setItems(ItemSort.Order.values());
		filterBox.setItems(ItemFilter.getFilters());

		searchField.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				event.cancel();
				return true;
			}

			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if (keycode == Keys.ESCAPE || keycode == Keys.ENTER)
					MadSand.getStage().setKeyboardFocus(null);
				event.cancel();
				return true;
			}
		});
		setUpTextFieldUnfocusers(parent);
	}

	public ItemSearchPanel(Actor parent) {
		this(parent, true);
	}

	private void setUpTextFieldUnfocusers(Actor parent) {
		parent.addListener(new InputListener() {
			/* Unfocus the item search text field on scroll */
			public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
				MadSand.getStage().setKeyboardFocus(null);
				return super.scrolled(event, x, y, amountX, amountY);
			}
		});

		parent.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (event.getTarget() != searchField)
					MadSand.getStage().setKeyboardFocus(null);
			}
		});
	}

	private Cell<Actor> addEntry(Actor leftActor, Actor rightActor, boolean leftToRight) {
		Actor searchComponent = leftToRight ? rightActor : leftActor;
		if (searchComponent instanceof TextField)
			searchComponent.addListener(new SearchQueryUpdater());
		else
			Gui.setAction(searchComponent, this::changed);
		add(leftActor).padRight(NAME_PAD);
		Cell<Actor> cell = add(rightActor).fillX().maxWidth(BOX_WIDTH).height(BOX_HEIGHT);
		if (vertical)
			row();
		return cell;
	}

	protected Cell<Actor> addEntry(String name, Actor entry) {
		return addEntry(label(name), entry, true);
	}

	protected Cell<Actor> addEntry(Actor entry, String name) {
		return addEntry(entry, label(name), false);
	}

	public Stream<Item> search(List<Item> list) {
		if (list.isEmpty())
			return Stream.empty();

		Stream<Item> items;
		String query = searchField.getText();
		boolean performSearch = !query.isEmpty();
		if (!performSearch)
			items = list.stream();
		else {
			items = FuzzySearch
					.extractSorted(query, list, item -> item.name(),
							query.length() > 2 ? SEARCH_CUTOFF : SEARCH_CUTOFF - 1)
					.stream()
					.map(result -> result.getReferent());
		}
		items = items.filter(getFilter());

		if (performSearch && getSort() == ItemSort.defaultSort)
			return items;

		return items.sorted(getSort());
	}

	public Comparator<Item> getSort() {
		return sortBox.getSelected().applyOrder(orderBox.getSelected());
	}

	public ItemFilter getFilter() {
		return filterBox.getSelected();
	}

	protected void changed() {
		onChange.run();
	}

	public void onChange(Runnable action) {
		onChange = action;
	}

	private class SearchQueryUpdater extends InputListener {
		private static final float DELAY = 0.2f;
		private Timer.Task updateTask = TimeUtils.createTask(ItemSearchPanel.this::changed);

		@Override
		public boolean keyTyped(InputEvent event, char character) {
			if (updateTask.isScheduled())
				updateTask.cancel();

			TimeUtils.scheduleTask(updateTask, DELAY);
			return super.keyTyped(event, character);
		}
	}
}
