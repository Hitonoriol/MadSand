package hitonoriol.madsand.map;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;

public class MapStructure {
	public int x, y;

	// width & height are specified in each structure's script separately and then passed to the java object
	public int xMax, yMax;
	public int width, height;

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
		return setCoords(coords.x, coords.y);
	}

	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
		xMax = x + width - 1;
		yMax = y + height - 1;
	}

	public int randX() {
		return Utils.rand(x, xMax);
	}

	public int randY() {
		return Utils.rand(y, yMax);
	}

	public Pair getFreeTile() {
		Pair coords = new Pair();
		Map map = MadSand.world.getCurLoc();

		do {
			coords.set(randX(), randY());
		} while (map.objectExists(coords.x, coords.y) || !map.getNpc(coords).equals(Map.nullNpc));

		return coords;
	}
	
	public void clear() {
		MadSand.world.getCurLoc().fillObject(x, y, width, height, 0);
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
