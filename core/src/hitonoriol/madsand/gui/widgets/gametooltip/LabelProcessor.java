package hitonoriol.madsand.gui.widgets.gametooltip;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

import hitonoriol.madsand.gui.textgenerator.TextGenerator;
import hitonoriol.madsand.util.If;

public class LabelProcessor {
	private boolean enabled = true;
	private List<RefreshableLabel> labels = new ArrayList<>();

	public void removeTextGenerator(TextGenerator generator) {
		labels.removeIf(label -> {
			boolean found = label.getGenerator() == generator;
			if (found)
				label.remove();
			return found;
		});
	}

	public RefreshableLabel addTextGenerator(int position, TextGenerator generator) {
		var label = new RefreshableLabel(generator);
		if (position < 0 || position >= labels.size())
			labels.add(label);
		else
			labels.add(position, label);
		return label;
	}

	public RefreshableLabel addTextGenerator(TextGenerator generator) {
		return addTextGenerator(-1, generator);
	}

	public void populateTable(Table table) {
		table.clear();
		labels.forEach(label -> If.then(label.getGenerator().isEnabled(), () -> table.add(label).row()));
	}

	public void refresh(int x, int y) {
		labels.forEach(label -> {
			if (label.getGenerator().textEmpty() || !label.isAutoUpdated() || !enabled)
				label.refresh(x, y);
		});
	}

	public void refresh() {
		refresh(0, 0);
	}

	public void setEnabled(boolean enabled) {
		labels.forEach(label -> label.getGenerator().setEnabled(this.enabled = enabled));
	}

	public boolean isEnabled() {
		return enabled;
	}

	public List<RefreshableLabel> getLabels() {
		return labels;
	}
}
