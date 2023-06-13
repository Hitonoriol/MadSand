package hitonoriol.madsand.gamecontent;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import hitonoriol.madsand.resources.GameAssetManager;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public interface Loadable {
	void registerLoader(GameAssetManager manager);
}
