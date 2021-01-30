package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.properties.CropContainer;

public class CropSeeds extends Placeable {
	public CropContainer cropContainer;
	
	public CropSeeds(CropSeeds protoItem) {
		super(protoItem);
		cropContainer = protoItem.cropContainer;
	}
	
	@Override
	public CropSeeds copy() {
		return new CropSeeds(this);
	}
}
