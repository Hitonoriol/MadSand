package hitonoriol.madsand.resources;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.assets.AssetDescriptor;

import hitonoriol.madsand.commons.reflection.Reflection;
import hitonoriol.madsand.gamecontent.Globals;
import hitonoriol.madsand.gamecontent.Items;
import hitonoriol.madsand.gamecontent.Loadable;
import hitonoriol.madsand.gamecontent.Npcs;
import hitonoriol.madsand.gamecontent.Objects;
import hitonoriol.madsand.gamecontent.Quests;
import hitonoriol.madsand.gamecontent.Skills;
import hitonoriol.madsand.gamecontent.Textures;
import hitonoriol.madsand.gamecontent.Tiles;
import hitonoriol.madsand.gamecontent.Tutorial;
import hitonoriol.madsand.gamecontent.WorldGenPresets;

public enum Content {
	textures("textures/textures.atlas", Textures.get()),
	globals("globals.json", Globals.values()),
	tiles("tiles.json", Tiles.all()),
	objects("objects.json", Objects.all()),
	items("items.json", Items.all()),
	quests("quests.json", Quests.all()),
	npcs("npcs.json", Npcs.all()),
	skills("skills.json", Skills.all()),
	worldGen("worldgen.json", WorldGenPresets.all()),
	tutorial("tutorial.json", Tutorial.get());

	private final static List<Content> values = Collections.unmodifiableList(Arrays.asList(values()));

	private Loadable contentStorage;
	private AssetDescriptor<?> descriptor;

	<T extends Loadable> Content(String filename, T assetStorage) {
		contentStorage = assetStorage;
		descriptor = descriptor(filename, Reflection.getClass(assetStorage));
	}

	public static void registerLoaders(GameAssetManager manager) {
		values.forEach(asset -> asset.contentStorage.registerLoader(manager));
	}

	public Loadable contentStorage() {
		return contentStorage;
	}

	public AssetDescriptor<?> descriptor() {
		return descriptor;
	}

	public static List<Content> asList() {
		return values;
	}

	public static <T> AssetDescriptor<T> descriptor(String filename, Class<T> type) {
		return new AssetDescriptor<>(filename, type);
	}

	public static GameAssetManager.Dependencies asDependencies(Content... assets) {
		return new GameAssetManager.Dependencies(
			Arrays.stream(assets)
				.map(Content::descriptor)
				.toArray(AssetDescriptor[]::new)
		);
	}
}
