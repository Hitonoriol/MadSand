package hitonoriol.madsand.world;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapEntity;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.ByteUtils;
import hitonoriol.madsand.util.Utils;

public class WorldMapSaver {
	final String LOOT_DELIM = "|";
	final int CROP_BLOCK_LEN = 16;
	final int BLOCK_SIZE = 2;
	final int LONG_BLOCK_SIZE = 8;

	private WorldMap worldMap;

	public WorldMapSaver(WorldMap worldMap) {
		setWorldMap(worldMap);
	}

	public WorldMapSaver() {}

	public byte[] locationToBytes(int wx, int wy) {
		Pair coords = new Pair();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		byte[] layer;
		long size;
		try {
			Location location = worldMap.getLocation(coords.set(wx, wy));
			// Header: format version, sector layer count
			stream.write(ByteUtils.encode8(GameSaver.saveFormatVersion));
			stream.write(ByteUtils.encode2(location.getLayerCount()));

			// Save all layers of sector
			for (Entry<Integer, Map> entry : location.layers.entrySet()) {
				layer = sectorToBytes(wx, wy, entry.getKey());
				size = layer.length;
				stream.write(ByteUtils.encode2(entry.getKey()));
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
		Resources.mapper.writeValue(GameSaver.getLocationFile(wx, wy), worldMap.getLocation(new Pair(wx, wy)));
	}

	public void bytesToLocation(byte[] location, int wx, int wy) throws Exception {
		ByteArrayInputStream stream = new ByteArrayInputStream(location);
		long layersz;
		long saveVersion;
		byte[] block = new byte[BLOCK_SIZE];
		byte[] longBlock = new byte[LONG_BLOCK_SIZE];

		saveVersion = loadNextLongBlock(stream, longBlock);
		if (saveVersion != GameSaver.saveFormatVersion)
			throw (new Exception("Incompatible save format version!"));

		int layers = loadNextBlock(stream, block);
		int layer;

		byte[] sectorContents;
		for (int i = 0; i < layers; ++i) {
			layer = loadNextBlock(stream, block);
			layersz = loadNextLongBlock(stream, longBlock);
			sectorContents = new byte[(int) layersz];
			stream.read(sectorContents);
			bytesToSector(sectorContents, wx, wy, layer);
		}
	}

	public Location loadLocationInfo(int wx, int wy) throws Exception {
		Location location = Resources.mapper.readValue(GameSaver.getLocationFile(wx, wy), Location.class);
		worldMap.addLocation(new Pair(wx, wy), location);
		Utils.dbg("{%X} Loaded location {%X} info @ (%d, %d)", worldMap.hashCode(), location.hashCode(), wx, wy);
		return location;
	}

	byte[] sectorToBytes(int wx, int wy, int layer) {
		try {
			Utils.out("Saving sector [%d, %d] Layer: %d", wx, wy, layer);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Pair loc = new Pair(wx, wy);
			Map map = worldMap.getLocation(loc).getLayer(layer);
			byte mapProperties[] = Resources.mapper.writeValueAsString(map).getBytes();

			Resources.mapper.writerFor(Resources.getMapType(Pair.class, AbstractNpc.class))
					.writeValue(new File(GameSaver.getNpcFile(wx, wy, layer)), map.getNpcs());

			Resources.mapper.writeValue(new File(GameSaver.getTimeDependentFile(wx, wy, layer)),
					map.getTimeDependentMapEntities());

			int xsz = map.getWidth();
			int ysz = map.getHeight();
			// header: width, height
			stream.write(ByteUtils.encode2(xsz));
			stream.write(ByteUtils.encode2(ysz));

			// Misc map properties as a json string
			stream.write(ByteUtils.encode8(mapProperties.length));
			stream.write(mapProperties);

			MapObject obj = new MapObject();
			ByteArrayOutputStream lootStream = new ByteArrayOutputStream();
			byte loot[];
			for (int y = 0; y < ysz; ++y) {
				for (int x = 0; x < xsz; ++x) {
					// Save tiles
					stream.write(ByteUtils.encode2(map.getTile(x, y).id));
					stream.write(ByteUtils.encode2(map.getTile(x, y).visited ? 1 : 0));

					// Save objects
					obj = map.getObject(x, y);
					stream.write(ByteUtils.encode2(obj.id));
					stream.write(ByteUtils.encode2(obj.hp));
					stream.write(ByteUtils.encode2(obj.maxHp));
				}
			}

			// Save loot
			loot = Resources.mapper.writerFor(Resources.getMapType(Pair.class, Loot.class))
					.writeValueAsString(map.getLoot()).getBytes();
			lootStream.write(ByteUtils.encode8(loot.length));
			lootStream.write(loot);

			// Get all bytes from streams & concat them into one array
			byte[] _loot = lootStream.toByteArray();
			lootStream.close();
			byte ret[] = stream.toByteArray();
			stream.close();

			ret = ByteUtils.concat(ret, _loot);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			Utils.die();
			return null;
		}
	}

	void bytesToSector(byte[] sector, int wx, int wy, int layer) {
		try {
			Utils.dbg("Loading sector (%d, %d) Layer %d...", wx, wy, layer);
			ByteArrayInputStream stream = new ByteArrayInputStream(sector);
			byte[] block = new byte[BLOCK_SIZE];
			byte[] longBlock = new byte[LONG_BLOCK_SIZE];
			// Read header
			int xsz = loadNextBlock(stream, block);
			int ysz = loadNextBlock(stream, block);
			byte mapProperties[] = new byte[(int) loadNextLongBlock(stream, longBlock)];
			stream.read(mapProperties);

			Pair loc = new Pair(wx, wy);
			Map map = Resources.mapper.readValue(new String(mapProperties), Map.class);
			map.setSize(xsz, ysz);
			map.purge();

			// Load time dependent entities
			HashMap<Pair, MapEntity> timeDepMap = Resources.mapper.readValue(
					new File(GameSaver.getTimeDependentFile(wx, wy, layer)),
					Resources.getMapType(Pair.class, MapEntity.class));
			map.setTimeDependentMapEntities(timeDepMap);

			// Load NPCs
			HashMap<Pair, AbstractNpc> npcs = Resources.mapper.readValue(
					new File(GameSaver.getNpcFile(wx, wy, layer)),
					Resources.getMapType(Pair.class, AbstractNpc.class));
			npcs.forEach((coords, npc) -> npc.postLoadInit());
			map.setNpcs(npcs);

			// Load tiles & objects
			for (int y = 0; y < ysz; ++y) {
				for (int x = 0; x < xsz; ++x) {
					map.addTile(x, y, loadNextBlock(stream, block), true);
					map.getTile(x, y).visited = loadNextBlock(stream, block) != 0;
					map.addObject(x, y, loadNextBlock(stream, block), false);
					map.getObject(x, y).hp = loadNextBlock(stream, block);
					map.getObject(x, y).maxHp = loadNextBlock(stream, block);
				}
			}

			// Load loot
			byte[] lootNode = new byte[(int) loadNextLongBlock(stream, longBlock)];
			stream.read(lootNode);
			HashMap<Pair, Loot> mapLoot = Resources.mapper.readValue(new String(lootNode),
					Resources.getMapType(Pair.class, Loot.class));
			map.setLoot(mapLoot);
			stream.close();

			// Add self to Location list
			map.postLoadInit();
			worldMap.addMap(loc, layer, map);
		} catch (Exception e) {
			e.printStackTrace();
			Utils.die();
		}
	}

	private long loadNextLongBlock(ByteArrayInputStream stream, byte[] buffer) throws Exception {
		stream.read(buffer);
		return ByteUtils.decode8(buffer);
	}

	private int loadNextBlock(ByteArrayInputStream stream, byte[] buffer) throws Exception {
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
