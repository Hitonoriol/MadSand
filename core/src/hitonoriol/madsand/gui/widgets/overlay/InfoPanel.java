package hitonoriol.madsand.gui.widgets.overlay;

import java.util.function.Supplier;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.gui.textgenerator.StaticTextGenerator;
import hitonoriol.madsand.gui.textgenerator.TextGenerator;
import hitonoriol.madsand.gui.widgets.gametooltip.LabelProcessor;
import hitonoriol.madsand.gui.widgets.gametooltip.RefreshableLabel;

public class InfoPanel extends ScrollablePanel {
	private LabelProcessor processor = new LabelProcessor();
	private Table title = new Table();
	private Label expandLbl = new Label("", Gui.skin);

	public InfoPanel() {
		Label titleLbl = Gui.setFontSize(new Label("Info", Gui.skin), Gui.FONT_M);
		titleLbl.setAlignment(Align.center);
		title.add(titleLbl).row();
		title.background(getContentTable().getBackground());
		add(title).width(WIDTH).fill().align(Align.center).padBottom(1).row();
		expandLbl.setAlignment(Align.center);
		title.add(expandLbl).fill().align(Align.center).row();
		addBody();
		updateExpandLbl();

		Gui.setClickAction(this, () -> {
			toggleEnabled();
		});
	}

	private void addBody() {
		add(getScroll()).minHeight(1).maxHeight(HEIGHT).padTop(-1).row();
		setSize(WIDTH, HEIGHT);
		pack();
	}

	private void updateExpandLbl() {
		expandLbl.setText("Click to " + (processor.isEnabled() ? "hide" : "expand"));
	}

	public RefreshableLabel addEntry(TextGenerator generator) {
		RefreshableLabel label;
		addContents(label = processor.addTextGenerator(generator)).row();
		return label;
	}

	public RefreshableLabel addEntry(Supplier<String> stringGen) {
		return addEntry(new StaticTextGenerator(stringGen));
	}

	public void refresh() {
		processor.refresh();
	}

	public void toggleEnabled() {
		boolean enabled = !processor.isEnabled();
		processor.setEnabled(enabled);
		updateExpandLbl();
		processor.refresh();

		if (!enabled) {
			Gui.removeActor(this, getScroll());
			Gui.resumeGameFocus();
		} else
			addBody();
	}

	public LabelProcessor getProcessor() {
		return processor;
	}
}
