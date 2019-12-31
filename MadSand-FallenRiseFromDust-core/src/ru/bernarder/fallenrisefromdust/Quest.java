package ru.bernarder.fallenrisefromdust;

public class Quest {
	public int id, next, exp;
	public String startMsg, endMsg, reqMsg;
	public String reqItems, giveItems, rewardItems, removeOnCompletion;
	public boolean repeatable;

	public Quest(int id) {
		this.id = id;
	}
}
