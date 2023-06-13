package hitonoriol.madsand.entities.npc;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gamecontent.NpcDescriptor;
import hitonoriol.madsand.util.Utils;

public class Npc extends AbstractNpc {
	public boolean canTrade = false;

	public Npc(NpcDescriptor protoNpc) {
		super(protoNpc);
		canTrade = protoNpc.canTrade;

		if (stats.faction.isHuman())
			rollQuestGiveAbility();
	}

	public Npc() {
	}

	@Override
	protected void despawnProcess() {
		if (!stats().luckRoll())
			damage(0.025f);
		Utils.dbg("Despawning {%s}", this);
	}

	@Override
	public void interact(Player player) {
		player.interact(this);
		addLifetime();
	}

	@Override
	public String interactButtonString() {
		if (stats.faction.isHuman())
			return "Talk to ";
		else
			return super.interactButtonString();
	}
}
