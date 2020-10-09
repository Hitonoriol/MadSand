package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;

public class GameTooltip extends Table {
	static float TOOLTIP_WIDTH = 500;
	static float TOOLTIP_XPAD = TOOLTIP_WIDTH / 2 + 20;
	static float TOOLTIP_HEIGHT = 300;
	static float TOOLTIP_YPAD = TOOLTIP_HEIGHT / 2 + 25;

	public Label tooltipLabel;

	public GameTooltip() {
		super();
		super.setVisible(true);
		tooltipLabel = new Label("", Gui.skin);
		tooltipLabel.setWrap(true);
		tooltipLabel.setOriginY(Align.topLeft);
		tooltipLabel.setAlignment(Align.topLeft);
		super.setOriginY(Align.topLeft);
		super.add(tooltipLabel).width(TOOLTIP_WIDTH).height(TOOLTIP_HEIGHT).align(Align.topLeft);
		super.row();
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
}
