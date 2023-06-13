package hitonoriol.madsand.resources.loaders;

import java.util.HashMap;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;

import hitonoriol.madsand.Enumerable;
import hitonoriol.madsand.gamecontent.ContentStorage;
import hitonoriol.madsand.resources.GameAssetManager;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;

public class ContentLoader<S extends ContentStorage<T>, T extends Enumerable> extends AssetLoader<S> {
	private S storage;
	private Class<T> elementType;

	public ContentLoader(GameAssetManager manager, S storage, Class<T> elementType) {
		super(manager);
		this.storage = storage;
		this.elementType = elementType;
	}

	@Override
	public final S load(AssetManager assetManager, String fileName, FileHandle file, Parameters<S> parameter) {
		storage.set(Resources.loader().loadEnumerableMap(fileName, elementType));
		Utils.out(
			"[%s Loader] Loaded %d %ss",
			storage.getClass().getSimpleName(), storage.get().size(), elementType.getSimpleName()
		);
		load(Resources.manager(), storage);
		return storage;
	}

	/* For additional resource loading */
	protected void load(GameAssetManager manager, S storage) {}

	public static abstract class ContentMap<T> extends HashMap<Integer, T> {}
}
