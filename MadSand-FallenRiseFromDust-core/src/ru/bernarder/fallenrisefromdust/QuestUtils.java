package ru.bernarder.fallenrisefromdust;

public class QuestUtils {
	static final int REWARD = 0;
	static final int STEXT = 1;
	static final int ETEXT = 2;
	static final int RTEXT = 3;
	static final int REQUIREMENT = 4;
	static final int QUESTS = 2;
	static final int ACTIVE = 5;
	static final int REPEATEBLE = 6;
	static final int DONE = 7;
	static final int AFTERTEXT = 8;
	static final int PREQUESTITEM = 9;
	static final int PARAMETERS = 10;
	static String[][] questList;
	static int qu;

	public static void init() {
		qu = Utils.countKeys(Utils.RES, "quest");
		Utils.out(qu + " quests loaded");
		questList = new String[qu][10];
		int i = 0;
		while (i < qu) {
			questList[i][0] = "n";
			questList[i][1] = Utils.getKey(Utils.RES, "quest", i + "", "starttext");
			questList[i][2] = Utils.getKey(Utils.RES, "quest", i + "", "completedtext");
			questList[i][3] = Utils.getKey(Utils.RES, "quest", i + "", "afterstarttext");
			questList[i][4] = Utils.getKey(Utils.RES, "quest", i + "", "requirement");
			questList[i][6] = Utils.getKey(Utils.RES, "quest", i + "", "repeatable");
			questList[i][9] = Utils.getKey(Utils.RES, "quest", i + "", "forquest");
			i++;
		}
	}

	public static void invokeQuest(int id) {
		if (id < qu) {
			if ((MadSand.quests[id][1] == 1) && (LootLayer.ifExist(questList[id][4]))) {
				LootLayer.putLootQuery(MadSand.x, MadSand.y, questList[id][0]);
				MadSand.showDialog(1, questList[id][2], id);
				LootLayer.removeLootFromInv(questList[id][4]);
				MadSand.quests[id][0] = 1;
				MadSand.quests[id][1] = 0;
				return;
			}
			if (((MadSand.quests[id][1] != 1) && (MadSand.quests[id][0] != 1)) || (questList[id][6] == "1")) {
				MadSand.quests[id][1] = 1;
				MadSand.showDialog(1, questList[id][1], id);
				LootLayer.putLootQuery(MadSand.x, MadSand.y, questList[id][9]);
				return;
			}
			if (MadSand.quests[id][1] == 1) {
				MadSand.showDialog(1, questList[id][3], id);
			}
		}
	}
}
