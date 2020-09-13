package hitonoriol.madsand.entities.quest;

import java.util.ArrayList;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.properties.QuestList;

public class QuestWorker {

	public HashSet<Integer> completedQuests = new HashSet<Integer>(); // sets of completed quests and the ones in progress
	public HashSet<Integer> questsInProgress = new HashSet<Integer>();
	private Player player;

	public QuestWorker(Player player) {
		setPlayer(player);
	}

	public QuestWorker() {
		this(null);
	}

	@JsonIgnore
	public void setPlayer(Player player) {
		this.player = player;
	}

	public boolean isQuestInProgress(int id) {
		return questsInProgress.contains(id);
	}

	public boolean isQuestCompleted(int id) {
		return completedQuests.contains(id);
	}

	private void startQuest(Quest quest) {
		Utils.out("Trying to start quest id" + quest.id);

		if (isQuestCompleted(quest.id))
			return;

		if (player.inventory.putItem(quest.giveItems) != -1)
			MadSand.print("You get " + Item.queryToName(quest.giveItems));

		questsInProgress.add(quest.id);
		GameDialog.generateDialogChain(quest.startMsg, Gui.overlay).show();
	}

	private void completeQuest(Quest quest) {
		MadSand.notice("You completed a quest!");

		player.inventory.delItem(quest.reqItems);
		player.inventory.delItem(quest.removeOnCompletion);
		player.inventory.putItem(quest.rewardItems);
		player.addExp(quest.exp);
		MadSand.notice("You get " + quest.exp + " EXP!");

		if (!quest.repeatable)
			completedQuests.add(quest.id);

		questsInProgress.remove(quest.id);
		GameDialog.generateDialogChain(quest.endMsg, Gui.overlay).show();
	}

	public void processQuest(int id) {
		Utils.out("Processing quest " + id);

		Quest quest = QuestList.quests.get(id);
		if (isQuestInProgress(id) && !isQuestCompleted(id)) {
			if (player.inventory.itemsExist(quest.reqItems))
				completeQuest(quest);
			else
				GameDialog.generateDialogChain(quest.reqMsg, Gui.overlay).show();
		} else
			startQuest(quest);
	}

	public int getAvailableQuest(ArrayList<Integer> quests) { // search NPC's quest list for {not yet started / not finished / repeatable} quests
		for (int qid : quests) {

			if (isQuestInProgress(qid) || !isQuestCompleted(qid))
				return qid;

		}
		return QuestList.NO_QUESTS_STATUS;
	}
}
