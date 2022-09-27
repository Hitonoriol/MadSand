package hitonoriol.madsand.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.gamecontent.Objects;
import hitonoriol.madsand.gamecontent.Tiles;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.util.Utils;

public class MapTest {
	Map map = MadSand.world().getCurLoc();

	@Test
	void addRemoveObject() {
		MapObject expectedObject = MapObject.create(pickObjectId());
		Pair objectPos = map.randPlaceObject(expectedObject.id()).copy();
		assertEquals(expectedObject, map.getObject(objectPos), "Object should be present on map");

		map.delObject(objectPos);
		assertEquals(Map.nullObject, map.getObject(objectPos), "Object should be removed from map");
	}

	@Test
	void addRemoveTile() {
		Tile expectedTile = new Tile(pickTileId());
		Pair tilePos = map.randPlaceTile(expectedTile.id()).copy();
		assertEquals(expectedTile, map.getTile(tilePos), "Tile should be present on map");

		map.delTile(tilePos);
		assertEquals(map.defTile, map.getTile(tilePos).id(), "Tile should be replaced by default map tile");
	}

	@Test
	void spawnDespawnNpc() {
		AbstractNpc npc = map.spawnNpc(1);
		Pair npcPos = npc.getPosition();
		assertNotEquals(Map.nullNpc, npc, "Npc should be able to spawn");
		assertEquals(npc, map.getNpc(npcPos), "Npc should be present on map");

		npc.die(); /* How cruel. RIP, dummy NPC #9384783 */
		assertEquals(Map.nullNpc, map.getNpc(npcPos), "Npc should be removed from map");
	}

	private int pickTileId() {
		int tileId;
		do
			tileId = Utils.randElement(Tiles.all().get().keySet());
		while (tileId == map.defTile);
		return tileId;
	}

	private int pickObjectId() {
		return Utils.randElement(Objects.all().get().keySet(), 1);
	}
}
