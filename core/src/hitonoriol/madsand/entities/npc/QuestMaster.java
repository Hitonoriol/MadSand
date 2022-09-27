package hitonoriol.madsand.entities.npc;

import java.util.ArrayList;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gamecontent.NpcDescriptor;

public class QuestMaster extends AbstractNpc {
	public ArrayList<Integer> questList = new ArrayList<Integer>();

	public QuestMaster(NpcDescriptor protoNpc) {
		super(protoNpc);
		questList = new ArrayList<>(protoNpc.questList);
		canGiveQuests = true;
	}

	public QuestMaster() {
		super();
	}
	
	@Override
	public void interact(Player player) {
		player.interact(this);
	}
}
