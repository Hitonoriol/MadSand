package hitonoriol.madsand;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

import hitonoriol.madsand.util.Utils;

public class TextureContainer {
	private TextureAtlas atlas;
	private Map<Integer, AtlasRegion> textureCache = new HashMap<>(1);

	public TextureContainer(TextureAtlas atlas) {
		this.atlas = atlas;
	}

	public AtlasRegion get(int id) {
		if (textureCache.containsKey(id))
			return textureCache.get(id);
		else {
			AtlasRegion region = atlas.findRegion(Utils.str(id));
			textureCache.put(id, region);
			return region;
		}
	}
}
