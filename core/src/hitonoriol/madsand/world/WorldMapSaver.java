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
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.map.Crop;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.ItemProducer;
import hitonoriol.madsand.map.object.MapObject;

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
			// Header: format version, sector layer count
			stream.write(GameSaver.encode8(GameSaver.saveFormatVersion));
			stream.write(GameSaver.encode2(location.getLayerCount()));

			// Save all layers of sector
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

	public Location loadLocationInfo(int wx, int wy) throws Exception {
		Location location = Resources.mapper.readValue(GameSaver.getLocationFile(wx, wy), Location.class);
		worldMap.locations.put(new Pair(wx, wy), location);
		return location;
	}

	byte[] sectorToBytes(int wx, int wy, int layer) {
		try {
			Utils.out("Saving sector " + wx + ", " + wy + " Layer: " + layer);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Pair loc = new Pair(wx, wy);
			Map map = worldMap.locations.get(loc).getLayer(layer);
			byte mapProperties[] = Resources.mapper.writeValueAsString(map).getBytes();

			Resources.mapper.writerFor(Resources.getMapType(Pair.class, AbstractNpc.class))
					.writeValue(new File(GameSaver.getNpcFile(wx, wy, layer)), map.getNpcs());

			Resources.mapper.writeValue(new File(GameSaver.getProdStationFile(wx, wy, layer)),
					map.getMapProductionStations());

			int xsz = map.getWidth();
			int ysz = map.getHeight();
			// header: width, height
			stream.write(GameSaver.encode2(xsz));
			stream.write(GameSaver.encode2(ysz));

			// Misc map properties as a json string
			stream.write(GameSaver.encode8(mapProperties.length));
			stream.write(mapProperties);

			MapObject obj = new MapObject();
			ByteArrayOutputStream lootStream = new ByteArrayOutputStream();
			ByteArrayOutputStream cropStream = new ByteArrayOutputStream();
			byte loot[];
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
					stream.write(GameSaver.encode2(obj.maxHp));

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

			// Save loot
			loot = Resources.mapper.writeValueAsString(map.getLoot()).getBytes();
			lootStream.write(GameSaver.encode8(loot.length));
			lootStream.write(loot);

			// Get all bytes from streams & concat them into one array
			byte[] cropCount = GameSaver.encode8(cropBlocks);
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
			int xsz = loadNextBlock(stream, block);
			int ysz = loadNextBlock(stream, block);
			byte mapProperties[] = new byte[(int) loadNextLongBlock(stream, longBlock)];
			stream.read(mapProperties);

			Pair loc = new Pair(wx, wy);
			Map map = Resources.mapper.readValue(new String(mapProperties), Map.class);
			map.setSize(xsz, ysz);
			map.purge();

			// Load NPCs
			HashMap<Pair, AbstractNpc> npcs = Resources.mapper.readValue(
					new File(GameSaver.getNpcFile(wx, wy, layer)),
					Resources.getMapType(Pair.class, AbstractNpc.class));

			for (Entry<Pair, AbstractNpc> npc : npcs.entrySet()) {
				npc.getValue().loadSprite();
				npc.getValue().initStatActions();
			}

			map.setNpcs(npcs);

			// Load tiles & objects
			for (int y = 0; y < ysz; ++y) {
				for (int x = 0; x < xsz; ++x) {
					map.addTile(x, y, loadNextBlock(stream, block), true);
					map.getTile(x, y).visited = loadNextBlock(stream, block) != 0;
					map.addObject(x, y, loadNextBlock(stream, block));
					map.getObject(x, y).hp = loadNextBlock(stream, block);
					map.getObject(x, y).maxHp = loadNextBlock(stream, block);
				}
			}

			// Load production stations
			HashMap<Pair, ItemProducer> prodStations = Resources.mapper.readValue(
					new File(GameSaver.getProdStationFile(wx, wy, layer)),
					Resources.getMapType(Pair.class, ItemProducer.class));
			map.setMapProductionStations(prodStations);

			// Load loot
			byte[] lootNode = new byte[(int) loadNextLongBlock(stream, longBlock)];
			stream.read(lootNode);
			HashMap<Pair, Loot> mapLoot = Resources.mapper.readValue(new String(lootNode),
					Resources.getMapType(Pair.class, Loot.class));
			map.setLoot(mapLoot);

			// Load crops
			int cropsCount = (int) loadNextLongBlock(stream, longBlock);
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
