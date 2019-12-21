package ru.bernarder.fallenrisefromdust;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class Location extends HashMap<MapID, Map> {
	private static final long serialVersionUID = -4489829388439109446L;

	public static int layers = 2; // Default number of layers
	public static int maxLayers = 40; // Maximum possible underground layer - 1; TODO implement dynamic dungeon
										// generation when player descends further underground

	final String LOOT_DELIM = "|";
	final int CROP_BLOCK_LEN = 16;
	final int BLOCK_SIZE = 2;
	final int LONG_BLOCK_SIZE = 8;

	byte[] sectorToBytes(int wx, int wy, int layer) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			MapID loc = new MapID(new Pair(wx, wy), layer);
			Map map = this.get(loc);
			int xsz = map.getWidth();
			int ysz = map.getHeight();
			// header: width, height, default tile, biome
			stream.write(GameSaver.encode2(xsz));
			stream.write(GameSaver.encode2(ysz));
			stream.write(GameSaver.encode2(map.getDefTile()));
			stream.write(GameSaver.encode2(map.getBiome()));
			MapObject obj = new MapObject();

			ByteArrayOutputStream lootStream = new ByteArrayOutputStream();

			ByteArrayOutputStream cropStream = new ByteArrayOutputStream();
			String loot;
			Crop crop;
			int cropBlocks = 0;
			for (int y = 0; y < ysz; ++y) {
				for (int x = 0; x < xsz; ++x) {
					stream.write(GameSaver.encode2(map.getTile(x, y).id));
					obj = map.getObject(x, y);
					stream.write(GameSaver.encode2(obj.id));
					stream.write(GameSaver.encode2(obj.hp));

					loot = map.getLoot(x, y).getContents();
					lootStream.write(GameSaver.encode2(loot.length()));
					lootStream.write(loot.getBytes());

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
			return null;
		}
	}

	byte[] locationToBytes(int wx, int wy) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		byte[] layer;
		long size;
		try {
			stream.write(GameSaver.encode2(layers));
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

	void bytesToLocation(byte[] location, int wx, int wy) {
		ByteArrayInputStream stream = new ByteArrayInputStream(location);
		long layersz;
		try {
			layers = GameSaver.decode2(stream.readNBytes(BLOCK_SIZE));
			for (int i = 0; i < layers; ++i) {
				layersz = GameSaver.decode8(stream.readNBytes(LONG_BLOCK_SIZE));
				bytesToSector(stream.readNBytes((int) layersz), wx, wy, i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void bytesToSector(byte[] sector, int wx, int wy, int layer) {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(sector);
			int xsz = GameSaver.decode2(stream.readNBytes(BLOCK_SIZE));
			int ysz = GameSaver.decode2(stream.readNBytes(BLOCK_SIZE));
			int defTile = GameSaver.decode2(stream.readNBytes(BLOCK_SIZE));
			int biome = GameSaver.decode2(stream.readNBytes(BLOCK_SIZE));
			MapID loc = new MapID(new Pair(wx, wy), layer);
			Map map = new Map(xsz, ysz);
			map.purge();
			map.setBiome(biome);
			map.setDefTile(defTile);
			byte[] block = new byte[BLOCK_SIZE];
			for (int y = 0; y < ysz; ++y) {
				for (int x = 0; x < xsz; ++x) {
					stream.read(block);
					map.addTile(x, y, GameSaver.decode2(block), true);
					stream.read(block);
					map.addObject(x, y, GameSaver.decode2(block));
					stream.read(block);
					map.getObject(x, y).hp = GameSaver.decode2(block);
				}
			}
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
			if (this.containsKey(loc))
				this.remove(loc);
			this.put(loc, map);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
