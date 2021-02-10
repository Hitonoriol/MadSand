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
		if (player.stats.stamina >= staminaCost)
			return;

		MadSand.notice((type == Type.Oneshot ? "You use " : "You activate") + name);

		player.changeStamina(-staminaCost);
		super.apply();
	}

	public static enum Type {
		Oneshot, Toggleable
	}
}
