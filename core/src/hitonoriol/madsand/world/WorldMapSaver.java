package hitonoriol.madsand.world;

import static hitonoriol.madsand.util.ByteUtils.decode2;
import static hitonoriol.madsand.util.ByteUtils.decode8;
import static hitonoriol.madsand.util.ByteUtils.encode2;
import static hitonoriol.madsand.util.ByteUtils.encode8;
import static hitonoriol.madsand.world.GameSaver.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.gamecontent.Objects;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapEntity;
import hitonoriol.madsand.util.Utils;

public class WorldMapSaver {
	private final static int BLOCK_SIZE = 2, LONG_BLOCK_SIZE = 8;
	private final byte[] block = new byte[BLOCK_SIZE];
	private final byte[] longBlock = new byte[LONG_BLOCK_SIZE];

	private GameSaver saver;
	private WorldMap worldMap;

	public WorldMapSaver(GameSaver saver) {
		this.saver = saver;
	}

	public byte[] locationToBytes(int wx, int wy) {
		var coords = new Pair();
		var stream = new ByteArrayOutputStream();
		try {
			var location = worldMap.getLocation(coords.set(wx, wy));
			// Header: format version, sector layer count
			stream.write(encode8(GameSaver.saveFormatVersion));
			stream.write(encode2(location.getLayerCount()));

			// Save all layers of sector
			for (int layerNum : location.getLayers().keySet()) {
				byte[] layer = sectorToBytes(wx, wy, layerNum);
				long size = layer.length;
				stream.write(encode2(layerNum));
				stream.write(encode8(size));
				stream.write(layer);
				System.gc();
			}
			System.gc();
			return stream.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			Utils.die();
			return null;
		}
	}

	public void saveLocationInfo(int wx, int wy) throws Exception {
		serializer().writeValue(saver.getLocationFile(wx, wy), worldMap.getLocation(new Pair(wx, wy)));
	}

	public void bytesToLocation(byte[] locationData, int wx, int wy) throws IOException {
		var stream = new ByteArrayInputStream(locationData);

		long saveVersion = nextLongBlock(stream, longBlock);
		if (saveVersion != GameSaver.saveFormatVersion)
			throw new RuntimeException("Incompatible save format version!");

		int layers = nextBlock(stream, block);
		for (int i = 0; i < layers; ++i) {
			int layer = nextBlock(stream, block);
			long layerLen = nextLongBlock(stream, longBlock);
			byte[] sectorContents = new byte[(int) layerLen];
			stream.read(sectorContents);
			bytesToSector(sectorContents, wx, wy, layer);
		}
	}

	public Location loadLocationInfo(int wx, int wy) throws IOException {
		var location = serializer().readValue(saver.getLocationFile(wx, wy), Location.class);
		worldMap.addLocation(new Pair(wx, wy), location);
		Utils.dbg("World map: {%X}, Location: {%X} @ (%d, %d)", worldMap.hashCode(), location.hashCode(), wx, wy);
		return location;
	}

	private byte[] sectorToBytes(int wx, int wy, int layer) {
		try {
			Utils.out("Saving sector (%d, %d): %d", wx, wy, layer);
			var stream = new ByteArrayOutputStream();
			var map = worldMap.getLocation(new Pair(wx, wy)).getLayer(layer);

			Utils.out("Saving NPCs and time-dependent entities...");
			serializer().saveMap(saver.getNpcFile(wx, wy, layer), map.getNpcs(), Pair.class, AbstractNpc.class);
			serializer().save(saver.getTimeDependentFile(wx, wy, layer), map.getTimeDependentMapEntities());

			int xsz = map.getWidth();
			int ysz = map.getHeight();
			// header: width, height
			stream.write(encode2(xsz));
			stream.write(encode2(ysz));

			Utils.out("Saving map (%dx%d) data...", xsz, ysz);
			writeStringBlock(stream, serializer().writeValueAsString(map));

			for (int y = 0; y < ysz; ++y) {
				for (int x = 0; x < xsz; ++x) {
					// Save tile
					var tile = map.getTile(x, y);
					stream.write(encode2(tile.id()));
					stream.write(encode2(tile.visited ? 1 : 0));

					// Save object
					var obj = map.getObject(x, y);
					stream.write(encode2(obj.id()));
					stream.write(encode2(obj.hp));
					stream.write(encode2(obj.maxHp));
				}
			}

			Utils.out("Saving loot...");
			writeStringBlock(stream, serializer().saveMap(map.getLoot(), Loot.class));

			Utils.out("Saving custom object names...");
			var modifiedObjNames = new HashMap<Pair, String>();
			map.getObjects().stream()
				.filter(obj -> !obj.getName().equals(Objects.all().getName(obj.id())))
				.forEach(obj -> modifiedObjNames.put(obj.getPosition(), obj.getName()));
			writeStringBlock(stream, serializer().saveMap(modifiedObjNames, String.class));
			return stream.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			Utils.die();
			return null;
		}
	}

