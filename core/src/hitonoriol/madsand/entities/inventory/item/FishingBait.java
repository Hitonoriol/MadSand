package hitonoriol.madsand.entities.inventory.item;

public class FishingBait extends LevelBoundItem {
	public FishingBait(FishingBait protoItem) {
		super(protoItem);
	}
	
	@Override
	public FishingBait copy() {
		return new FishingBait(this);
	}
}
