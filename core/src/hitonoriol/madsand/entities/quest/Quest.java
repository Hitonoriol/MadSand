package hitonoriol.madsand.entities.quest;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import hitonoriol.madsand.Enumerable;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.dialog.DialogChainGenerator;
import hitonoriol.madsand.dialog.TextSubstitutor;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.gamecontent.Items;
import hitonoriol.madsand.gamecontent.Npcs;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.widgets.waypoint.QuestArrow;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({ @Type(ProceduralQuest.class) })
public class Quest implements Enumerable {
	public static String OBJECTIVE_COLOR = "[#58FFB1D8]", REWARD_COLOR = "[#16E1EAD8]", EXP_COLOR = "[#DAA520D1]";

	private int id;
	public String name;
	public int exp;
	public int previousQuest = -1; // Quest id of quest after completion of which this quest becomes available

	public Pair npcWorldPos;
	public long npcUID;

	public String startMsg; // Dialog chain string -- displayed on quest start
	public String endMsg; // Dialog chain string -- displayed on completion of this quest
	public String reqMsg; // Dialog chain string -- displayed if player talks to npc with this quest still active

	public String reqKills = Item.EMPTY_ITEM; // "npcId/killCount:npcId/killCount"
	public String reqItems = Item.EMPTY_ITEM; // Item string (id/quantity:id/quantity:...) -- items that are required for the quest completion
	public String giveItems = Item.EMPTY_ITEM; // Item string -- Items to give after the quest start
	public String rewardItems = Item.EMPTY_ITEM; // Item string -- Items to give on quest completion
	public String removeOnCompletion = Item.EMPTY_ITEM; // Item string -- Items to remove on quest completion
	public String execOnCompletion = "", execOnStart = ""; // Lua script to be executed on quest start/completion

	public long startTime;

	public boolean repeatable = false;
	public boolean deleteRequiredItems = true; // Whether to delete items from reqItems list on quest completion or not
	public boolean isComplete = false;
	public boolean isProcedural = false;

	Player player;
	public HashMap<Integer, Integer> itemObjective; // {item id, quantity} pairs required to complete quest
	public HashMap<Integer, Integer> killObjective; // {Npc id, number of kills} pairs required to complete quest
	public HashMap<Integer, Integer> kills; //	{Npc id, kills} state of player's kill counter on quest start

	private QuestArrow questArrow;
	public static Comparator<Quest> startTimeComparator = Comparator.comparing(quest1 -> quest1.startTime);

	public Quest(int id) {
		this.id = id;
	}

