package hitonoriol.madsand.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.gui.textgenerator.TooltipTextGenerator;

public class GameTooltip extends Table {
	static float TOOLTIP_WIDTH = 500;
	static float TOOLTIP_XPAD = 20;
	static float TOOLTIP_HEIGHT = 300;
	static float TOOLTIP_YPAD = TOOLTIP_HEIGHT + 25;

	private List<TooltipLabel> labels = new ArrayList<>();

	private static GameTooltip instance = new GameTooltip();

	private GameTooltip() {
		super();
		super.setVisible(true);
		super.align(Align.topLeft);
		super.setOriginY(Align.topLeft);
		super.setHeight(TOOLTIP_HEIGHT);
		super.defaults().width(TOOLTIP_WIDTH).pad(1f).align(Align.topLeft);
		super.row();
	}

	public GameTooltip addTextGenerator(TooltipTextGenerator generator) {
		TooltipLabel label = new TooltipLabel(generator);
		labels.add(label);
		super.add(label).row();
		return this;
	}

	public void refresh(int x, int y) {
		labels.forEach(label -> label.refresh(x, y));
	}

	public void moveTo(int x, int y) {
		super.addAction(Actions.moveTo(x + TOOLTIP_XPAD, y - TOOLTIP_YPAD, 0.1F));
	}

	public void show() {
		setVisible(true);
	}

	public void hide() {
		setVisible(false);
	}

	public static GameTooltip instance() {
		return instance;
	}
}
