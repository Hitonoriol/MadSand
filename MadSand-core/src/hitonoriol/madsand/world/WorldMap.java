package hitonoriol.madsand.world;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.core.type.TypeReference;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.map.Crop;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapObject;

public class WorldMap extends HashMap<MapID, Map> {
	private static final long serialVersionUID = -4489829388439109446L;
	private int DEFAULT_LAYERS = 2;

	private Pair coords = new Pair();
	private MapID mapID = new MapID();
	public HashMap<MapID, Integer> layers = new HashMap<>(); // Default number of layers

	final String LOOT_DELIM = "|";
	final int CROP_BLOCK_LEN = 16;
	final int BLOCK_SIZE = 2;
	final int LONG_BLOCK_SIZE = 8;

	byte[] sectorToBytes(int wx, int wy, int layer) {
		try {
			Utils.out("Saving sector " + wx + ", " + wy + " : " + layer);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			MapID loc = new MapID(new Pair(wx, wy), layer);
			Map map = this.get(loc);
			ArrayList<Npc> npcs = new ArrayList<Npc>();
			HashMap<Pair, Npc> mapNpcs = map.getNpcs();
			if (!mapNpcs.isEmpty())
				for (Entry<Pair, Npc> entry : mapNpcs.entrySet()) {
					npcs.add(entry.getValue());
				}

			String npf = GameSaver.getNpcFile(wx, wy, layer);
			MadSand.mapper.writeValue(new File(npf), npcs);

			int xsz = map.getWidth();
			int ysz = map.getHeight();
			// header: isEditable, width, height,spawnpoint(x, y) - for dungeon levels,
			// default tile, default object, biome
			stream.write(GameSaver.encode2(Utils.val(map.editable)));
			stream.write(GameSaver.encode2(xsz));
			stream.write(GameSaver.encode2(ysz));
			stream.write(GameSaver.encode2(map.spawnPoint.x));
			stream.write(GameSaver.encode2(map.spawnPoint.y));
			stream.write(GameSaver.encode2(map.defTile));
			stream.write(GameSaver.encode2(map.defObject));
			stream.write(GameSaver.encode2(map.getBiome()));
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

	public byte[] locationToBytes(int wx, int wy) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		byte[] layer;
		long size;
		try {
			int layers = getLayerCount(wx, wy);
			//Header: format version, sector layer count
			stream.write(GameSaver.encode8(GameSaver.saveFormatVersion));
			stream.write(GameSaver.encode2(layers));

			//Save all layers of sector
			for (int i = 0; i < layers; ++i) {
				layer = sectorToBytes(wx, wy, i);
				size = layer.length;
				stream.write(GameSaver.encode8(size));
				stream.write(layer);
			}

			return stream.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void bytesToLocation(byte[] location, int wx, int wy) throws Exception {
		ByteArrayInputStream stream = new ByteArrayInputStream(location);
		long layersz;
		long saveVersion;

		saveVersion = GameSaver.decode8(stream.readNBytes(LONG_BLOCK_SIZE));
		if (saveVersion != GameSaver.saveFormatVersion)
			throw (new Exception("Save version is too old / new!"));

		int layers = GameSaver.decode2(stream.readNBytes(BLOCK_SIZE));
		setLayerCount(wx, wy, layers);
		for (int i = 0; i < layers; ++i) {
			layersz = GameSaver.decode8(stream.readNBytes(LONG_BLOCK_SIZE));
			bytesToSector(stream.readNBytes((int) layersz), wx, wy, i);
		}

	}

	void bytesToSector(byte[] sector, int wx, int wy, int layer) {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(sector);
			// Read header
			boolean editable = Utils.bool(loadNextBlock(stream));
			int xsz = loadNextBlock(stream);
			int ysz = loadNextBlock(stream);
			int spawnX = loadNextBlock(stream);
			int spawnY = loadNextBlock(stream);
			int defTile = loadNextBlock(stream);
			int defObject = loadNextBlock(stream);
			int biome = loadNextBlock(stream);

			MapID loc = new MapID(new Pair(wx, wy), layer);
			Map map = new Map(xsz, ysz);
			map.purge();
			map.editable = editable;
			map.spawnPoint = new Pair(spawnX, spawnY);
			Utils.out("Dungeon spawnpoint: " + map.spawnPoint);

			// Load NPCs
			String npf = GameSaver.getNpcFile(wx, wy, layer);
			ArrayList<Npc> npcs = new ArrayList<Npc>();
			npcs = MadSand.mapper.readValue(GameSaver.getExternal(npf), new TypeReference<ArrayList<Npc>>() {
			});

			for (Npc npc : npcs) {
				npc.loadSprite();
				npc.initStatActions();
				map.putNpc(npc);
			}

			// Load tiles & objects
			map.setBiome(biome);
			map.defTile = defTile;
			map.defObject = defObject;
			byte[] block = new byte[BLOCK_SIZE];
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
			int cropsCount = GameSaver.decode2(stream.readNBytes(BLOCK_SIZE));
			int x, y, id, stage;
			long ptime;
			Crop crop;
			for (int i = 0; i < cropsCount; ++i) {
				x = GameSaver.decode2(stream.readNBytes(BLOCK_SIZE));
				y = GameSaver.decode2(stream.readNBytes(BLOCK_SIZE));
				id = GameSaver.decode2(stream.readNBytes(BLOCK_SIZE));
				ptime = GameSaver.decode8(stream.readNBytes(LONG_BLOCK_SIZE));
				stage = GameSaver.decode2(stream.readNBytes(BLOCK_SIZE));
				crop = new Crop(id, ptime, stage);
				map.putCrop(x, y, crop);
			}

			stream.close();

			// Add self to Location list
			if (this.containsKey(loc))
				this.remove(loc);
			this.put(loc, map);
		} catch (Exception e) {
			e.printStackTrace();
			Utils.die();
		}
	}

	private int loadNextBlock(ByteArrayInputStream stream) throws Exception {
		return GameSaver.decode2(stream.readNBytes(BLOCK_SIZE));
	}

	public void setLayerCount(int wx, int wy, int layers) {
		this.layers.put(new MapID(new Pair(wx, wy), World.LAYER_OVERWORLD), layers);
	}

	public int getLayerCount(int wx, int wy) {
		return layers.getOrDefault(mapID.set(coords.set(wx, wy), World.LAYER_OVERWORLD), DEFAULT_LAYERS);
	}

	public void increaseLayerCount(int wx, int wy) {
		setLayerCount(wx, wy, getLayerCount(wx, wy) + 1);
	}

	public Map remove(Object key) {
		Map removed = super.remove(key);
		MapID id = (MapID) key;
		if (id.layer == World.LAYER_OVERWORLD)
			deleteLayerCountEntry(id.worldxy.x, id.worldxy.y);
		return removed;
	}

	private void deleteLayerCountEntry(int wx, int wy) {
		layers.remove(mapID.set(coords.set(wx, wy), World.LAYER_OVERWORLD));
		Utils.out("Sector (" + wx + ", " + wy + ") has been removed from worldMap!");
	}
}
