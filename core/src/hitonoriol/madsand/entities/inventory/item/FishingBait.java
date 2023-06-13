package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.equipment.EquipSlot;

public class FishingBait extends LevelBoundItem {
	public FishingBait(FishingBait protoItem) {
		super(protoItem);
	}

	public FishingBait() {
	}

	@Override
	public FishingBait copy() {
		return new FishingBait(this);
	}

	@Override
	public void use(Player player) {
		toggleEquipped();
	}

	@Override
	public EquipSlot getEquipSlot() {
		return EquipSlot.Offhand;
	}
}
