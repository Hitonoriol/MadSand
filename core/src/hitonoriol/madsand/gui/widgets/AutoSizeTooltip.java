package hitonoriol.madsand.gui.widgets;

import java.util.function.Supplier;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Tooltip;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import hitonoriol.madsand.Gui;

public class AutoSizeTooltip extends Tooltip<Table> {
	private float WIDTH = 200f, HEIGHT = 50f, PAD = 7.5f;
	private final static Drawable background = Gui.getColorDrawable(new Color(0, 0, 0, 0.85f));

	private float maxWidth;
	private Table tooltipTbl = new Table();
	private Label tooltipLbl = new Label("", Gui.skin);
	private Supplier<String> updater;

	public AutoSizeTooltip() {
		super(null);
		tooltipLbl.setWrap(true);
		tooltipTbl.add(tooltipLbl).pad(PAD).width(WIDTH);
		tooltipTbl.row();

		tooltipTbl.setBackground(background);
		tooltipTbl.setSize(WIDTH, HEIGHT);
		setActor(tooltipTbl);
		setInstant(true);
		TooltipManager manager = super.getManager();
		manager.animations = false;
		manager.initialTime = 0;
		manager.resetTime = 0;
		manager.subsequentTime = 0;
	}

	public AutoSizeTooltip(Supplier<String> updater) {
		this();
		this.updater = updater;
		refresh();
	}

	public void setText(String text) {
		float txtWidth = Gui.getTextWidth(text);
		tooltipLbl.setText(text);
		tooltipTbl.getCell(tooltipLbl).width(maxWidth > 0 ? Math.min(maxWidth, txtWidth) : txtWidth);
	}

	public AutoSizeTooltip setMaxWidth(float maxWidth) {
		this.maxWidth = maxWidth;
		return this;
	}

	public void refresh() {
		if (updater != null)
			setText(updater.get());
	}

	public Label getLabel() {
		return tooltipLbl;
	}
}