	public Quest() {
		this(0);
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String name() {
		return name;
	}

	public Quest setPlayer(Player player) {
		this.player = player;
		return this;
	}

	public boolean hasQuestArrow() {
		return questArrow != null;
	}

	public QuestArrow questArrow() {
		if (hasQuestArrow())
			return questArrow;
		else
			return questArrow = new QuestArrow(this);
	}

	public boolean hasItemObjective() {
		return !reqItems.equals(Item.EMPTY_ITEM);
	}

	public boolean hasKillRequirements() {
		return !reqKills.equals(Item.EMPTY_ITEM);
	}

	public int getKillObjectiveProgress(int npcId) {

		if (isComplete)
			return killObjective.get(npcId);

		return player.getKillCount(npcId) - kills.get(npcId);
	}

	public int getItemObjectiveProgress(int itemId) {
		var item = player.inventory.getItem(itemId);

		if (isComplete)
			return itemObjective.get(itemId);

		if (item.equals(Item.nullItem))
			return 0;

		return item.quantity;
	}

	/*
	 * Returns human-readable quest objective string
	 * asList: "1 foo, 2 bar and 5 abc"
	 * !asList: "1 foo \n 2 bar \n 5 abc"
	 */
	private String getObjectiveString(
		HashMap<Integer, Integer> objectiveList, ObjectiveStringWorker worker,
		boolean asList
	) {
		var objective = "";
		if (objectiveList.isEmpty())
			return objective;

		int size = objectiveList.entrySet().size();
		int i = 0;
		for (Entry<Integer, Integer> entry : objectiveList.entrySet()) {
			objective += worker.makeObjectiveString(entry);
			if (!asList)
				objective += Resources.LINEBREAK;
			else {
				if (i == size - 2)
					objective += " and ";
				else if (i < size - 1)
					objective += ", ";
			}
			++i;
		}
		return objective;
	}

	@JsonIgnore
	private String getKillObjectiveString() {
		return getObjectiveString(
			killObjective,
			entry -> "Kill " + Npcs.all().get(entry.getKey()).name +
				" (" + getKillObjectiveProgress(entry.getKey()) + "/" + entry.getValue() + ")",
			false
		);
	}

	@JsonIgnore
	private String getItemObjectiveString() {
		return getObjectiveString(
			itemObjective,
			entry -> "Get " + Items.all().getName(entry.getKey()) +
				" (" + getItemObjectiveProgress(entry.getKey()) + "/" + entry.getValue() + ")",
			false
		);
	}

	protected String getObjectiveList(HashMap<Integer, Integer> objective, NameGetter nameGetter, String colorTag) {
		return getObjectiveString(
			objective,
			entry -> colorTag + entry.getValue() + " " + nameGetter.get(entry.getKey())
				+ Resources.COLOR_END,
			true
		);
	}

	protected String getObjectiveList(HashMap<Integer, Integer> objective, NameGetter nameGetter) {
		return getObjectiveList(objective, nameGetter, OBJECTIVE_COLOR);
	}

	private boolean verifyObjective(HashMap<Integer, Integer> objectiveList, ObjectiveVerifier verifier) {
		if (objectiveList.isEmpty())
			return true;
		for (Entry<Integer, Integer> entry : objectiveList.entrySet())
			if (!verifier.verify(entry))
				return false;
		return true;
	}

	private boolean verifyKillObjective() {
		return verifyObjective(
			killObjective,
			entry -> getKillObjectiveProgress(entry.getKey()) >= entry.getValue()
		);
	}

	private boolean verifyItemObjective() {
		return verifyObjective(
			itemObjective,
			entry -> getItemObjectiveProgress(entry.getKey()) >= entry.getValue()
		);
	}

	@JsonIgnore
	public boolean isComplete() {
		if (isComplete)
			return true;

		return (verifyKillObjective() && verifyItemObjective());
	}

	/*
	 * Fills map with objective entries: (id, quantity)
	 * Returns false if objective query string is invalid (must be in format:
	 * id/quantity:id/quantity:...)
	 */
	private boolean createObjectiveMap(String query, HashMap<Integer, Integer> objective) {
		if ((query == null) || !query.contains(Item.ITEM_DELIM))
			return false;

		Item.parseListString(query, (id, quantity) -> objective.put(id, quantity));

		return true;
	}

	private void initCurrentKills() {
		for (Entry<Integer, Integer> entry : killObjective.entrySet())
			kills.put(entry.getKey(), player.getKillCount(entry.getKey()));
	}

	@JsonIgnore
	public String getObjectiveString() {
		return getItemObjectiveString() + getKillObjectiveString();
	}

	@JsonIgnore
	public AbstractNpc getNpc() { // Returns nullNpc if quest npc does not exist or is not in the current location
		return MadSand.world().getCurLoc().getNpc(npcUID);
	}

	public void completionNotice() {
		MadSand.notice(
			"You've just completed all objectives for the \"" + name + "\". "
				+ "Return to " + getNpc().stats.name + " to claim your reward."
		);
	}

	protected void createEndMsg() {
		endMsg += Resources.LINEBREAK + Resources.LINEBREAK;
		endMsg += "Rewards:" + Resources.LINEBREAK;
		endMsg += Utils.subsName(TextSubstitutor.QUEST_EXP_REWARD);
		if (!rewardItems.equals(Item.EMPTY_ITEM))
			endMsg += ", " + Utils.subsName(TextSubstitutor.QUEST_ITEM_REWARD) + ".";
	}

	public void start(Player player, long npcUID) {
		this.player = player;
		this.npcUID = npcUID;
		npcWorldPos = new Pair(MadSand.world().getCurWPos());
		startTime = MadSand.world().currentActionTick();
		isComplete = false;
		itemObjective = new HashMap<>();
		killObjective = new HashMap<>();
		kills = new HashMap<>();
		Lua.execute(execOnStart);

		createObjectiveMap(reqItems, itemObjective);
		if (createObjectiveMap(reqKills, killObjective))
			initCurrentKills();
	}

	private void setTextSubstitutionValues() {
		var expRewardString = EXP_COLOR + "+" + exp + " EXP" + Resources.COLOR_END;
		var reward = new HashMap<Integer, Integer>();
		createObjectiveMap(rewardItems, reward);

		TextSubstitutor.add(TextSubstitutor.QUEST_ITEM_OBJECTIVE, getObjectiveList(itemObjective, itemNames));
		TextSubstitutor.add(TextSubstitutor.QUEST_KILL_OBJECTIVE, getObjectiveList(killObjective, npcNames));
		TextSubstitutor.add(
			TextSubstitutor.QUEST_ITEM_REWARD,
			getObjectiveList(reward, itemNames, REWARD_COLOR)
		);
		TextSubstitutor.add(TextSubstitutor.QUEST_EXP_REWARD, expRewardString);
	}

	private void showQuestDialog(String dialogText) {
		setTextSubstitutionValues();
		new DialogChainGenerator(dialogText)
			.setAllTitles(getNpc().stats.name)
			.generate(Gui.overlay)
			.show();
	}

	public void showQuestInProgressDialog() {
		showQuestDialog(reqMsg);
	}

	public void showQuestStartDialog() {
		showQuestDialog(startMsg);
	}

	public void showQuestCompleteDialog() {
		isComplete = true;
		createEndMsg();
		showQuestDialog(endMsg);
	}

	protected NameGetter itemNames = id -> Items.all().getName(id);
	protected NameGetter npcNames = id -> Npcs.all().get(id).name;

	private interface NameGetter {
		String get(int id);
	}

	private interface ObjectiveStringWorker {
		String makeObjectiveString(Entry<Integer, Integer> entry);
	}

	private interface ObjectiveVerifier {
		boolean verify(Entry<Integer, Integer> entry);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(9839, 31183).append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Quest))
			return false;
		if (obj == this)
			return true;

		var rhs = (Quest) obj;
		return new EqualsBuilder().append(id, rhs.id).isEquals();
	}
}
