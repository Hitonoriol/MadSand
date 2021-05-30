package hitonoriol.madsand.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.gui.textgenerator.TooltipTextGenerator;
import hitonoriol.madsand.util.Utils;

public class GameTooltip extends Table {
	static float TOOLTIP_WIDTH = 500;
	static float TOOLTIP_XPAD = TOOLTIP_WIDTH / 2 + 20;
	static float TOOLTIP_HEIGHT = 300;
	static float TOOLTIP_YPAD = TOOLTIP_HEIGHT / 2 + 25;

	private StringBuilder genText = new StringBuilder();
	private List<TooltipTextGenerator> textGenerators = new ArrayList<>();
	private Label tooltipLabel;

	private static GameTooltip instance = new GameTooltip();

	private GameTooltip() {
		super();
		super.setVisible(true);
		tooltipLabel = new Label("", Gui.skin);
		tooltipLabel.setWrap(true);
		tooltipLabel.setOriginY(Align.topLeft);
		tooltipLabel.setAlignment(Align.topLeft);
		super.setOriginY(Align.topLeft);
		super.add(tooltipLabel).size(TOOLTIP_WIDTH, TOOLTIP_HEIGHT).align(Align.topLeft);
		super.row();
	}

	public GameTooltip addTextGenerator(TooltipTextGenerator generator) {
		textGenerators.add(generator);
		return this;
	}

	public void refresh(int x, int y) {
		Utils.clearBuilder(genText);
		textGenerators.stream()
				.filter(generator -> generator.isEnabled())
				.forEach(generator -> {
					generator.update(x, y);
					genText.append(generator.getText()).append(Resources.LINEBREAK);
				});
		tooltipLabel.setText(genText.toString());
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
