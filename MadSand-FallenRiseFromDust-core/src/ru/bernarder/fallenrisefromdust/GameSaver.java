package ru.bernarder.fallenrisefromdust;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ru.bernarder.fallenrisefromdust.enums.GameState;

public class GameSaver {

	Thread saver = new Thread(new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {
			MadSand.print("World is saving... Don't quit the game!");
			GameSaver.saveWorld();
			GameSaver.this.saver.stop();
		}
	});

	static byte[] concat(byte[]... arrays) {
		int totalLength = 0;
		for (int i = 0; i < arrays.length; i++) {
			totalLength += arrays[i].length;
		}

		byte[] result = new byte[totalLength];

		int currentIndex = 0;
		for (int i = 0; i < arrays.length; i++) {
			System.arraycopy(arrays[i], 0, result, currentIndex, arrays[i].length);
			currentIndex += arrays[i].length;
		}

		return result;
	}

	public static byte[] encode8(long l) {
		byte[] result = new byte[8];
		for (int i = 7; i >= 0; i--) {
			result[i] = (byte) (l & 0xFF);
			l >>= 8;
		}
		return result;
	}

	public static long decode8(byte[] b) {
		long result = 0;
		for (int i = 0; i < 8; i++) {
			result <<= 8;
			result |= (b[i] & 0xFF);
		}
		return result;
	}

	static byte[] encode2(int val) {
		byte data[] = new byte[2];
		data[1] = (byte) (val & 0xFF);
		data[0] = (byte) ((val >> 8) & 0xFF);
		return data;
	}

	static int decode2(byte[] bytes) {
		return (bytes[0] << 8) | (bytes[1] & 0xFF);
	}

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
			e.printStackTrace();
			return "";
		}
	}

	public static void saveWorld() {
		GameSaver.createDirs();
		if (saveLocation() && saveChar())
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
		MadSand.world = new World();
		if (!loadChar())
			return false;
		if (loadLocation()) {
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
			String wfl = MadSand.MAPDIR + MadSand.WORLDNAME + MadSand.WORLDFILE;
			String ifl = MadSand.MAPDIR + MadSand.WORLDNAME + MadSand.INVFILE;
			MadSand.mapper.writeValue(new File(fl), World.player);
			MadSand.mapper.writeValue(new File(ifl), World.player.inventory);
			MadSand.mapper.writeValue(new File(wfl), MadSand.world);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	static boolean loadChar() {
		try {
			String fl = MadSand.MAPDIR + MadSand.WORLDNAME + MadSand.PLAYERFILE;
			String wfl = MadSand.MAPDIR + MadSand.WORLDNAME + MadSand.WORLDFILE;
			String ifl = MadSand.MAPDIR + MadSand.WORLDNAME + MadSand.INVFILE;

			World.player.inventory = new Inventory();

			World.player = MadSand.mapper.readValue(getExternal(fl), Player.class);

			World.player.reinit();
			World.player.inventory = MadSand.mapper.readValue(getExternal(ifl), Inventory.class);
			World.player.inventory.refreshContents();
			World.player.initStatActions();

			World w;
			w = MadSand.mapper.readValue(getExternal(wfl), World.class);
			MadSand.world.curxwpos = w.curxwpos;
			MadSand.world.curywpos = w.curywpos;
			MadSand.world.curlayer = w.curlayer;
			MadSand.world.worldtime = w.worldtime;
			MadSand.world.tick = w.tick;

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean saveLocation(int wx, int wy) {
		try {
			OutputStream os = new FileOutputStream(getSectorFile(wx, wy));
			os.write(MadSand.world.WorldLoc.locationToBytes(wx, wy));
			os.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean saveLocation() {
		return saveLocation(MadSand.world.curxwpos, MadSand.world.curywpos);
	}

	public static boolean loadLocation(int wx, int wy) {
		try {
			Path fileLocation = Paths.get(getSectorFile(wx, wy).toURI());
			byte[] data = Files.readAllBytes(fileLocation);
			MadSand.world.WorldLoc.bytesToLocation(data, wx, wy);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean loadLocation() {
		return loadLocation(MadSand.world.curxwpos, MadSand.world.curywpos);
	}

	public static void createDirs() {
		File saveloc = new File(MadSand.SAVEDIR);
		File maploc = new File(MadSand.MAPDIR);
		File curworld = new File(MadSand.MAPDIR + MadSand.WORLDNAME);

		if (!saveloc.exists()) {
			saveloc.mkdirs();
		}
		if (!maploc.exists()) {
			maploc.mkdirs();
		}
		if (!curworld.exists())
			curworld.mkdirs();
	}

}
