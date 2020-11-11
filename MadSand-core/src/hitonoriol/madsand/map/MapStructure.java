package hitonoriol.madsand.map;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;

public class MapStructure {
	public int x, y;
	// width & height are specified in each structure's script separately
	LuaValue script;

	public MapStructure(int x, int y) {
		setCoords(x, y);
	}

	public MapStructure(Pair coords) {
		setCoords(coords);
	}

	public MapStructure() {
		this(0, 0);
	}

	public MapStructure setCoords(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public MapStructure setCoords(Pair coords) {
		this.x = coords.x;
		this.y = coords.y;
		return this;
	}

	public MapStructure setName(String file) {
		script = LuaUtils.loadScript("structure/" + file + ".lua");
		return this;
	}

	public boolean build() {
		try {
			return script.call(CoerceJavaToLua.coerce(this)).toboolean();
		} catch (Exception e) { // This means that assert(x + w <= [map width] && y + h <= [map height]) failed
			Utils.out("Oopsie, couldn't build structure: " + script);
			return false;
		}
	}
}
