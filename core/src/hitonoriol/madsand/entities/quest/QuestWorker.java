package hitonoriol.madsand.entities.quest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.DialogChainGenerator;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.Reputation;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.gamecontent.Quests;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.dialogs.QuestListDialog;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;

public class QuestWorker {

	public int lastProceduralQuest = 0; // Decrements for each new procedural quest
	public ArrayList<Quest> completedQuests = new ArrayList<>(); // sets of completed quests and the ones in progress
	public ArrayList<Quest> questsInProgress = new ArrayList<>();
	public ArrayList<ProceduralQuest> proceduralQuests = new ArrayList<>();
	List<List<? extends Quest>> questLists;
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
		questLists = Arrays.asList(proceduralQuests, questsInProgress, completedQuests);
		for (List<? extends Quest> questList : questLists)
			for (Quest quest : questList)
				quest.setPlayer(player);
	}

	private ProceduralQuest findQuest(
		List<? extends Quest> list, long val,
		BiPredicate<ProceduralQuest, Long> criterion
	) {
		for (Quest quest : list)
			if (quest.id() < 0 && criterion.test((ProceduralQuest) quest, val))
				return (ProceduralQuest) quest;
		return null;
	}

	private ProceduralQuest findQuest(long val, BiPredicate<ProceduralQuest, Long> criterion) {
		ProceduralQuest quest;
		for (List<? extends Quest> questList : questLists) {
			Collections.sort(questList, Quest.startTimeComparator);
			quest = findQuest(questList, val, criterion);
			if ((quest) != null)
				return quest;
		}
		return null;
	}

	public Quest questById(int id) {
		if (id >= 0)
			return Quests.all().get(id);

		return findQuest(id, (var squest, var sid) -> squest.id() == sid.longValue());
	}

	public int getPreviousQuest(int id) {
		var quest = questById(id);

		if (quest.previousQuest == -1)
			return -1;

		return quest.previousQuest;
	}

	public boolean isQuestAvailable(int id) {
		var quest = questById(id);
		int prevQuestId = getPreviousQuest(id);

		if (isQuestCompleted(id) && !quest.repeatable)
			return false;

		if (
			(prevQuestId == -1 && (!isQuestCompleted(id) || quest.repeatable)) || isQuestCompleted(prevQuestId)
		)
			return true;

		if (isQuestInProgress(id))
			return true;

		return false;
	}

	public ArrayList<Integer> getAvailableQuests(ArrayList<Integer> mobQuestList) {
		var quests = new ArrayList<Integer>();

		for (int quest : mobQuestList)
			if (isQuestAvailable(quest))
				quests.add(quest);

		return quests;
	}

	private ProceduralQuest findProceduralQuest(long uid) {
		return findQuest(uid, (var quest, var suid) -> quest.npcUID == suid.longValue());
	}

	private ProceduralQuest getProceduralQuest(long npcUID) {
		var quest = findProceduralQuest(npcUID);
		if (quest == null) {
			quest = new ProceduralQuest(--lastProceduralQuest, npcUID);
			proceduralQuests.add(quest);
		} else {
			quest = findProceduralQuest(npcUID);
			if (isQuestCompleted(quest.id())) {
				if (quest.timeSinceCreated() < ProceduralQuest.QUEST_TIMEOUT)
					return quest;
				else {
					quest = new ProceduralQuest(--lastProceduralQuest, npcUID);
					proceduralQuests.add(quest);
				}
			}
		}
		return quest;
	}

	public ProceduralQuest startProceduralQuest(long uid) {
		var quest = getProceduralQuest(uid);
		long waitTime = (long) ((ProceduralQuest.QUEST_TIMEOUT - quest.timeSinceCreated())
			* MadSand.world().getRealtimeActionSeconds());

		if (waitTime > 0 && quest.isComplete) {
			new DialogChainGenerator(
				"You want another task?" + Resources.LINEBREAK +
					"Well, you'll have to wait another "
					+ Utils.timeString(waitTime) + " for me to come up with something for you."
			)
				.generate(Gui.overlay)
				.setTitle(quest.getNpc().stats.name)
				.show();
			quest = ProceduralQuest.timeoutQuest;
		} else
			processQuest(quest.id());

		return quest;
	}

	public boolean isQuestInProgress(int id) {
		return questsInProgress.contains(questById(id));
	}

	public boolean isQuestCompleted(int id) {
		return completedQuests.contains(questById(id));
	}

	private void startQuest(Quest quest, long npcUID) {
		Utils.dbg("Trying to start quest " + quest.id());

		if (isQuestCompleted(quest.id()))
			return;

		if (player.inventory.putItem(quest.giveItems))
			MadSand.print("You get " + Item.createReadableItemList(quest.giveItems));

		questsInProgress.add(quest);
		proceduralQuests.remove(quest);

		quest.start(player, npcUID);
		quest.showQuestStartDialog();
		
		if (quest.isInstant())
			completeQuest(quest);
	}

	public void completionNotice(Quest quest) {
		if (quest.isInstant())
			return;
		
		MadSand.notice(
			"You've just completed all objectives for the \"" + quest.name + "\". "
				+ "Return to " + quest.getNpc().stats.name + " to claim your reward."
		);
	}
	
	private void completeQuest(Quest quest) {
		if (!quest.isInstant())
			MadSand.notice("You completed a quest!");

		if (quest.deleteRequiredItems)
			player.inventory.delItem(quest.reqItems);

		if (!quest.execOnCompletion.equals(""))
			Lua.execute(quest.execOnCompletion);

		player.inventory.delItem(quest.removeOnCompletion);
		player.inventory.putItem(quest.rewardItems);
		player.addExp(quest.exp);
		player.getReputation().change(quest.getNpc().stats.faction, Reputation.QUEST_REWARD);
		
		if (quest.exp > 0)
			MadSand.notice("You get " + quest.exp + " EXP!");

		if (!quest.repeatable)
			completedQuests.add(quest);

		questsInProgress.remove(quest);
		Gui.refreshOverlay();
		quest.showQuestCompleteDialog();
	}

	public void processQuest(int id, long npcUID) {
		Utils.dbg("Processing quest " + id);

		var quest = questById(id);

		if (completedQuests.contains(quest)) // if modified instance of raw quest exists, use it instead
			quest = completedQuests.get(completedQuests.indexOf(quest));
		else if (questsInProgress.contains(quest))
			quest = questsInProgress.get(questsInProgress.indexOf(quest));

		quest.setPlayer(player);

		if (isQuestInProgress(id) && !isQuestCompleted(id)) {
			if (quest.isComplete())
				completeQuest(quest);
			else
				quest.showQuestInProgressDialog();

		} else
			startQuest(quest, npcUID);

	}

	public void processQuest(int id) {
		processQuest(id, MadSand.world().getCurLoc().getNpc(player.lookingAt()).uid());
	}

	public boolean processQuests(ArrayList<Integer> mobQuestList, AbstractNpc npc) {
		var availableQuests = getAvailableQuests(mobQuestList);

		if (availableQuests.size() == 0)
			return false;
		else if (availableQuests.size() == 1)
			processQuest(availableQuests.get(0), npc.uid());
		else
			new QuestListDialog(this, availableQuests, npc.stats.name).show();

		return true;
	}

	public int getAvailableQuest(ArrayList<Integer> quests) { // search NPC's quest list for {not yet started / not finished / repeatable} quests
		for (int qid : quests)
			if (isQuestInProgress(qid) || !isQuestCompleted(qid))
				return qid;

		return Quests.NO_QUESTS_STATUS;
	}
}
