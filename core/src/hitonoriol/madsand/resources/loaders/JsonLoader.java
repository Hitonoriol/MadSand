package hitonoriol.madsand.resources.loaders;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;

import hitonoriol.madsand.resources.GameAssetManager;
import hitonoriol.madsand.resources.Resources;

public class JsonLoader<T> extends AssetLoader<T> {
	private Class<T> type;

	public JsonLoader(GameAssetManager manager, Class<T> type) {
		super(manager);
		this.type = type;
	}

	@Override
	public final T load(AssetManager assetManager, String fileName, FileHandle file, Parameters<T> parameter) {
		var object = Resources.loader().load(fileName, type);
		load(object);
		return object;
	}

	protected void load(T object) {}
}
