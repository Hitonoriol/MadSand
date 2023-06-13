package hitonoriol.madsand.resources.loaders;

import java.util.HashMap;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;

import hitonoriol.madsand.commons.reflection.Reflection;
import hitonoriol.madsand.resources.GameAssetManager;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;

public class MapLoader<T extends HashMap<K, V>, K, V> extends AssetLoader<T> {
	private Class<T> storageType;
	private Class<K> keyType;
	private Class<V> valueType;

	public MapLoader(GameAssetManager manager, Class<T> storageType, Class<K> keyType, Class<V> valueType) {
		super(manager);
		this.storageType = storageType;
		this.keyType = keyType;
		this.valueType = valueType;
	}

	public MapLoader(GameAssetManager manager, Class<K> keyType, Class<V> valueType) {
		this(manager, Reflection.mapClass(HashMap.class), keyType, valueType);
	}

	@Override
	public T load(AssetManager assetManager, String fileName, FileHandle file, Parameters<T> parameter) {
		var map = Resources.loader().loadMap(fileName, storageType, keyType, valueType);
		Utils.out(
			"[%s Loader] Loaded %d %ss",
			storageType.getSimpleName(), map.size(), valueType.getSimpleName()
		);
		return map;
	}
}
