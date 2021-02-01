package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.entities.Player;

public class FishingBait extends LevelBoundItem {
	public FishingBait(FishingBait protoItem) {
		super(protoItem);
	}
	
	public FishingBait() {
		super();
	}

	@Override
	public FishingBait copy() {
		return new FishingBait(this);
	}

	@Override
	public void use(Player player) {
		equip(player);
	}
}
