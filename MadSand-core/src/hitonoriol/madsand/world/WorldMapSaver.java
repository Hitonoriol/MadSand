package hitonoriol.madsand.world;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.map.Crop;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.map.ProductionStation;

public class WorldMapSaver {
	final String LOOT_DELIM = "|";
	final int CROP_BLOCK_LEN = 16;
	final int BLOCK_SIZE = 2;
	final int LONG_BLOCK_SIZE = 8;

	WorldMap worldMap;

	public WorldMapSaver(WorldMap worldMap) {
		this.worldMap = worldMap;
	}

	public byte[] locationToBytes(int wx, int wy) {
		Pair coords = new Pair();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		byte[] layer;
		long size;
		try {
			Location location = worldMap.getLocation(coords.set(wx, wy));
			//Header: format version, sector layer count
			stream.write(GameSaver.encode8(GameSaver.saveFormatVersion));
			stream.write(GameSaver.encode2(location.getLayerCount()));

			//Save all layers of sector
			for (Entry<Integer, Map> entry : location.layers.entrySet()) {
				layer = sectorToBytes(wx, wy, entry.getKey());
				size = layer.length;
				stream.write(GameSaver.encode2(entry.getKey()));
				stream.write(GameSaver.encode8(size));
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

	public void loadLocationInfo(int wx, int wy) throws Exception {
		Location location = Resources.mapper.readValue(GameSaver.getLocationFile(wx, wy), Location.class);
		worldMap.locations.put(new Pair(wx, wy), location);
	}

	byte[] sectorToBytes(int wx, int wy, int layer) {
		try {
			Utils.out("Saving sector " + wx + ", " + wy + " Layer: " + layer);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Pair loc = new Pair(wx, wy);
			Map map = worldMap.locations.get(loc).getLayer(layer);

			Resources.mapper.writeValue(
					new File(GameSaver.getNpcFile(wx, wy, layer)),
					map.getNpcs());

			Resources.mapper.writeValue(
					new File(GameSaver.getProdStationFile(wx, wy, layer)),
					map.getMapProductionStations());

			int xsz = map.getWidth();
			int ysz = map.getHeight();
			// header: isEditable, width, height,spawnpoint(x, y) - for dungeon levels,
			// default tile, default object
			stream.write(GameSaver.encode2(Utils.val(map.editable)));
			stream.write(GameSaver.encode2(xsz));
			stream.write(GameSaver.encode2(ysz));
			stream.write(GameSaver.encode2(map.spawnPoint.x));
			stream.write(GameSaver.encode2(map.spawnPoint.y));
			stream.write(GameSaver.encode2(map.defTile));
			stream.write(GameSaver.encode2(map.defObject));
			MapObject obj = new MapObject();

			ByteArrayOutputStream lootStream = new ByteArrayOutputStream();
			ByteArrayOutputStream cropStream = new ByteArrayOutputStream();
			String loot;
			Crop crop;
			int cropBlocks = 0;
			for (int y = 0; y < ysz; ++y) {
				for (int x = 0; x < xsz; ++x) {
					// Save tiles
					stream.write(GameSaver.encode2(map.getTile(x, y).id));
					stream.write(GameSaver.encode2(map.getTile(x, y).visited ? 1 : 0));

					// Save objects
					obj = map.getObject(x, y);
					stream.write(GameSaver.encode2(obj.id));
					stream.write(GameSaver.encode2(obj.hp));

					// Save loot
					loot = map.getLoot(x, y).getContents();
					lootStream.write(GameSaver.encode2(loot.length()));
					lootStream.write(loot.getBytes());

					// Save crops
					crop = map.getCrop(x, y);
					if (crop.id == Map.nullCrop.id)
						continue;
					cropStream.write(GameSaver.encode2(x));
					cropStream.write(GameSaver.encode2(y));
					cropStream.write(GameSaver.encode2(crop.id));
					cropStream.write(GameSaver.encode8(crop.plantTime));
					cropStream.write(GameSaver.encode2(crop.curStage));
					++cropBlocks;
				}
			}
			byte[] cropCount = GameSaver.encode2(cropBlocks);
			byte[] _crops = cropStream.toByteArray();
			cropStream.close();

			byte[] _loot = lootStream.toByteArray();
			lootStream.close();
			byte ret[] = stream.toByteArray();
			stream.close();

			ret = GameSaver.concat(ret, _loot, cropCount, _crops);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			Utils.die();
			return null;
		}
	}

	void bytesToSector(byte[] sector, int wx, int wy, int layer) {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(sector);
			byte[] block = new byte[BLOCK_SIZE];
			byte[] longBlock = new byte[LONG_BLOCK_SIZE];
			// Read header
			boolean editable = Utils.bool(loadNextBlock(stream, block));
			int xsz = loadNextBlock(stream, block);
			int ysz = loadNextBlock(stream, block);
			int spawnX = loadNextBlock(stream, block);
			int spawnY = loadNextBlock(stream, block);
			int defTile = loadNextBlock(stream, block);
			int defObject = loadNextBlock(stream, block);

			Pair loc = new Pair(wx, wy);
			Map map = new Map(xsz, ysz);
			map.purge();
			map.editable = editable;
			map.spawnPoint = new Pair(spawnX, spawnY);

			// Load NPCs
			HashMap<Pair, Npc> npcs = Resources.mapper.readValue(
					new File(GameSaver.getNpcFile(wx, wy, layer)),
					Resources.getMapType(Pair.class, Npc.class));

			for (Entry<Pair, Npc> npc : npcs.entrySet()) {
				npc.getValue().loadSprite();
				npc.getValue().initStatActions();
			}

			map.setNpcs(npcs);

			// Load tiles & objects
			map.defTile = defTile;
			map.defObject = defObject;

			for (int y = 0; y < ysz; ++y) {
				for (int x = 0; x < xsz; ++x) {
					stream.read(block);
					map.addTile(x, y, GameSaver.decode2(block), true);
					stream.read(block);
					map.getTile(x, y).visited = GameSaver.decode2(block) != 0;
					stream.read(block);
					map.addObject(x, y, GameSaver.decode2(block));
					stream.read(block);
					map.getObject(x, y).hp = GameSaver.decode2(block);
				}
			}

			//Load production stations
			HashMap<Pair, ProductionStation> prodStations = Resources.mapper.readValue(
					new File(GameSaver.getProdStationFile(wx, wy, layer)),
					Resources.getMapType(Pair.class, ProductionStation.class));

			map.setMapProductionStations(prodStations);

			// Load loot
			String loot;
			byte[] _len = new byte[BLOCK_SIZE];
			int len;
			byte[] node;
			for (int y = 0; y < ysz; ++y) {
				for (int x = 0; x < xsz; ++x) {
					stream.read(_len);
					len = GameSaver.decode2(_len);
					node = new byte[len];
					stream.read(node);
					loot = new String(node);
					Loot.addLootQ(loot, x, y, map);
				}
			}

			//Load crops
			int cropsCount = loadNextBlock(stream, block);
			int x, y, id, stage;
			long ptime;
			Crop crop;
			for (int i = 0; i < cropsCount; ++i) {
				x = loadNextBlock(stream, block);
				y = loadNextBlock(stream, block);
				id = loadNextBlock(stream, block);
				ptime = loadNextLongBlock(stream, longBlock);
				stage = loadNextBlock(stream, block);
				crop = new Crop(id, ptime, stage);
				map.putCrop(x, y, crop);
			}

			stream.close();

			// Add self to Location list
			worldMap.addMap(loc, layer, map);

		} catch (Exception e) {
			e.printStackTrace();
			Utils.die();
		}
	}

	private long loadNextLongBlock(ByteArrayInputStream stream, byte[] buffer) throws Exception {
		stream.read(buffer);
		return GameSaver.decode8(buffer);
	}

	private int loadNextBlock(ByteArrayInputStream stream, byte[] buffer) throws Exception {
		stream.read(buffer);
		return GameSaver.decode2(buffer);
	}
}
