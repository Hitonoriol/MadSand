package ru.bernarder.fallenrisefromdust;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map.Entry;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;

import ru.bernarder.fallenrisefromdust.enums.GameState;
import ru.bernarder.fallenrisefromdust.properties.ItemProp;

public class GameSaver {

	Thread saver = new Thread(new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {
			MadSand.print("World is saving... Don't quit the game!");
			GameSaver.saveWorld();
			GameSaver.this.saver.stop();
		}
	});

	public static void saveToExternal(String name, String text) {
		try {
			File file = new File(name);
			PrintWriter pw = new PrintWriter(file);
			pw.print(text);
			pw.close();
		} catch (Exception e) {

		}
	}

	static String SECTOR_DELIM = "!";

	static File getSectorFile(int wx, int wy) {
		return new File(
				MadSand.MAPDIR + MadSand.WORLDNAME + "/sector" + SECTOR_DELIM + wx + SECTOR_DELIM + wy + ".mws");
	}

	public static String getExternal(String name) {
		try {
			File file = new File(name);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line1 = br.readLine();
			br.close();
			return line1;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String getExternalNl(String name) {
		try {
			File file = new File(name);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String lk = "";
			String l;
			while ((l = br.readLine()) != null) {
				lk = lk + l;
			}
			br.close();
			return lk;
		} catch (Exception e) {
			return "";
		}
	}

	public static void saveWorld() {
		MadSand.createDirs();
		if (saveSector() && saveChar())
			MadSand.print("Game saved!");
		else
			MadSand.print("Couldn't save the game. Check logs.");
	}

	public static boolean loadWorld(String filename) {
		MadSand.WORLDNAME = filename;
		File f = new File(MadSand.MAPDIR + filename);
		Utils.out("Loading " + f.getAbsolutePath() + " Exists: " + f.exists() + " isDirectory: " + f.isDirectory());
		if (!f.exists()) {
			MadSand.print("Unable to load world");
			MadSand.state = GameState.NMENU;
			return false;
		}
		if (!f.isDirectory()) {
			MadSand.print("Unable to load world");
			MadSand.state = GameState.NMENU;
			return false;
		}
		MadSand.world.clearCurLoc();
		if (loadSector() && loadChar()) {
			MadSand.print("Loaded Game!");
			return true;
		} else
			return false;

	}

	public static boolean verifyNextSector(int x, int y) {
		File file = getSectorFile(x, y);
		if (file.exists()) {
			return true;
		}
		return false;
	}

	static boolean saveChar() {
		try {
			String fl = MadSand.MAPDIR + MadSand.WORLDNAME + MadSand.PLAYERFILE;
			Output output = new Output(new FileOutputStream(fl));
			MadSand.kryo.writeObject(output, MadSand.player.inventory.items);
			MadSand.kryo.writeObject(output, MadSand.player.stats);
			output.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	static boolean loadChar() {
		try {
			String fl = MadSand.MAPDIR + MadSand.WORLDNAME + MadSand.PLAYERFILE;
			Input input = new Input(new FileInputStream(fl));
			MadSand.player.inventory.items = MadSand.kryo.readObject(input, Vector.class);
			MadSand.player.stats = MadSand.kryo.readObject(input, Stats.class);
			MadSand.player.inventory.setMaxWeight(MadSand.player.stats.str * Stats.STR_WEIGHT_MULTIPLIER);
			MadSand.player.inventory.refreshWeight();
			input.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean saveSector(int wx, int wy, int layer) {
		try {
			String fl = getSectorFile(wx, wy).getAbsolutePath();
			MapID key = new MapID(new Pair(wx, wy), layer);
			Output output = new Output(new FileOutputStream(fl));
			HashMap<MapID, Map> map = MadSand.world._getLoc(wx, wy, layer);
			Location loc = new Location();
			loc.put(key, map.get(key));
			MadSand.mapSerializer.write(MadSand.kryo, output, loc);
			output.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	static boolean saveSector() {
		return saveSector(MadSand.world.curxwpos, MadSand.world.curywpos, 0);
	}

	public static boolean loadSector(int wx, int wy, int layer) {
		try {
			MapID key = new MapID(new Pair(wx, wy), layer);
			String fl = getSectorFile(wx, wy).getAbsolutePath();
			Utils.out("LoadSector: " + fl);
			Input input = new Input(new FileInputStream(fl));
			HashMap<MapID, Map> map = MadSand.mapSerializer.read(MadSand.kryo, input, Location.class);
			if (MadSand.world.locExists(key))
				MadSand.world.WorldLoc.remove(key);
			MadSand.world.WorldLoc.put(key, map.get(key));
			input.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	static boolean loadSector() {
		return loadSector(MadSand.world.curxwpos, MadSand.world.curywpos, 0);
	}

}
