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
import com.fasterxml.jackson.annotation.JsonTypeInfo.*;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.NpcProp;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({ @Type(ProceduralQuest.class) })
public class Quest {
	public int id;
	public String name;
	public int exp;
	public int previousQuest = -1; // Quest id of quest after completion of which this quest becomes available

	public Pair npcWorldPos;
	public long npcUID;

	public String startMsg; // Dialog chain string -- displayed on quest start
	public String endMsg; // Dialog chain string -- displayed on completion of this quest
	public String reqMsg; // Dialog chain string -- displayed if player talks to npc with this quest still active

	public String reqKills; // "npcId/killCount:npcId/killCount"
	public String reqItems = Item.EMPTY_ITEM; // Item string (id/quantity:id/quantity:...) -- items that are required for the quest completion
	public String giveItems = Item.EMPTY_ITEM; // Item string -- Items to give after the quest start
	public String rewardItems = Item.EMPTY_ITEM; // Item string -- Items to give on quest completion
	public String removeOnCompletion = Item.EMPTY_ITEM; // Item string -- Items to remove on quest completion
	public String execOnCompletion = ""; // Lua script to be executed on quest completion

	public long startTime;

	public boolean repeatable = false;
	public boolean deleteRequiredItems = true; // Whether to delete items from reqItems list on quest completion or not
	public boolean isComplete = false;
	public boolean isProcedural = false;

	Player player;
	public HashMap<Integer, Integer> itemObjective; // {item id, quantity} pairs required to complete quest
	public HashMap<Integer, Integer> killObjective; // {Npc id, number of kills} pairs required to complete quest
	public HashMap<Integer, Integer> kills; //	{Npc id, kills} state of player's kill counter on quest start 

	public static Comparator<Quest> startTimeComparator = new Comparator<Quest>() {

		@Override
		public int compare(Quest o1, Quest o2) {
			return Long.compare(o1.startTime, o2.startTime);
		}
	};

	public Quest(int id) {
		this.id = id;
	}

	public Quest() {
		this(0);
	}

	public Quest setPlayer(Player player) {
		this.player = player;
		return this;
	}

	public int getKillObjectiveProgress(int npcId) {

		if (isComplete)
			return killObjective.get(npcId);

		return player.getKillCount(npcId) - kills.get(npcId);
	}

	public int getItemObjectiveProgress(int itemId) {
		Item item = player.inventory.getItem(itemId);

		if (isComplete)
			return itemObjective.get(itemId);

		if (item.equals(Item.nullItem))
			return 0;

		return item.quantity;
	}

	@JsonIgnore
	private String getObjectiveString(HashMap<Integer, Integer> objectiveList, ObjectiveStringWorker worker) {
		String objective = "";
		if (objectiveList.isEmpty())
			return objective;
		for (Entry<Integer, Integer> entry : objectiveList.entrySet())
			objective += worker.makeObjectiveString(entry) + Resources.LINEBREAK;
		return objective;
	}

	@JsonIgnore
	private String getKillObjectiveString() {
		return getObjectiveString(killObjective,
				(Entry<Integer, Integer> entry) -> {
					return "Kill " + NpcProp.npcs.get(entry.getKey()).name +
							" (" + getKillObjectiveProgress(entry.getKey()) + "/" + entry.getValue() + ")";
				});
	}

	@JsonIgnore
	private String getItemObjectiveString() {
		return getObjectiveString(itemObjective,
				(Entry<Integer, Integer> entry) -> {
					return "Get " + ItemProp.getItemName(entry.getKey()) +
							" (" + getItemObjectiveProgress(entry.getKey()) + "/" + entry.getValue() + ")";
				});
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
		return verifyObjective(killObjective,
				(Entry<Integer, Integer> entry) -> {
					return getKillObjectiveProgress(entry.getKey()) >= entry.getValue();
				});
	}

	private boolean verifyItemObjective() {
		return verifyObjective(itemObjective,
				(Entry<Integer, Integer> entry) -> {
					return getItemObjectiveProgress(entry.getKey()) >= entry.getValue();
				});
	}

	@JsonIgnore
	public boolean isComplete() {
		if (isComplete)
			return true;

		return isComplete = (verifyKillObjective() && verifyItemObjective());
	}

	private boolean initObjective(String query, HashMap<Integer, Integer> objective) {
		if (query == null)
			return false;
		if (!query.contains(Item.ITEM_DELIM))
			return false;
		if (!query.contains(Item.BLOCK_DELIM))
			reqItems += Item.BLOCK_DELIM;
		String blocks[] = query.split(Item.BLOCK_DELIM);
		String item[];
		for (String itemStr : blocks) {
			item = itemStr.split(Item.ITEM_DELIM);
			objective.put(Utils.val(item[0]), Utils.val(item[1]));
		}

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
	public Npc getNpc() { // Returns nullNpc if quest npc does not exist or is not in the current location
		return MadSand.world.getCurLoc().getNpc(npcUID);
	}

	public void start(Player player, long npcUID) {
		this.player = player;
		this.npcUID = npcUID;
		this.npcWorldPos = new Pair(MadSand.world.getCurWPos());
		this.startTime = MadSand.world.globalRealtimeTick;
		isComplete = false;
		itemObjective = new HashMap<>();
		killObjective = new HashMap<>();
		kills = new HashMap<>();

		initObjective(reqItems, itemObjective);
		if (initObjective(reqKills, killObjective))
			initCurrentKills();
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

		Quest rhs = (Quest) obj;
		return new EqualsBuilder().append(id, rhs.id).isEquals();
	}
}
