package hitonoriol.madsand.resources;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.assets.loaders.CubemapLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.I18NBundleLoader;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.assets.loaders.PixmapLoader;
import com.badlogic.gdx.assets.loaders.ShaderProgramLoader;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonRegionLoader;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.UBJsonReader;

import hitonoriol.madsand.Enumerable;
import hitonoriol.madsand.commons.reflection.Reflection;
import hitonoriol.madsand.gamecontent.ContentStorage;
import hitonoriol.madsand.gamecontent.Loadable;
import hitonoriol.madsand.resources.loaders.ContentLoader;
import hitonoriol.madsand.resources.loaders.ContentLoader.ContentMap;
import hitonoriol.madsand.resources.loaders.JsonLoader;
import hitonoriol.madsand.resources.loaders.MapLoader;
import hitonoriol.madsand.resources.loaders.SimpleLoader;
import hitonoriol.madsand.screens.LoadingScreen;
import hitonoriol.madsand.util.Utils;

public class GameAssetManager extends AssetManager {
	private boolean loadingInProgress = false;
	private FileHandleResolver fileHandleResolver;
	private LinkedList<Runnable> completionTasks = new LinkedList<>();
	private Map<String, Runnable> assetTasks = new ConcurrentHashMap<>();
	private LoadingScreen loadingScreen;

	public GameAssetManager(FileHandleResolver resolver) {
		super(null, false);
		setFileHandleResolver(resolver);
		setErrorListener(this::loadingError);
	}

	public GameAssetManager() {
		this(new InternalFileHandleResolver());
	}

	public void setFileHandleResolver(FileHandleResolver resolver) {
		if (loadingInProgress)
			throw new RuntimeException("Can't change the FileHandleResolver while loading is in progress.");

		fileHandleResolver = resolver;
		Content.registerLoaders(this);
		/* Re-register all default loaders with the new FileHandleResolver */
		setLoader(BitmapFont.class, new BitmapFontLoader(resolver));
		setLoader(Music.class, new MusicLoader(resolver));
		setLoader(Pixmap.class, new PixmapLoader(resolver));
		setLoader(Sound.class, new SoundLoader(resolver));
		setLoader(TextureAtlas.class, new TextureAtlasLoader(resolver));
		setLoader(Texture.class, new TextureLoader(resolver));
		setLoader(Skin.class, new SkinLoader(resolver));
		setLoader(ParticleEffect.class, new ParticleEffectLoader(resolver));
		setLoader(
			com.badlogic.gdx.graphics.g3d.particles.ParticleEffect.class,
			new com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader(resolver)
		);
		setLoader(PolygonRegion.class, new PolygonRegionLoader(resolver));
		setLoader(I18NBundle.class, new I18NBundleLoader(resolver));
		setLoader(Model.class, ".g3dj", new G3dModelLoader(new JsonReader(), resolver));
		setLoader(Model.class, ".g3db", new G3dModelLoader(new UBJsonReader(), resolver));
		setLoader(Model.class, ".obj", new ObjLoader(resolver));
		setLoader(ShaderProgram.class, new ShaderProgramLoader(resolver));
		setLoader(Cubemap.class, new CubemapLoader(resolver));
	}

	@Override
	public FileHandleResolver getFileHandleResolver() {
		return fileHandleResolver;
	}

	public void onCompletionLast(Runnable task) {
		completionTasks.addLast(task);
	}

	public void onCompletion(Runnable task) {
		completionTasks.addFirst(task);
	}

	public <T> T loadAndWait(String fileName, Class<T> type, AssetLoaderParameters<T> parameter) {
		load(fileName, type, parameter);
		return finishLoadingAsset(fileName);
	}

	public <T> T loadAndWait(String fileName, Class<T> type) {
		return loadAndWait(fileName, type, null);
	}

	public <T> void loadAndThen(String fileName, Class<T> type, AssetLoaderParameters<T> parameter, Consumer<T> task) {
		assetTasks.put(fileName, () -> task.accept(get(fileName)));
		load(fileName, type, parameter);
	}

	public <T> void loadAndThen(String fileName, Class<T> type, Consumer<T> task) {
		loadAndThen(fileName, type, null, task);
	}

