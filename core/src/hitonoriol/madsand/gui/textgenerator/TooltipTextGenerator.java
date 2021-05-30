package hitonoriol.madsand.gui.textgenerator;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.util.Utils;

public abstract class TooltipTextGenerator {
	private String text = "foo";
	protected final StringBuilder builder = new StringBuilder();
	private boolean enabled = true;

	public abstract void update(int x, int y);

	public String getText() {
		if (builder.indexOf(text) != -1)
			return text;

		return text = builder.toString();
	}

	public void setText(String text) {
		clearBuilder();
		builder.append(text);
	}

	public TooltipTextGenerator addLine(String line) {
		builder.append(line).append(Resources.LINEBREAK);
		return this;
	}

	public TooltipTextGenerator addLine(String line, Object... args) {
		return addLine(String.format(line, args));
	}

	protected void clearBuilder() {
		Utils.clearBuilder(builder);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
