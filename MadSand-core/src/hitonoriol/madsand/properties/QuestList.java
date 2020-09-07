package hitonoriol.madsand.properties;

import java.util.HashMap;

import hitonoriol.madsand.Quest;

public class QuestList {
	public static final int NO_QUESTS_STATUS = -1;
	public static final int QUEST_IN_PROGRESS_STATUS = -2;
	
	public static HashMap<Integer, Quest> quests =  new HashMap<Integer, Quest>();
}
