package hitonoriol.madsand.world;

import static hitonoriol.madsand.resources.Resources.getMapReader;
import static hitonoriol.madsand.resources.Resources.getMapper;
import static hitonoriol.madsand.resources.Resources.readMap;
import static hitonoriol.madsand.resources.Resources.save;
import static hitonoriol.madsand.resources.Resources.saveMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapEntity;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.util.ByteUtils;
import hitonoriol.madsand.util.Utils;

public class WorldMapSaver {
	private final static int BLOCK_SIZE = 2, LONG_BLOCK_SIZE = 8;
	private final byte[] block = new byte[BLOCK_SIZE];
	private final byte[] longBlock = new byte[LONG_BLOCK_SIZE];

	private WorldMap worldMap;

	public WorldMapSaver(WorldMap worldMap) {
		setWorldMap(worldMap);
	}

	public WorldMapSaver() {}

	public byte[] locationToBytes(int wx, int wy) {
		Pair coords = new Pair();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			Location location = worldMap.getLocation(coords.set(wx, wy));
			// Header: format version, sector layer count
			stream.write(ByteUtils.encode8(GameSaver.saveFormatVersion));
			stream.write(ByteUtils.encode2(location.getLayerCount()));

			// Save all layers of sector
			for (int layerNum : location.getLayers().keySet()) {
				byte[] layer = sectorToBytes(wx, wy, layerNum);
				long size = layer.length;
				stream.write(ByteUtils.encode2(layerNum));
				stream.write(ByteUtils.encode8(size));
				stream.write(layer);
			}
			return stream.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			Utils.die();
			return null;
		}
	}

	public void saveLocationInfo(int wx, int wy) throws Exception {
		getMapper().writeValue(GameSaver.getLocationFile(wx, wy), worldMap.getLocation(new Pair(wx, wy)));
	}

	public void bytesToLocation(byte[] locationData, int wx, int wy) throws Exception {
		ByteArrayInputStream stream = new ByteArrayInputStream(locationData);

		long saveVersion = nextLongBlock(stream, longBlock);
		if (saveVersion != GameSaver.saveFormatVersion)
			throw (new Exception("Incompatible save format version!"));

		int layers = nextBlock(stream, block);
		for (int i = 0; i < layers; ++i) {
			int layer = nextBlock(stream, block);
			long layerLen = nextLongBlock(stream, longBlock);
			byte[] sectorContents = new byte[(int) layerLen];
			stream.read(sectorContents);
			bytesToSector(sectorContents, wx, wy, layer);
		}
	}

	public Location loadLocationInfo(int wx, int wy) throws Exception {
		Location location = getMapper().readValue(GameSaver.getLocationFile(wx, wy), Location.class);
		worldMap.addLocation(new Pair(wx, wy), location);
		Utils.dbg("{%X} Loaded location {%X} info @ (%d, %d)", worldMap.hashCode(), location.hashCode(), wx, wy);
		return location;
	}

	private byte[] sectorToBytes(int wx, int wy, int layer) {
		try {
			Utils.out("Saving sector [%d, %d] Layer: %d", wx, wy, layer);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Map map = worldMap.getLocation(new Pair(wx, wy)).getLayer(layer);

			saveMap(GameSaver.getNpcFile(wx, wy, layer), map.getNpcs(), Pair.class, AbstractNpc.class);
			save(GameSaver.getTimeDependentFile(wx, wy, layer), map.getTimeDependentMapEntities());

			int xsz = map.getWidth();
			int ysz = map.getHeight();
			// header: width, height
			stream.write(ByteUtils.encode2(xsz));
			stream.write(ByteUtils.encode2(ysz));

			// Misc map properties as a json string
			writeStringBlock(stream, getMapper().writeValueAsString(map));

			for (int y = 0; y < ysz; ++y) {
				for (int x = 0; x < xsz; ++x) {
					// Save tile
					Tile tile = map.getTile(x, y);
					stream.write(ByteUtils.encode2(tile.id));
					stream.write(ByteUtils.encode2(tile.visited ? 1 : 0));

					// Save object
					MapObject obj = map.getObject(x, y);
					stream.write(ByteUtils.encode2(obj.id));
					stream.write(ByteUtils.encode2(obj.hp));
					stream.write(ByteUtils.encode2(obj.maxHp));
				}
			}

			// Save loot
			writeStringBlock(stream, saveMap(map.getLoot(), Loot.class));

			// Save modified MapObject names
			HashMap<Pair, String> modifiedObjNames = new HashMap<>();
			map.getObjects().stream()
					.filter(obj -> !obj.getName().equals(ObjectProp.getName(obj.id)))
					.forEach(obj -> modifiedObjNames.put(obj.getPosition(), obj.getName()));
			writeStringBlock(stream, saveMap(modifiedObjNames, String.class));
			return stream.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			Utils.die();
			return null;
		}
	}

	private void bytesToSector(byte[] sectorData, int wx, int wy, int layer) {
		try {
			Utils.dbg("Loading sector (%d, %d) Layer %d...", wx, wy, layer);
			ByteArrayInputStream stream = new ByteArrayInputStream(sectorData);
			// Read header
			int xsz = nextBlock(stream, block);
			int ysz = nextBlock(stream, block);

			Pair worldPos = new Pair(wx, wy);
			Map map = getMapper().readValue(nextStringBlock(stream), Map.class);
			map.setSize(xsz, ysz);
			map.purge();

			// Load time dependent entities
			HashMap<Pair, MapEntity> timeDepMap = readMap(GameSaver.getTimeDependentFile(wx, wy, layer),
					MapEntity.class);
			map.setTimeDependentMapEntities(timeDepMap);

			// Load NPCs
			HashMap<Pair, AbstractNpc> npcs = readMap(GameSaver.getNpcFile(wx, wy, layer), AbstractNpc.class);
			npcs.forEach((coords, npc) -> npc.postLoadInit());
			map.setNpcs(npcs);

			// Load tiles & objects
			for (int y = 0; y < ysz; ++y) {
				for (int x = 0; x < xsz; ++x) {
					map.addTile(x, y, nextBlock(stream, block), true);
					map.getTile(x, y).visited = nextBlock(stream, block) != 0;

					map.addObject(x, y, nextBlock(stream, block), false);
					map.getObject(x, y).hp = nextBlock(stream, block);
					map.getObject(x, y).maxHp = nextBlock(stream, block);
				}
			}

			// Load loot
			HashMap<Pair, Loot> mapLoot = getMapReader(Loot.class).readValue(nextStringBlock(stream));
			map.setLoot(mapLoot);

			// Load modified MapObject names
			HashMap<Pair, String> modifiedObjNames = getMapReader(String.class).readValue(nextStringBlock(stream));
			modifiedObjNames.forEach((position, name) -> map.getObject(position).name = name);

			// Add loaded layer map to location @(wx, wy)
			map.postLoadInit();
			worldMap.addMap(worldPos, layer, map);
		} catch (Exception e) {
			e.printStackTrace();
			Utils.die();
		}
	}

	/* Writes String as bytes to stream as a sequence: [(long) string length][string bytes] */
	private void writeStringBlock(ByteArrayOutputStream stream, String str) throws Exception {
		byte bytes[] = str.getBytes();
		stream.write(ByteUtils.encode8(bytes.length));
		stream.write(bytes);
	}

	/* Reads byte sequence: [(long) length of contents][contents] and returns contents as String*/
	private String nextStringBlock(ByteArrayInputStream stream) throws Exception {
		byte bytes[] = new byte[(int) nextLongBlock(stream, longBlock)];
		stream.read(bytes);
		return new String(bytes);
	}

	private long nextLongBlock(ByteArrayInputStream stream, byte[] buffer) throws Exception {
		stream.read(buffer);
		return ByteUtils.decode8(buffer);
	}

	private int nextBlock(ByteArrayInputStream stream, byte[] buffer) throws Exception {
		stream.read(buffer);
		return ByteUtils.decode2(buffer);
	}

	public void setWorldMap(WorldMap worldMap) {
		this.worldMap = worldMap;
	}

	public WorldMap getWorldMap() {
		return worldMap;
	}
}
