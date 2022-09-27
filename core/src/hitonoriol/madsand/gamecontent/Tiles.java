package hitonoriol.madsand.gamecontent;

import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.resources.GameAssetManager;
import hitonoriol.madsand.resources.loaders.ContentLoader;

public class Tiles extends ContentStorage<Tile> {
	private static final Tiles instance = new Tiles();

	private Tiles() {
		super(Map.nullTile);
	}

	public String getOnInteract(int id) {
		return get(id).onInteract;
	}

	@Override
	public void registerLoader(GameAssetManager manager) {
		manager.setLoader(Tiles.class, new ContentLoader<>(manager, instance, Tile.class));
	}

	public static Tiles all() {
		return instance;
	}
}