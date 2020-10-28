package hitonoriol.madsand.entities.quest;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.gui.dialogs.QuestListDialog;
import hitonoriol.madsand.properties.QuestList;

public class QuestWorker {

	public int lastProceduralQuest = 0; // Decrements for each new procedural quest
	public ArrayList<ProceduralQuest> proceduralQuests = new ArrayList<>();
	public ArrayList<Quest> completedQuests = new ArrayList<>(); // sets of completed quests and the ones in progress
	public ArrayList<Quest> questsInProgress = new ArrayList<>();
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

	public Quest questById(int id) {
		return (id >= 0) ? QuestList.quests.get(id) : proceduralQuests.get(-(id + 1));
	}

	public int getPreviousQuest(int id) {
		Quest quest = questById(id);

		if (quest.previousQuest == -1)
			return -1;

		return quest.previousQuest;
	}

	public boolean isQuestAvailable(int id) {
		Quest quest = questById(id);
		int prevQuestId = getPreviousQuest(id);

		if (isQuestCompleted(id) && !quest.repeatable)
			return false;

		if (prevQuestId == -1 && (!isQuestCompleted(id) || quest.repeatable))
			return true;

		if (isQuestCompleted(prevQuestId))
			return true;

		if (isQuestInProgress(id))
			return true;

		return false;
	}

	public ArrayList<Integer> getAvailableQuests(ArrayList<Integer> mobQuestList) {
		ArrayList<Integer> quests = new ArrayList<>();

		for (int quest : mobQuestList)
			if (isQuestAvailable(quest))
				quests.add(quest);

		return quests;
	}

	public ArrayList<Integer> getAvailableQuests(long npcUID) {
		ArrayList<Integer> quests = new ArrayList<>();

		for (ProceduralQuest quest : proceduralQuests)
			if (quest.npcUID == npcUID)
				quests.add(-quest.id);

		return quests;
	}

	private ProceduralQuest findProceduralQuest(long uid) {
		for (ProceduralQuest quest : proceduralQuests)
			if (quest.npcUID == uid)
				return quest;
		return null;
	}

	private boolean proceduralQuestExists(long uid) {
		for (ProceduralQuest quest : proceduralQuests)
			if (quest.npcUID == uid)
				return true;
		return false;
	}

	public ProceduralQuest createNewProceduralQuest(long npcUID) {
		ProceduralQuest quest;
		if (!proceduralQuestExists(npcUID)) {
			quest = new ProceduralQuest(--lastProceduralQuest, npcUID);
			proceduralQuests.add(quest);
		} else
			quest = findProceduralQuest(npcUID);
		return quest;
	}

	public boolean isQuestInProgress(int id) {
		return questsInProgress.contains(questById(id));
	}

	public boolean isQuestCompleted(int id) {
		return completedQuests.contains(questById(id));
	}

	private void startQuest(Quest quest, long npcUID) {
		Utils.out("Trying to start quest " + quest.id);

		if (isQuestCompleted(quest.id))
			return;

		if (player.inventory.putItem(quest.giveItems) != -1)
			MadSand.print("You get " + Item.queryToName(quest.giveItems));

		questsInProgress.add(quest);
		quest.start(player, npcUID);
		GameDialog.generateDialogChain(quest.startMsg, Gui.overlay).show();
	}

	private void completeQuest(Quest quest) {
		MadSand.notice("You completed a quest!");

		if (quest.deleteRequiredItems)
			player.inventory.delItem(quest.reqItems);

		player.inventory.delItem(quest.removeOnCompletion);
		player.inventory.putItem(quest.rewardItems);
		player.addExp(quest.exp);
		MadSand.notice("You get " + quest.exp + " EXP!");

		if (!quest.repeatable)
			completedQuests.add(quest);

		questsInProgress.remove(quest);
		Gui.refreshOverlay();
		GameDialog.generateDialogChain(quest.endMsg, Gui.overlay).show();
	}

	public void processQuest(int id, long npcUID) {
		Utils.out("Processing quest " + id);

		Quest quest = questById(id);

		if (completedQuests.contains(quest)) // if modified instance of raw quest exists, use it instead
			quest = completedQuests.get(completedQuests.indexOf(quest));
		else if (questsInProgress.contains(quest))
			quest = questsInProgress.get(questsInProgress.indexOf(quest));

		quest.setPlayer(player);

		if (isQuestInProgress(id) && !isQuestCompleted(id)) {
			if (quest.isComplete())
				completeQuest(quest);
			else
				GameDialog.generateDialogChain(quest.reqMsg, Gui.overlay).show();
		} else
			startQuest(quest, npcUID);

	}

	public void processQuest(int id) {
		processQuest(id, MadSand.world.getCurLoc().getNpc(player.lookingAt()).uid);
	}

	public boolean processQuests(ArrayList<Integer> mobQuestList, Npc npc) {
		ArrayList<Integer> availableQuests = getAvailableQuests(mobQuestList);

		if (availableQuests.size() == 0)
			return false;
		else if (availableQuests.size() == 1)
			processQuest(availableQuests.get(0), npc.uid);
		else
			new QuestListDialog(this, availableQuests, npc.stats.name).show();

		return true;
	}

	public int getAvailableQuest(ArrayList<Integer> quests) { // search NPC's quest list for {not yet started / not finished / repeatable} quests

		for (int qid : quests)
			if (isQuestInProgress(qid) || !isQuestCompleted(qid))
				return qid;

		return QuestList.NO_QUESTS_STATUS;
	}
}
