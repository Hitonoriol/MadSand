package hitonoriol.madsand.gamecontent;

import java.util.ArrayList;
import java.util.List;

import hitonoriol.madsand.resources.GameAssetManager;
import hitonoriol.madsand.resources.loaders.ContentLoader;
import hitonoriol.madsand.world.worldgen.WorldGenPreset;

public class WorldGenPresets extends ContentStorage<WorldGenPreset> {
	private List<String> encounters;

	private static final WorldGenPresets instance = new WorldGenPresets();

	protected WorldGenPresets() {
		super(null);
	}

	public List<String> encounters() {
		return encounters;
	}

	public void setEncounters(List<String> encounters) {
		this.encounters = encounters;
	}

	@Override
	public void registerLoader(GameAssetManager manager) {
		manager.jsonLoader(EncounterList.class);
		manager.setLoader(WorldGenPresets.class, new ContentLoader<>(manager, instance, WorldGenPreset.class) {
			private static final String ENCOUNTER_FILE = "encounters.json";

			@Override
			protected void load(GameAssetManager manager, WorldGenPresets storage) {
				storage.setDefaultValue(new WorldGenPreset());
				manager.loadAndThen(ENCOUNTER_FILE, EncounterList.class, storage::setEncounters);
			}
		});
	}

	public static WorldGenPresets all() {
		return instance;
	}

	private static class EncounterList extends ArrayList<String> {}
}
