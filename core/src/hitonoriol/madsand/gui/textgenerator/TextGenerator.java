package hitonoriol.madsand.gui.textgenerator;

import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Strings;

public abstract class TextGenerator {
	private String text = "foo";
	protected final StringBuilder builder = new StringBuilder();
	private boolean enabled = true;

	public abstract void update(int x, int y);

	public String getText() {
		if (Strings.builderEquals(builder, text))
			return text;

		return text = builder.toString();
	}

	public void setText(String text) {
		clearBuilder();
		builder.append(text);
		this.text = text;
	}

	public TextGenerator addLine(String line) {
		builder.append(line).append(Resources.LINEBREAK);
		return this;
	}

	public TextGenerator addLine(String line, Object... args) {
		return addLine(String.format(line, args));
	}

	protected void clearBuilder() {
		Strings.clearBuilder(builder);
	}
	
	protected void clear() {
		setText("");
	}
	
	public boolean textEmpty() {
		return text.isEmpty() && builder.length() == 0;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (!enabled)
			clear();
	}
}
