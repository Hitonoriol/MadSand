package hitonoriol.madsand.gui.widgets.overlay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.HotbarAssignable;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gui.OverlayMouseoverListener;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.util.Functional;

public class Hotbar extends Table {
	static float E_WIDTH = 175, HEIGHT = OverlayBottomMenu.HEIGHT + Gui.FONT_XXS;
	static final String NO_BIND = "None";

	private List<Entry> hotEntries = new ArrayList<>();

	Table container = new Table();

	public Hotbar() {
		super();

		AutoFocusScrollPane scroll = new AutoFocusScrollPane(container);
		scroll.setScrollingDisabled(false, true);
		super.add(scroll).size(Gdx.graphics.getWidth(), HEIGHT + Gui.FONT_XXS);

		container.defaults().padRight(5).padLeft(5).size(E_WIDTH, HEIGHT);
		container.align(Align.left);
		super.align(Align.bottomLeft);
		super.setBackground(Gui.darkBackgroundSizeable);
		super.pack();
		super.moveBy(0, OverlayBottomMenu.HEIGHT + OverlayBottomMenu.BUTTON_PADDING * 2);
		container.moveBy(5, 0);
	}

	public void refreshVisibility() {
		boolean visible = super.isVisible();
		boolean noAbilities = MadSand.player().getAbilities().isEmpty();
		if (visible && noAbilities)
			super.setVisible(false);
		else if (!visible && !noAbilities)
			super.setVisible(true);
	}

	public void addEntry(HotbarAssignable hotAssignable) {
		if (getEntry(hotAssignable).isPresent())
			return;

		Entry entry = new Entry(hotAssignable, this);
		hotEntries.add(entry);
		container.add(entry.button);
	}

	public void removeEntry(HotbarAssignable hotAssignable) {
		getEntry(hotAssignable)
				.ifPresent(entry -> {
					hotEntries.remove(entry);
					container.removeActor(entry.button);
				});
	}

	Optional<Entry> getEntry(HotbarAssignable hotAssignable) {
		return hotEntries.stream()
				.filter(entry -> entry.item == hotAssignable)
				.findFirst();
	}

	public void refresh() {
		MutableBoolean layoutChanged = new MutableBoolean(false);
		Iterator<Entry> it = hotEntries.iterator();
		while (it.hasNext()) {
			Functional.with(it.next(), entry -> {
				entry.refresh();
				if (entry.button.getText().toString().contains(NO_BIND)) {
					it.remove();
					layoutChanged.setTrue();
				}
			});
		}

		if (layoutChanged.isTrue()) {
			container.clear();
			hotEntries.forEach(entry -> container.add(entry.button));
		}
		refreshVisibility();
	}

	private static class Entry {
		HotbarAssignable item;
		TextButton button;

		public Entry(HotbarAssignable item, Hotbar hotbar) {
			this.item = item;
			button = new TextButton(item.getHotbarString(), Gui.skin);
			button.addListener(new OverlayMouseoverListener());
			Gui.setFontSize(button.getLabel(), Gui.FONT_XXS);
			Gui.setAction(button, () -> item.hotbarAction());
		}

		public void refresh() {
			String hotStr;
			if (!button.getLabel().textEquals(hotStr = item.getHotbarString()))
				button.setText(hotStr);
		}
	}
}
