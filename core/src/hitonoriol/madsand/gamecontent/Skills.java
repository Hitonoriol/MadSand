package hitonoriol.madsand.gamecontent;

import java.util.Map;

import hitonoriol.madsand.entities.skill.Skill;
import hitonoriol.madsand.entities.skill.SkillValue;
import hitonoriol.madsand.resources.GameAssetManager;
import hitonoriol.madsand.resources.loaders.JsonLoader;

public class Skills implements Loadable {
	private Map<Skill, SkillValue> requirements;

	private static Skills instance = new Skills();

	public Map<Skill, SkillValue> requirements() {
		return requirements;
	}

	@Override
	public void registerLoader(GameAssetManager manager) {
		manager.setLoader(Skills.class, new JsonLoader<>(manager, Skills.class) {
			@Override
			protected void load(Skills object) {
				instance = object;
			}
		});
	}

	public static Skills all() {
		return instance;
	}
}
