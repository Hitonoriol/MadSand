package hitonoriol.madsand.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.quest.QuestWorker;
import hitonoriol.madsand.entities.skill.Skill;
import hitonoriol.madsand.entities.skill.SkillContainer;
import hitonoriol.madsand.gui.stages.Overlay;
import hitonoriol.madsand.properties.QuestList;

public class QuestTest {
	Player player = MadSand.player();
	QuestWorker quests = player.getQuestWorker();
	Overlay overlay = Gui.overlay;

	SkillContainer skills = player.stats().skills, expectedSkills = new SkillContainer();

	@Test
	void startEndQuestTest() {
		QuestList.quests.forEach((id, quest) -> {
			player.inventory.clear();
			expectedSkills.increaseSkill(Skill.Level, quest.exp);
			int expectedLvl = expectedSkills.getLvl(), expectedExp = expectedSkills.getExp();
			List<Item> requiredItems = Item.parseItemString(quest.reqItems);

			quests.processQuest(id);
			overlay.closeAllDialogs();
			assertTrue(quests.isQuestInProgress(id), "Quest should be in progress");

			if (quest.hasItemObjective())
				player.addItem(requiredItems);

			if (quest.hasKillRequirements())
				quest.killObjective.forEach((npcId, reqKills) -> player.addToKillCount(npcId, reqKills));

			assertTrue(quest.isComplete(), "Quest objectives are satisfied, so quest should be completed");

			quests.processQuest(id);
			overlay.closeAllDialogs();
			List<Item> rewardItems = Item.parseItemString(quest.rewardItems);
			/* 
			 * hasItem(id, q) is used here instead of containsAll()
			 * because of possible unique reward items -- equals() won't work for them
			 * (because they're... well... unique.)
			 */
			assertTrue(rewardItems.stream().allMatch(item -> player.inventory.hasItem(item.id, item.quantity)),
					"Player should receive all reward items on quest completion");
			assertEquals(expectedLvl, skills.getLvl());
			assertEquals(expectedExp, skills.getExp());
		});
	}
}
