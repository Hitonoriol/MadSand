package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.properties.CropContainer;

public class CropSeeds extends PlaceableItem {
	public CropContainer cropContainer;
	
	public CropSeeds(CropSeeds protoItem) {
		super(protoItem);
		cropContainer = protoItem.cropContainer;
	}
	
	public CropSeeds() {
		super();
	}
	
	@Override
	public CropSeeds copy() {
		return new CropSeeds(this);
	}
	
	@Override
	public void use(Player player) {
		player.useItem(this);
	}
}
