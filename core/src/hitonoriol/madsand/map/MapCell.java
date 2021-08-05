package hitonoriol.madsand.map;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.gui.widgets.overlay.GameContextMenu;
import hitonoriol.madsand.map.object.Crop;
import hitonoriol.madsand.map.object.ItemFactory;
import hitonoriol.madsand.map.object.MapObject;

public class MapCell {
	private Tile tile;
	private Loot loot;
	private MapObject object;
	private AbstractNpc npc;
	private boolean validCoords;
	private Pair coords = new Pair();

	public MapCell(int x, int y) {
		coords.set(x, y);
	}

	/* Init with non-null values from empty map when using no-arg constructor */
	public MapCell() {
		Map.nullMap.getMapCell(this);
	}

	public boolean hasLoot() {
		return loot != Map.nullLoot;
	}

	public boolean hasObject() {
		return !object.isEmpty();
	}

	public boolean hasCrop() {
		return !getCrop().isEmpty();
	}

	public boolean hasNpc() {
		return !npc.isEmpty();
	}

	public boolean hasItemFactory() {
		return object.is(ItemFactory.class);
	}

	public boolean hasPlayer() {
		return MadSand.player().at(coords);
	}

	public boolean isOccupied() {
		return hasNpc() ||
				hasObject() ||
				tile.hasFishingSpot() ||
				hasPlayer();
	}

	public Tile getTile() {
		return tile;
	}

	public Loot getLoot() {
		return loot;
	}

	public MapObject getObject() {
		return object;
	}

	public Crop getCrop() {
		return object.as(Crop.class).orElse(Map.nullCrop);
	}

	public ItemProducer getItemFactoryProducer() {
		return object.as(ItemFactory.class)
				.map(itemFactory -> itemFactory.getItemProducer())
				.orElse(null);
	}

	public AbstractNpc getNpc() {
		return npc;
	}

	public Pair getCoords() {
		return coords;
	}

	public MapCell setCoords(int x, int y) {
		coords.set(x, y);
		return this;
	}

	public boolean coordsValid() {
		return validCoords;
	}

	MapCell get(Map map) {
		tile = map.getTile(coords);
		loot = map.getLoot(coords);
		object = map.getObject(coords);
		npc = map.getNpc(coords);
		validCoords = map.validCoords(coords);
		return this;
	}

	public void populateContextMenu(GameContextMenu menu) {
		if (hasObject())
			object.populateContextMenu(menu);

		if (hasNpc())
			npc.populateContextMenu(menu);
	}

	/* Populates MapCell with cell contents from currently loaded map */
	public static MapCell get(MapCell cell) {
		return MadSand.world().getCurLoc().getMapCell(cell);
	}

	public static MapCell get(MapCell cell, int x, int y) {
		return get(cell.setCoords(x, y));
	}
}