	private void bytesToSector(byte[] sectorData, int wx, int wy, int layer) {
		try {
			Utils.out("Loading sector (%d, %d):%d...", wx, wy, layer);
			var stream = new ByteArrayInputStream(sectorData);
			// Read header
			int xsz = nextBlock(stream, block);
			int ysz = nextBlock(stream, block);

			var map = serializer().readValue(nextStringBlock(stream), Map.class);
			map.setSize(xsz, ysz);
			map.purge();
			worldMap.addMap(new Pair(wx, wy), layer, map);
			Utils.out("Loaded map (%dx%d) properties...", xsz, ysz);

			Utils.out("Restoring time dependent entities...");
			HashMap<Pair, MapEntity> timeDepMap = serializer().readMap(
				saver.getTimeDependentFile(wx, wy, layer),
				MapEntity.class
			);
			map.setTimeDependentMapEntities(timeDepMap);

			Utils.out("Loading NPCs...");
			HashMap<Pair, AbstractNpc> npcs = serializer().readMap(saver.getNpcFile(wx, wy, layer), AbstractNpc.class);
			map.setNpcs(npcs).forEach((coords, npc) -> {
				npc.postLoadInit(map);
				Utils.out("(%s): [%s]", coords, npc);
			});

			Utils.out("Loading tiles & objects...");
			for (int y = 0; y < ysz; ++y) {
				for (int x = 0; x < xsz; ++x) {
					map.addTile(x, y, nextBlock(stream, block), true);
					map.getTile(x, y).visited = nextBlock(stream, block) != 0;

					map.addObject(x, y, nextBlock(stream, block), false);
					map.getObject(x, y).hp = nextBlock(stream, block);
					map.getObject(x, y).maxHp = nextBlock(stream, block);
				}
			}

			Utils.out("Loading loot...");
			HashMap<Pair, Loot> mapLoot = serializer().getMapReader(Loot.class).readValue(nextStringBlock(stream));
			map.setLoot(mapLoot);

			Utils.out("Loading custom object names...");
			HashMap<Pair, String> modifiedObjNames = serializer().getMapReader(String.class)
				.readValue(nextStringBlock(stream));
			modifiedObjNames.forEach((position, name) -> map.getObject(position).name = name);

			Utils.out("Finishing up...");
			map.postLoadInit();
		} catch (Exception e) {
			e.printStackTrace();
			Utils.die();
		}
	}

	/* Writes String as bytes to stream as a sequence: [(long) string length][string
	 * bytes] */
	private void writeStringBlock(ByteArrayOutputStream stream, String str) throws Exception {
		byte bytes[] = str.getBytes();
		stream.write(encode8(bytes.length));
		stream.write(bytes);
	}

	/* Reads byte sequence: [(long) length of contents][contents] and returns
	 * contents as String */
	private String nextStringBlock(ByteArrayInputStream stream) throws Exception {
		byte bytes[] = new byte[(int) nextLongBlock(stream, longBlock)];
		stream.read(bytes);
		return new String(bytes);
	}

	private long nextLongBlock(ByteArrayInputStream stream, byte[] buffer) throws IOException {
		stream.read(buffer);
		return decode8(buffer);
	}

	private int nextBlock(ByteArrayInputStream stream, byte[] buffer) throws IOException {
		stream.read(buffer);
		return decode2(buffer);
	}

	public void setWorldMap(WorldMap worldMap) {
		this.worldMap = worldMap;
	}

	public WorldMap getWorldMap() {
		return worldMap;
	}
}
