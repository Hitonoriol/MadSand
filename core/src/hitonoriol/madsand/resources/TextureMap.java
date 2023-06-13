package hitonoriol.madsand.resources;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

import hitonoriol.madsand.util.Utils;

public class TextureMap<T> {
	protected final TextureAtlas atlas;
	private final String subdir;
	private final Map<T, AtlasRegion> textureCache = new HashMap<>(1);

	public TextureMap(TextureAtlas atlas, String subdir) {
		this.atlas = atlas;
		this.subdir = (subdir != null) ? (subdir + "/") : null;
	}

	public TextureMap(TextureAtlas atlas) {
		this(atlas, null);
	}

	private AtlasRegion getAndCache(T id) {
		var name = id.toString();
		var region = atlas.findRegion(subdir != null ? (subdir + name) : name);
		textureCache.put(id, region);
		if (region == null)
			Utils.dbg("Failed to find region: %s", name);
		return region;
	}

	public AtlasRegion get(T id) {
		if (textureCache.containsKey(id))
			return textureCache.get(id);
		else
			return getAndCache(id);
	}

	public int size() {
		return textureCache.size();
	}
}
