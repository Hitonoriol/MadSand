package hitonoriol.madsand.gui.textgenerator;

import java.util.function.BiFunction;

public class StaticTextGenerator extends TooltipTextGenerator {
	private BiFunction<Integer, Integer, String> textUpdater;

	public StaticTextGenerator(String text) {
		super.setText(text);
	}

	public StaticTextGenerator() {}

	public StaticTextGenerator(BiFunction<Integer, Integer, String> updateAction) {
		this.textUpdater = updateAction;
	}

	@Override
	public void update(int x, int y) {
		if (textUpdater != null)
			super.setText(textUpdater.apply(x, y));
	}
}
