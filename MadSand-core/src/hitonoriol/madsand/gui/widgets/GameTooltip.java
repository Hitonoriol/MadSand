package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import hitonoriol.madsand.Gui;

public class GameTooltip extends Table {

	public Label tooltipLabel;

	public GameTooltip() {
		super();
		super.setVisible(true);
		tooltipLabel = new Label("", Gui.skin);
		super.add(tooltipLabel).width(100.0F);
		super.row();
	}
}
