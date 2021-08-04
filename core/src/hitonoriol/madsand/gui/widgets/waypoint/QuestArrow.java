package hitonoriol.madsand.gui.widgets.waypoint;

import com.badlogic.gdx.graphics.Color;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.entities.quest.Quest;

public class QuestArrow extends WaypointArrow {
	private Quest quest;

	public QuestArrow(Quest quest) {
		getArrow().setColor(Color.BLACK);
		this.quest = quest;
		setDestinationName(quest.name);
		update();
	}

	public Quest getQuest() {
		return quest;
	}

	@Override
	public void update() {
		if (!MadSand.player().getQuestWorker().isQuestInProgress(quest.id) || !quest.isComplete()) {
			remove();
			return;
		}

		AbstractNpc npc = quest.getNpc();
		if (!npc.isEmpty()) {
			setDestination(npc.x, npc.y);
			super.update();
		}
		else {
			Pair curWCoords = MadSand.world().getCurWPos();
			setDestination(quest.npcWorldPos);
			update(curWCoords.x, curWCoords.y);
		}
	}
}
