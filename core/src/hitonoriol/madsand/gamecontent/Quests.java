package hitonoriol.madsand.gamecontent;

import hitonoriol.madsand.entities.quest.Quest;
import hitonoriol.madsand.resources.GameAssetManager;

public class Quests extends ContentStorage<Quest> {
	public static final int NO_QUESTS_STATUS = -1;
	public static final int QUEST_IN_PROGRESS_STATUS = -2;

	private final static Quests instance = new Quests();

	protected Quests() {
		super(new Quest());
	}

	@Override
	public void registerLoader(GameAssetManager manager) {
		manager.contentLoader(instance, Quest.class);
	}

	public static Quests all() {
		return instance;
	}
}