	public final void registerLoader(Loadable loadable) {
		loadable.registerLoader(this);
	}

	public final <T> void jsonLoader(Class<T> type) {
		setLoader(type, new JsonLoader<>(this, type));
	}

	public final <T> void simpleLoader(Class<T> type, SimpleLoader<T> loader) {
		setLoader(type, loader);
	}

	public final <T extends ContentStorage<E>, E extends Enumerable> void contentLoader(
		T storage,
		Class<E> elementType
	) {
		setLoader(Reflection.getClass(storage), new ContentLoader<>(this, storage, elementType));
	}

	/* Register a loader via a HashMap<K, V> "typedef" T:
	 * For each loader, T must be a unique class extending HashMap<K, V> */
	public final <T extends HashMap<K, V>, K, V> void mapLoader(
		Class<T> storageType, Class<K> keyType,
		Class<V> valueType
	) {
		setLoader(storageType, new MapLoader<>(this, storageType, keyType, valueType));
	}

	/* Register a loader via a ContentMap<V> "typedef" T (ContentMaps have a fixed key type):
	 * For each loader, T must be a unique class extending ContentMap<V> */
	public final <T extends ContentMap<V>, V> void contentMapLoader(Class<T> storageType, Class<V> valueType) {
		mapLoader(storageType, Enumerable.idType, valueType);
	}

	/* Register a loader without a HashMap<K, V> "typedef":
	 * This uses filename suffixes to trigger different loaders instead of class
	 * references */
	public final <K, V> void mapLoader(String suffix, Class<K> keyType, Class<V> valueType) {
		setLoader(Reflection.mapClass(HashMap.class), suffix, new MapLoader<>(this, keyType, valueType));
	}

	@Override
	public synchronized <T> void load(String fileName, Class<T> type, AssetLoaderParameters<T> parameter) {
		if (!loadingInProgress)
			startLoading();
		super.load(fileName, type, parameter);
	}

	@Override
	protected <T> void addAsset(String fileName, Class<T> type, T asset) {
		setStatusText("Loaded %s (%s)", fileName, type.getSimpleName());
		super.addAsset(fileName, type, asset);
		if (assetTasks.containsKey(fileName)) {
			assetTasks.get(fileName).run();
			assetTasks.remove(fileName);
		}
	}

	@Override
	public synchronized boolean update() {
		boolean complete = super.update();
		if (!isLoadingScreenAttached() && complete) {
			if (loadingInProgress)
				stopLoading();
		}
		return complete;
	}

	private void startLoading() {
		Utils.out("Starting to load a new batch of assets...");
		loadingInProgress = true;
	}

	private void stopLoading() {
		Utils.out("Stopped loading assets.");
		loadingInProgress = false;
		loadingScreen = null;
		if (assetTasks.isEmpty()) {
			completionTasks.removeIf(task -> {
				task.run();
				return true;
			});
		}
	}

	private void requireLoadingScreen() {
		if (!isLoadingScreenAttached())
			throw new RuntimeException("Loading screen is not attached to this asset loader.");
	}

	public void setStatusText(String format, Object... args) {
		var str = String.format(format, args);
		Utils.out(str);
		if (isLoadingScreenAttached())
			loadingScreen.setStatusText(str);
	}

	public synchronized void completeVisualLoading() {
		requireLoadingScreen();
		stopLoading();
	}

	private void loadingError(AssetDescriptor<?> asset, Throwable throwable) {
		setStatusText("Failed to load: %s", asset.fileName);
		throwable.printStackTrace();
	}

	public void attachLoadingScreen(LoadingScreen screen) {
		loadingScreen = screen;
	}

	private boolean isLoadingScreenAttached() {
		return loadingScreen != null;
	}

	public LoadingScreen createLoadingScreen() {
		var screen = new LoadingScreen(this);
		attachLoadingScreen(screen);
		return screen;
	}

	@SuppressWarnings("rawtypes")
	public static class Dependencies extends Array<AssetDescriptor> {
		public Dependencies(AssetDescriptor... descriptors) {
			super(descriptors);
		}
	}
}
