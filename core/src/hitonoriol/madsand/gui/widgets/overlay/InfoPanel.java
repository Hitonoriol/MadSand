package hitonoriol.madsand.gui.widgets.overlay;

import java.util.function.Supplier;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.MouseoverListener;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.textgenerator.StaticTextGenerator;
import hitonoriol.madsand.gui.textgenerator.TextGenerator;
import hitonoriol.madsand.gui.widgets.gametooltip.LabelProcessor;
import hitonoriol.madsand.gui.widgets.gametooltip.RefreshableLabel;

public class InfoPanel extends ScrollablePanel {
	private final static float ENTRY_PAD = 15;

	private LabelProcessor processor = new LabelProcessor();
	private Label title = Widgets.label("", Gui.FONT_M);
	private Label subTitle = Widgets.label();

	public InfoPanel(String title) {
		this();
		setTitle(title);
	}

	public InfoPanel() {
		add(createToggleButton())
			.width(WIDTH)
			.align(Align.center)
			.padBottom(1).row();
		addBody();
		updateSubTitle();
	}

	public void setTitle(String title) {
		this.title.setText(title);
	}

	public void setSubTitle(String subTitle) {
		this.subTitle.setText(subTitle);
	}

	private TextButton createToggleButton() {
		var button = Widgets.button();
		title.setAlignment(Align.center);
		button.clearChildren();
		button.add(title).expand().fill().row();
		subTitle.setAlignment(Align.center);
		button.add(subTitle).expand().fill().align(Align.center).row();

		var style = new TextButtonStyle(button.getStyle());
		style.up = style.over = style.down = style.checked = getContentTable().getBackground();
		button.setStyle(style);

		MouseoverListener.setUp(button);
		Gui.setAction(button, this::toggleEnabled);
		return button;
	}

	private void addBody() {
		add(getScroll()).maxSize(WIDTH, HEIGHT).minHeight(1).padTop(-1).row();
		setSize(WIDTH, HEIGHT);
		pack();
	}

	private void updateSubTitle() {
		subTitle.setText("Click to " + (processor.isEnabled() ? "hide" : "expand"));
	}

	public RefreshableLabel addEntry(TextGenerator generator) {
		var label = processor.addTextGenerator(generator);
		addContents(label).padLeft(ENTRY_PAD).row();
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
		updateSubTitle();
		processor.refresh();

		if (!enabled) {
			Gui.removeActor(this, getScroll());
		} else
			addBody();
	}

	public LabelProcessor getProcessor() {
		return processor;
	}
}
