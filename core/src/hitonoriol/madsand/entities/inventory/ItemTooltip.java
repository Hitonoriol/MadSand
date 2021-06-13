package hitonoriol.madsand.entities.inventory;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Tooltip;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.entities.inventory.item.Item;

public class ItemTooltip extends Tooltip<Table> {

	private float TOOLTIP_WIDTH = 200F;
	private float TOOLTIP_HEIGHT = 50F;

	private Table tooltipTbl;
	private Label itemInfoLbl;
	private Item item;

	public ItemTooltip(Item item) {
		super(null);
		this.item = item;

		tooltipTbl = new Table();
		itemInfoLbl = new Label("", Gui.skin);
		itemInfoLbl.setWrap(true);

		tooltipTbl.add(itemInfoLbl).width(TOOLTIP_WIDTH);
		tooltipTbl.row();

		tooltipTbl.setBackground(Gui.darkBackgroundSizeable);
		tooltipTbl.setSize(TOOLTIP_WIDTH, TOOLTIP_HEIGHT);

		super.setActor(tooltipTbl);
		super.setInstant(true);
		refresh();

		TooltipManager manager = super.getManager();
		manager.animations = false;
		manager.initialTime = 0;
		manager.resetTime = 0;
		manager.subsequentTime = 0;
	}

	public void refresh() {
		itemInfoLbl.setText(item.getInfoString());
	}

	@Override
	public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
		refresh();
		super.enter(event, x, y, pointer, fromActor);
	}
}
