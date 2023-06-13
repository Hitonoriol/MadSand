package hitonoriol.madsand.gamecontent;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Page;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import hitonoriol.madsand.resources.GameAssetManager;
import hitonoriol.madsand.resources.GameAssetManager.Dependencies;
import hitonoriol.madsand.resources.TextureMap;
import hitonoriol.madsand.resources.loaders.AssetLoader;

public class Textures extends TextureAtlas implements Loadable {
	private TextureMap<String> textureMap;
	private TextureMap<Integer> tiles;
	private TextureMap<Integer> objects;
	private TextureMap<Integer> items;
	private TextureMap<Integer> npcs;
	private boolean initialized = false;

	private final static Textures instance = new Textures();

	private Textures() {}

	public TextureMap<String> textureMap() {
		return textureMap;
	}

	public TextureMap<Integer> tileMap() {
		return tiles;
	}

	public TextureMap<Integer> objectMap() {
		return objects;
	}

	public TextureMap<Integer> itemMap() {
		return items;
	}

	public TextureMap<Integer> npcMap() {
		return npcs;
	}

	public static TextureRegion getTile(int id) {
		return instance.tiles.get(id);
	}

	public static TextureRegion getObject(int id) {
		return instance.objects.get(id);
	}

	public static TextureRegion getItem(int id) {
		return instance.items.get(id);
	}

	public static TextureRegion getNpc(int id) {
		return instance.npcs.get(id);
	}

	public static TextureRegion getTexture(String name) {
		return instance.textureMap.get(name);
	}

	public static Textures get() {
		return instance;
	}

	@Override
	public void registerLoader(GameAssetManager manager) {
		manager.setLoader(Textures.class, new Loader(manager));
	}

	private static class Loader extends AssetLoader<Textures> {
		private TextureAtlasData data;

		public Loader(GameAssetManager manager) {
			super(manager);
		}

		@Override
		public Textures load(
			AssetManager assetManager, String fileName, FileHandle file,
			Parameters<Textures> parameter
		) {
			for (Page page : data.getPages()) {
				var texture = assetManager.get(page.textureFile.path().replaceAll("\\\\", "/"), Texture.class);
				page.texture = texture;
			}

			instance.load(data);
			if (!instance.initialized) {
				instance.initialized = true;
				instance.textureMap = new TextureMap<>(instance);
				instance.tiles = new TextureMap<>(instance, "terrain");
				instance.objects = new TextureMap<>(instance, "obj");
				instance.items = new TextureMap<>(instance, "inv");
				instance.npcs = new TextureMap<>(instance, "npc");
			}
			data = null;
			return instance;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Dependencies getDependencies(String fileName, FileHandle atlasFile, Parameters<Textures> parameter) {
			var imgDir = atlasFile.parent();
			data = new TextureAtlasData(atlasFile, imgDir, false);
			var dependencies = new Dependencies();
			for (Page page : data.getPages()) {
				var params = new TextureParameter();
				params.format = page.format;
				params.genMipMaps = page.useMipMaps;
				params.minFilter = page.minFilter;
				params.magFilter = page.magFilter;
				dependencies.add(new AssetDescriptor(page.textureFile, Texture.class, params));
			}
			return dependencies;
		}
	}
}
