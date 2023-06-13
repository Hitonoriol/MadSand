package hitonoriol.madsand.entities.ability;

import java.util.Optional;

import com.badlogic.gdx.Input.Keys;

import hitonoriol.madsand.HotbarAssignable;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.resources.Resources;

public class ActiveAbility extends Ability implements HotbarAssignable {

	public float staminaCost, tmpBonus;
	public Type type;

	@Override
	public void apply() {
		var player = MadSand.player();
		if (player.stats.stamina < staminaCost) {
			MadSand.warn("You don't have enough stamina to use this ability!");
			return;
		}

		MadSand.notice((type == Type.Oneshot ? "You use " : "You activate") + name);

		super.apply();
	}

	public float getStaminaCost() {
		float cost = staminaCost + tmpBonus;
		tmpBonus = 0;
		return cost;
	}

	public ActiveAbility addBonusCost(float cost) {
		tmpBonus = cost;
		return this;
	}

	@Override
	public String toString() {
		return super.toString() + Resources.LINEBREAK
			+ "(" + staminaCost + " stamina)";
	}

	public String getBindKeyString() {
		return "[" + Optional.of(MadSand.player().getAbilityKey(id))
			.filter(key -> key != -1)
			.map(Keys::toString)
			.orElse("None") + "]";
	}

	public enum Type {
		Oneshot, Toggleable
	}

	@Override
	public String getHotbarString() {
		return toString() + Resources.LINEBREAK + getBindKeyString();
	}

	@Override
	public void hotbarAction() {
		apply();
	}
}
