package hitonoriol.madsand.resources.loaders;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;

import hitonoriol.madsand.resources.GameAssetManager;
import hitonoriol.madsand.resources.GameAssetManager.Dependencies;
import hitonoriol.madsand.resources.loaders.AssetLoader.Parameters;

public abstract class AssetLoader<T> extends SynchronousAssetLoader<T, Parameters<T>> {
	public AssetLoader(GameAssetManager manager) {
		super(manager.getFileHandleResolver());
	}
	
	@Override
	public Dependencies getDependencies(String fileName, FileHandle file, Parameters<T> parameter) {
		return null;
	}
	
	public static class Parameters<T> extends AssetLoaderParameters<T> {}
}
