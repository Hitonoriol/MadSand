package hitonoriol.madsand.entities.npc;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.properties.NpcContainer;

public class Npc extends AbstractNpc {
	public boolean canTrade = false;

	public Npc(NpcContainer protoNpc) {
		super(protoNpc);
		canTrade = protoNpc.canTrade;

		if (stats.faction.isHuman())
			rollQuestGiveAbility();
	}

	public Npc() {
		super();
	}

	@Override
	public void interact(Player player) {
		player.interact(this);
	}

	@Override
	public String interactButtonString() {
		if (stats.faction.isHuman())
			return "Talk to ";
		else
			return super.interactButtonString();
	}
}
