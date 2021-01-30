package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.Resources;

public class Projectile extends LevelBoundItem {
	public int dmg;
	public boolean thrownByHand = false;

	public Projectile(Projectile protoItem) {
		super(protoItem);
		dmg = protoItem.dmg;
		thrownByHand = protoItem.thrownByHand;
	}

	@Override
	public String getMiscInfo() {
		return "Projectile damage: [GREEN]" + dmg + Resources.COLOR_END + Resources.LINEBREAK;
	}
}
