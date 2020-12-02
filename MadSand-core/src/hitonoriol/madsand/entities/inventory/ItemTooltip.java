package hitonoriol.madsand.entities.inventory;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Tooltip;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;

import hitonoriol.madsand.Gui;

public class ItemTooltip extends Tooltip<Table>{
	
	private float TOOLTIP_WIDTH = 200F;
	private float TOOLTIP_HEIGHT = 50F;
	
	private Table tooltipTbl;
	private Label itemInfoLbl;
	
	public ItemTooltip(Item item) {
		super(null);
		
		tooltipTbl = new Table();
		itemInfoLbl = new Label(item.getInfoString(), Gui.skin);
		itemInfoLbl.setWrap(true);

		tooltipTbl.add(itemInfoLbl).width(TOOLTIP_WIDTH);
		tooltipTbl.row();

		tooltipTbl.setBackground(Gui.darkBackgroundSizeable);
		tooltipTbl.setSize(TOOLTIP_WIDTH, TOOLTIP_HEIGHT);

		super.setActor(tooltipTbl);
		super.setInstant(true);
		
		TooltipManager manager = super.getManager();
		manager.animations = false;
		manager.initialTime = 0;
		manager.resetTime = 0;
		manager.subsequentTime = 0;
	}

}
