package hitonoriol.madsand.entities.quest;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Quest {
	@JsonIgnore
	public int id;
	
	public int exp;
	public String startMsg, endMsg, reqMsg;
	public String reqItems, giveItems, rewardItems, removeOnCompletion;
	public boolean repeatable;

	public Quest(int id) {
		this.id = id;
	}

	public Quest() {
		this(0);
	}
}
