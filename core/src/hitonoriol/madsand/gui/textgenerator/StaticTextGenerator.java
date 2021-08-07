package hitonoriol.madsand.gui.textgenerator;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class StaticTextGenerator extends TextGenerator {
	private BiFunction<Integer, Integer, String> textUpdater;

	protected StaticTextGenerator() {}

	public StaticTextGenerator(String text) {
		super.setText(text);
	}

	public StaticTextGenerator(BiFunction<Integer, Integer, String> updateAction) {
		this.textUpdater = updateAction;
	}

	public StaticTextGenerator(Supplier<String> updateAction) {
		this.textUpdater = (x, y) -> updateAction.get();
	}

	@Override
	public void update(int x, int y) {
		if (textUpdater != null)
			super.setText(textUpdater.apply(x, y));
	}

	public void update() {
		update(0, 0);
	}
}
