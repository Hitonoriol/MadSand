package hitonoriol.madsand.entities.inventory.item;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Player;

public abstract class AbstractEquipment extends LevelBoundItem {
	public int hp = -1;
	public int maxHp = hp;
	public long uid = 0;
	public boolean cursed = false; 

	public AbstractEquipment(AbstractEquipment protoItem) {
		super(protoItem);
		hp = protoItem.hp;
		maxHp = hp;
		uid = MadSand.world.itemCounter++;
	}

	public AbstractEquipment() {
		super();
	}

	@Override
	public void use(Player player) {
		super.useIfPossible(player, () -> equip(player));
	}

	boolean damage(int amt) {
		hp -= amt;
		return (hp <= 0);
	}

	boolean damage() {
		return damage(1);
	}

	@Override
	public Item reinit() {
		return this;
	}

	@JsonIgnore
	public float getHpPercent() {
		return 100 * ((float) hp / (float) maxHp);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(25703, 89611).append(super.hashCode()).append(uid).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;

		return uid == ((AbstractEquipment) obj).uid;
	}
}
