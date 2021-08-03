package hitonoriol.madsand.tests;

import org.junit.jupiter.api.Test;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.skill.Skill;
import hitonoriol.madsand.entities.skill.SkillContainer;
import hitonoriol.madsand.gui.widgets.overlay.ResourceProgressBar;
import hitonoriol.madsand.map.object.ResourceObject;
import hitonoriol.madsand.util.Utils;

public class SkillProgressionTest {

	@Test
	void resourceSkillProgression() {
		showSkillProgression(Skill.Mining);
	}

	private void showSkillProgression(Skill skill) {
		Utils.out("%s Skill progression:", skill);
		ResourceObject object = new ResourceObject();
		object.maxHp = 8;
		object.harvestHp = 10;
		object.skill = skill;
		ResourceProgressBar bar = new ResourceProgressBar(object);
		SkillContainer skills = MadSand.player().stats.skills;
		for (int lvl = 0; lvl <= SkillContainer.MAX_SKILL_LVL; ++lvl) {
			Utils.out("%s %s effect: %f", skill, skills.get(skill), skills.getSkillEffect(skill));
			bar.preCalculateGathering();
			skills.get(skill).addExp(skills.get(skill).expToNextLvl());
			skills.check(skill);
		}
		Utils.out();
	}
}
