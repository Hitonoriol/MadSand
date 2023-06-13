package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.resources.Resources;

public class Consumable extends Item {
	public int satiationAmount;
	public int healAmount;
	public int staminaAmount;

	public Consumable(Consumable protoItem) {
		super(protoItem);
		healAmount = protoItem.healAmount;
		satiationAmount = protoItem.satiationAmount;
		staminaAmount = protoItem.staminaAmount;
	}

	public Consumable() {
	}

	@Override
	public Consumable copy() {
		return new Consumable(this);
	}

	@Override
	public void use(Player player) {
		super.use(player);
		player.useItem(this);
	}

	public int getNutritionalValue() {
		return (int) (0.125f * satiationAmount + 0.275f * healAmount + 0.525f * staminaAmount);
	}

	@Override
	public String getMiscInfo() {
		var info = "Satiation: " + satiationAmount + Resources.LINEBREAK;
		info += "Health: " + healAmount + Resources.LINEBREAK;
		info += "Stamina: " + staminaAmount + Resources.LINEBREAK;
		info += "Nutritional value: " + getNutritionalValue() + Resources.LINEBREAK;
		return info;
	}
}
