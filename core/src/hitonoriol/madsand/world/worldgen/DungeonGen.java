package hitonoriol.madsand.world.worldgen;

import java.util.ArrayList;
import java.util.List;

import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.commons.reflection.Reflection;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.Location;
import hitonoriol.madsand.world.World;

public class DungeonGen extends DungeonGenerator {
	private static final float DUNGEON_CORRIDOR_LEVEL = 0.0f;
	private static final float DUNGEON_WALL_LEVEL = 1.0f;
	private static final float DUNGEON_ROOM_LEVEL = 0.5f;

	private Map map;
	private List<Room> roomList;

	public DungeonGen(Map map) {
		this.map = map;
	}

	public void generate(DungeonPreset dungeon, int depth) {
		var contents = dungeon.dungeonContents;

		int floorNumber = depth;
		DungeonFloorContents floor;
		for (int i = 0; i < contents.size(); ++i) {
			floor = contents.get(i);
			if (floor.fromFloor <= depth)
				floorNumber = i;
		}

		var curDungeonFloor = contents.get(floorNumber).contents;

		var mobs = curDungeonFloor.mobs;
		var loot = curDungeonFloor.loot;

		map.rollSize(curDungeonFloor.minSize, curDungeonFloor.maxSize);
		map.fillTile();
		map.editable = false;
		int w = map.getWidth(), h = map.getHeight();
		final var grid = new Grid(w, h);

		super.setRoomGenerationAttempts(World.DEFAULT_MAPSIZE);
		super.setMaxRoomSize(dungeon.maxRoomSize);
		super.setTolerance(dungeon.tolerance);
		super.setMinRoomSize(dungeon.minRoomSize);
		super.generate(grid);

		var doors = new ArrayList<Pair>();
		int y = 0, x = 0;
		int mobCount = 0;

		while (y < h) {
			while (x < w) {
				if (grid.get(x, y) == DUNGEON_CORRIDOR_LEVEL) { // corridors
					map.delObject(x, y);
					map.addTile(x, y, dungeon.corridorTile, true);

					if (
						Utils.percentRoll(curDungeonFloor.mobCorridorProbability)
							&& mobCount < curDungeonFloor.maxMobs
					) {

						map.spawnNpc(Utils.randElement(mobs), x, y);
						++mobCount;
					}

					if (Utils.percentRoll(curDungeonFloor.lootCorridorProbability)) {
						map.putLoot(x, y, loot.rollItems());
						luckLootRoll(x, y);
					}
				}

				if (grid.get(x, y) == DUNGEON_WALL_LEVEL) { // walls
					map.addObject(x, y, dungeon.wallObject);
					map.addTile(x, y, dungeon.wallTile);
				}

				if (grid.get(x, y) == DUNGEON_ROOM_LEVEL) { // rooms
					map.delObject(x, y);
					map.addTile(x, y, dungeon.roomTile, true);

					if (Utils.percentRoll(curDungeonFloor.mobProbability) && mobCount < curDungeonFloor.maxMobs) {
						map.spawnNpc(Utils.randElement(mobs), x, y);
						++mobCount;
					}

					if (Utils.percentRoll(curDungeonFloor.lootProbability)) {
						map.putLoot(x, y, loot.rollItems());
						luckLootRoll(x, y);
					}

					if (map.spawnPoint.equals(Pair.nullPair)) {
						map.spawnPoint = new Pair(x, y);
						if (depth > Location.LAYER_BASE_DUNGEON + 1)
							map.addObject(x, y, dungeon.staircaseUpObject);
					}
				}

				if (isDoorway(grid, x, y, w, h)) // door
					doors.add(new Pair(x, y));

				x++;
			}
			x = 0;
			y++;
		}

		placeSpecialMobs(curDungeonFloor);
		placeDoors(curDungeonFloor.doorObject, doors);

		if (
			depth >= Math.min(
				DungeonPreset.EXIT_FLOOR_BASE + MadSand.player().dungeonsCompleted(),
				DungeonPreset.EXIT_FLOOR_MAX
			)
		)
			placeExit(dungeon);
		else
			placeObjectInRoom(dungeon.staircaseDownObject);
	}

	@Override
	protected void reset() {
		/* DungeonGenerator's `rooms` list is not exposed in any way despite being the
		 * only place where room params are stored, so the only way to access it is copy
		 * it here via reflection, as it gets cleared after grid generation. */
		roomList = Reflection.readField(this, "rooms");
		if (!roomList.isEmpty())
			roomList = new ArrayList<>(roomList);
		super.reset();
	}

	private void luckLootRoll(int x, int y) {
		if (MadSand.player().stats().luckRoll())
			map.putLoot(x, y);
	}

	private void placeExit(DungeonPreset dungeon) {
		MadSand.notice("You feel that exit to the surface is somewhere nearby...");
		var coords = randomRoomPoint();
		map.addObject(coords.x, coords.y, dungeon.exitObject);
	}

	private void placeDoors(int id, ArrayList<Pair> doors) {
		for (Pair coords : doors)
			map.addObject(coords.x, coords.y, id);
		doors.clear();
	}

	private void placeSpecialMobs(DungeonContents contents) {
		Room room;
		Pair coords;
		int npcId;
		int specialTile, specialWallObject;
		int quantity = Utils.rand(1, contents.specialMobsMax);

		for (int i = 0; i < quantity; ++i) {
			room = getRandomRoom();
			coords = new Pair(-1, -1);
			npcId = Utils.randElement(contents.specialMobs);

			while (!map.spawnNpc(npcId, coords.x, coords.y))
				coords = randomRoomPoint(room, coords);

			specialTile = Utils.randElement(contents.specialRoomTiles);
			specialWallObject = Utils.randElement(contents.specialRoomWalls);
			map.fillTile(room.getX(), room.getY(), room.getWidth(), room.getHeight(), specialTile);
			map.drawObjectRectangle(
				room.getX() - 1, room.getY() - 1, room.getWidth() + 1, room.getHeight() + 1,
				specialWallObject
			);

			Utils.dbg("Spawned special mob (id: " + npcId + ") at " + coords);
		}

	}

	private void placeObjectInRoom(int id) {
		var roomCoords = randomRoomPoint();
		map.addObject(roomCoords.x, roomCoords.y, id);
	}

	private Pair randomRoomPoint(Room room, Pair coords) {
		int roomX = room.getX(), roomY = room.getY();

		int x = Utils.rand(roomX, roomX + room.getWidth() - 1);
		int y = Utils.rand(roomY, roomY + room.getHeight() - 1);

		return coords.set(x, y);
	}

	private Pair randomRoomPoint(Pair coords) {
		return randomRoomPoint(getRandomRoom(), coords);

	}

	private Pair randomRoomPoint() {
		return randomRoomPoint(new Pair());
	}

	private Room getRandomRoom() {
		return Utils.randElement(roomList);
	}

	private boolean isDoorway(Grid grid, int x, int y, int xsz, int ysz) {
		float up, down, left, right;
		up = down = left = right = -1;
		float current = grid.get(x, y);

		if (y + 1 < ysz)
			up = grid.get(x, y + 1);
		if (y - 1 >= 0)
			down = grid.get(x, y - 1);
		if (x - 1 >= 0)
			left = grid.get(x - 1, y);
		if (x + 1 < xsz)
			right = grid.get(x + 1, y);

		return (current == DUNGEON_CORRIDOR_LEVEL)
			&& (up == DUNGEON_ROOM_LEVEL
				|| down == DUNGEON_ROOM_LEVEL
				|| left == DUNGEON_ROOM_LEVEL
				|| right == DUNGEON_ROOM_LEVEL);
	}
}
