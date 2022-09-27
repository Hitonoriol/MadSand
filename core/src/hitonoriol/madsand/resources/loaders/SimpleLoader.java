package hitonoriol.madsand.resources.loaders;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;

import hitonoriol.madsand.resources.GameAssetManager;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;

public abstract class SimpleLoader<T> extends AssetLoader<T> {

	public SimpleLoader(GameAssetManager manager) {
		super(manager);
	}

	@Override
	public final T load(AssetManager assetManager, String fileName, FileHandle file, Parameters<T> parameter) {
		Utils.out("[Asset Loader] Loading %s", fileName);
		return load(Resources.manager());
	}

	protected abstract T load(GameAssetManager manager);
}
