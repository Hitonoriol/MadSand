package hitonoriol.madsand.gui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.HotbarAssignable;
import hitonoriol.madsand.gui.OverlayMouseoverListener;

public class Hotbar extends Table {
	static float E_WIDTH = 175, HEIGHT = OverlayBottomMenu.HEIGHT;

	private List<Entry> hotEntries = new ArrayList<>();

	Table container = new Table();

	public Hotbar() {
		super();

		AutoFocusScrollPane scroll = new AutoFocusScrollPane(container);
		scroll.setScrollingDisabled(false, true);
		super.add(scroll).size(Gdx.graphics.getWidth(), HEIGHT);

		container.defaults().padRight(5);
		container.align(Align.left);
		super.align(Align.bottomLeft);
		super.pack();
		super.moveBy(0, HEIGHT + OverlayBottomMenu.BUTTON_PADDING * 2);
	}

	public void addEntry(HotbarAssignable hotAssignable) {
		if (getEntry(hotAssignable).isPresent())
			return;

		Entry entry = new Entry(hotAssignable);
		hotEntries.add(entry);
		container.add(entry.button).padLeft(5).size(E_WIDTH, HEIGHT);
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
		hotEntries.forEach(entry -> entry.refresh());
	}

	private static class Entry {
		HotbarAssignable item;
		TextButton button;

		public Entry(HotbarAssignable item) {
			this.item = item;
			button = new TextButton(item.getHotbarString(), Gui.skin);
			button.addListener(OverlayMouseoverListener.instance());
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
