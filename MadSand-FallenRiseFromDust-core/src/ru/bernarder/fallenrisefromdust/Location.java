package ru.bernarder.fallenrisefromdust;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;

public class Location extends HashMap<MapID, Map> {
	private static final long serialVersionUID = -4489829388439109446L;

	final String LOOT_DELIM = "|";

	byte[] sectorToBytes(int wx, int wy, int layer) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			MapID loc = new MapID(new Pair(wx, wy), layer);
			Map map = this.get(loc);
			int xsz = map.getWidth();
			int ysz = map.getHeight();
			stream.write(GameSaver.encode2(xsz));
			stream.write(GameSaver.encode2(ysz));
			MapObject obj = new MapObject();
			for (int y = 0; y < ysz; ++y) {
				for (int x = 0; x < xsz; ++x) {
					stream.write(GameSaver.encode2(map.getTile(x, y).id));
					obj = map.getObject(x, y);
					stream.write(GameSaver.encode2(obj.id));
					stream.write(GameSaver.encode2(obj.hp));
				}
			}
			ByteArrayOutputStream lootStream = new ByteArrayOutputStream();
			String loot;
			for (int y = 0; y < ysz; ++y) {
				for (int x = 0; x < xsz; ++x) {
					loot = map.getLoot(x, y).getContents();
					lootStream.write(GameSaver.encode2(loot.length()));
					lootStream.write(loot.getBytes());
				}
			}
			byte[] _loot = lootStream.toByteArray();
			lootStream.close();
			byte ret[] = stream.toByteArray();
			stream.close();
			ret = GameSaver.concat(ret, _loot);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	void bytesToSector(byte[] sector, int wx, int wy, int layer) {
		try {
			int xsz = GameSaver.decode2(Arrays.copyOfRange(sector, 0, 2));
			int ysz = GameSaver.decode2(Arrays.copyOfRange(sector, 2, 4));
			MapID loc = new MapID(new Pair(wx, wy), layer);
			Map map = new Map(xsz, ysz);
			map.purge();
			ByteArrayInputStream stream = new ByteArrayInputStream(sector);
			for (int i = 0; i < 4; ++i)
				stream.read();
			byte[] block = new byte[2];
			for (int y = 0; y < ysz; ++y) {
				for (int x = 0; x < xsz; ++x) {
					stream.read(block);
					map.addTile(x, y, GameSaver.decode2(block));
					stream.read(block);
					map.addObject(x, y, GameSaver.decode2(block));
					stream.read(block);
					map.getObject(x, y).hp = GameSaver.decode2(block);
				}
			}
			String loot;
			byte[] _len = new byte[2];
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

			stream.close();
			if (this.containsKey(loc))
				this.remove(loc);
			this.put(loc, map);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
