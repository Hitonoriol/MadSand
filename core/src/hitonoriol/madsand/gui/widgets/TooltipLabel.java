package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.gui.textgenerator.TooltipTextGenerator;

public class TooltipLabel extends Label {
	private TooltipTextGenerator generator;
	private Cell<TooltipLabel> parentCell;
	private boolean visible = true;

	public TooltipLabel(TooltipTextGenerator generator) {
		super("", Gui.skin);
		this.generator = generator;
		setWrap(true);
		setOriginY(Align.topLeft);
		setAlignment(Align.topLeft);
	}

	public void refresh(int x, int y) {
		if (!generator.isEnabled()) {
			if (visible)
				hide();
			return;
		}

		if (!visible && generator.isEnabled())
			show();

		generator.update(x, y);
		setText(generator.getText());
	}

	private Cell<TooltipLabel> getParentCell() {
		if (parentCell == null)
			parentCell = ((Table) getParent()).getCell(this);

		return parentCell;
	}

	public void hide() {
		getParentCell().clearActor();
		visible = false;
	}

	public void show() {
		getParentCell().setActor(this);
		visible = true;
	}

	public TooltipTextGenerator getGenerator() {
		return generator;
	}
}
