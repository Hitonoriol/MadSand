package hitonoriol.madsand.entities.ability;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.world.World;

public class ActiveAbility extends Ability {

	public int staminaCost;
	public Type type;

	@Override
	public void apply() {
		Player player = World.player;
		if (player.stats.stamina < staminaCost) {
			MadSand.warn("You don't have enough stamina to use this ability!");
			return;
		}

		MadSand.notice((type == Type.Oneshot ? "You use " : "You activate") + name);

		super.apply();
	}

	public static enum Type {
		Oneshot, Toggleable
	}
}
