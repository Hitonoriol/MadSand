package hitonoriol.madsand.entities.inventory.item;

import java.util.Random;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.badlogic.gdx.graphics.Color;
import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gfx.ConditionalEffects;
import hitonoriol.madsand.gfx.Effects;

public abstract class AbstractEquipment extends LevelBoundItem {
	public int hp = -1;
	public int maxHp = hp;
	public long uid = 0;
	public boolean cursed = false;

	public static ConditionalEffects<AbstractEquipment> equipEffects = new ConditionalEffects<>((effects, random) -> {
		effects.put(equipment -> Utils.percentRoll(random, 90),
				Effects.colorize(new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1)));
		effects.put(equipment -> Utils.percentRoll(random, 5), Effects.colorInversion);
		effects.put(equipment -> equipment.cursed, Effects.colorize(Color.RED));
	});

	public AbstractEquipment(AbstractEquipment protoItem) {
		super(protoItem);
		hp = protoItem.hp;
		maxHp = hp;

		if (uid == 0) {
			uid = ++MadSand.world.itemCounter;
			cursed = Utils.percentRoll(7.5);
		} else {
			uid = protoItem.uid;
			cursed = protoItem.cursed;
		}
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
	
	public boolean cantBeDropped() {
		return cursed && hp > 0;
	}

	public Random itemRandom() {
		return new Random(uid + Utils.val(cursed) + lvl);
	}

	@Override
	public void refreshTextureEffects() {
		super.refreshTextureEffects();
		equipEffects.apply(this);
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
