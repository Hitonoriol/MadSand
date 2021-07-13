package hitonoriol.madsand.map;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.lua.LuaLambda;
import hitonoriol.madsand.lua.LuaLambda.LuaPredicate;
import hitonoriol.madsand.util.Utils;

public class MapStructure {
	public int x, y;

	// width & height are specified in each structure's script separately and then passed to the java object
	public int xMax, yMax;
	public int width, height;

	private Map map = MadSand.world().getCurLoc();
	private LuaPredicate builder;

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

	private static final int maxAttempts = 10000;

	public Pair getFreeTile() {
		Pair coords = new Pair();
		Map map = MadSand.world().getCurLoc();
		int attempt = 0;
		do {
			coords.set(randX(), randY());

			if (attempt > maxAttempts)
				break;

			++attempt;
		} while (MadSand.player().at(coords) ||
				!map.isFreeTile(coords));

		return coords;
	}

	public void fillTile(int id) {
		map.fillTile(x, y, width - 1, height - 1, id);
	}

	public void clear() {
		map.fillObject(x, y, width, height, 0);
	}

	public MapStructure setName(String file) {
		builder = LuaLambda.predicate(Lua.loadScript("structure/" + file + ".lua"));
		return this;
	}

	public boolean build() {
		try {
			return builder.test(this);
		} catch (Exception e) { // This means that assert(x + w <= [map width] && y + h <= [map height]) failed
			return false;
		}
	}
}
