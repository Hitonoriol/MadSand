package hitonoriol.madsand.entities.quest;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Quest {
	@JsonIgnore
	public int id;
	public String name;
	public int previousQuest = -1;
	
	public int exp;

	public String startMsg; // Dialog chain string -- displayed on quest start
	public String endMsg; // Dialog chain string -- displayed on completion of this quest
	public String reqMsg; // Dialog chain string -- displayed if player talks to npc with this quest still active
	public String journalText; // String -- displayed in the "Requirements" column in quest journal

	public String reqItems; // Item string (id/quantity:id/quantity:...) -- items that are required for the quest completion
	public String giveItems; // Item string -- Items to give after the quest start
	public String rewardItems; // Item string -- Items to give on quest completion
	public String removeOnCompletion; // Item string -- Items to remove on quest completion

	public boolean repeatable = false;
	public boolean deleteRequiredItems = true; // Whether to delete items from reqItems list on quest completion or not

	public Quest(int id) {
		this.id = id;
	}

	public Quest() {
		this(0);
	}
}
