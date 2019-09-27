package ru.bernarder.fallenrisefromdust;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

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
			MadSand.kryo.writeObject(output, MadSand.player.x);
			MadSand.kryo.writeObject(output, MadSand.player.y);
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
			MadSand.player.inventory = new Inventory();
			MadSand.player.inventory.items = MadSand.kryo.readObject(input, Vector.class);
			MadSand.player.stats = MadSand.kryo.readObject(input, Stats.class);
			MadSand.player.inventory.setMaxWeight(MadSand.player.stats.str * Stats.STR_WEIGHT_MULTIPLIER);
			MadSand.player.inventory.refreshWeight();
			MadSand.player.x = MadSand.kryo.readObject(input, Integer.class);
			MadSand.player.y = MadSand.kryo.readObject(input, Integer.class);
			MadSand.player.updCoords();
			input.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean saveSector(int wx, int wy, int layer) {
		try {
			OutputStream os = new FileOutputStream(getSectorFile(wx, wy));
			os.write(MadSand.world.WorldLoc.sectorToBytes(wx, wy, layer));
			os.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	static boolean saveSector() {
		return saveSector(MadSand.world.curxwpos, MadSand.world.curywpos, MadSand.world.curlayer);
	}

	public static boolean loadSector(int wx, int wy, int layer) {
		try {
			Path fileLocation = Paths.get(getSectorFile(wx, wy).toURI());
			byte[] data = Files.readAllBytes(fileLocation);
			MadSand.world.WorldLoc.bytesToSector(data, wx, wy, layer);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	static boolean loadSector() {
		return loadSector(MadSand.world.curxwpos, MadSand.world.curywpos, MadSand.world.curlayer);
	}

}
