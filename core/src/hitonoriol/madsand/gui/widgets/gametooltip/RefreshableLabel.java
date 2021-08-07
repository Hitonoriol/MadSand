package hitonoriol.madsand.gui.widgets.gametooltip;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.gui.textgenerator.TextGenerator;
import hitonoriol.madsand.util.TimeUtils;

public class RefreshableLabel extends Label {
	private final TextGenerator generator;
	private Cell<RefreshableLabel> parentCell;
	private boolean visible = true;
	private Timer.Task updateTask;

	public RefreshableLabel(TextGenerator generator) {
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

	public void refresh() {
		refresh(0, 0);
	}

	private Cell<RefreshableLabel> getParentCell() {
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

	public TextGenerator getGenerator() {
		return generator;
	}

	public RefreshableLabel update(float periodSeconds) {
		TimeUtils.scheduleRepeatingTask(updateTask = TimeUtils.createTask(() -> refresh()), periodSeconds);
		return this;
	}
	
	public boolean isAutoUpdated() {
		return updateTask != null;
	}

	@Override
	public boolean remove() {
		if (isAutoUpdated())
			updateTask.cancel();
		return super.remove();
	}
}
