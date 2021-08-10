package hitonoriol.madsand.entities.inventory.item;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.badlogic.gdx.graphics.Color;
import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gfx.ConditionalEffects;
import hitonoriol.madsand.gfx.Effects;
import hitonoriol.madsand.util.Functional;
import hitonoriol.madsand.util.Utils;

public abstract class AbstractEquipment extends LevelBoundItem {
	public int hp = -1;
	public int maxHp = hp;
	public long uid = 0;
	private boolean cursed, identified;

	public static ConditionalEffects<AbstractEquipment> textureFx = ConditionalEffects.create(fx -> fx
			.addEffect(equipment -> Utils.percentRoll(fx.random(), 40),
					item -> Effects.colorize(Utils.randomColor(fx.random())))

			.addEffect(equipment -> Utils.percentRoll(fx.random(), 5),
					item -> Effects.colorInversion)

			.addEffect(equipment -> equipment.cursed,
					item -> Effects.colorize(Color.RED)));

	public AbstractEquipment(AbstractEquipment protoItem) {
		super(protoItem);
		hp = protoItem.hp;
		maxHp = hp;

		if (protoItem.isProto()) {
			uid = MadSand.world().itemCounter().incrementAndGet();
			cursed = Utils.percentRoll(7.5);
			identified = Utils.percentRoll(15);
		} else {
			uid = protoItem.uid;
			cursed = protoItem.cursed;
			identified = protoItem.identified;
		}
	}

	public AbstractEquipment() {}

	@Override
	public Item setQuantity(int quantity) {
		this.quantity = 1;
		return this;
	}

	public void identify() {
		if (!identified)
			identified = true;
	}

	public boolean identified() {
		return identified;
	}

	@Override
	public void use(Player player) {
		useIfPossible(player, () -> equip(player));
	}

	@Override
	public void leftClickAction() {
		useIfPossible(MadSand.player(), () -> toggleEquipped());
	}

	public boolean damage(int amt) {
		hp -= amt;
		return (hp <= 0);
	}

	public boolean damage() {
		return damage(1);
	}

	public boolean cantBeDropped() {
		return cursed && hp > 0;
	}

	@Override
	public void refreshTextureEffects() {
		super.refreshTextureEffects();
		textureFx.apply(this);
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
		if (!(obj instanceof AbstractEquipment))
			return false;

		if (!super.equals(obj))
			return false;

		return uid == ((AbstractEquipment) obj).uid;
	}

	@Override
	public String getFullName() {
		return (identified && cursed ? "[RED]Cursed[] " : "")
				+ super.getFullName();
	}

	public static boolean isCursed(Item item) {
		return Functional.test(item.as(AbstractEquipment.class), eq -> eq.cantBeDropped());
	}
}
