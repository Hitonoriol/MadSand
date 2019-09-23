package ru.bernarder.fallenrisefromdust;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Vector;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import ru.bernarder.fallenrisefromdust.enums.GameState;

public class GameSaver {

	Thread saver = new Thread(new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {
			MadSand.print("World is saving... Don't quit the game!");
			GameSaver.saveWorld(MadSand.WORLDNAME);
			MadSand.print("World saved!");
			GameSaver.this.saver.stop();
		}
	});

	public void init() {
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

	public static void saveWorld(String filename) {
		MadSand.createDirs();
		String curf = MadSand.MAPDIR + filename + "/" + "sector-" + MadSand.world.curxwpos + "-"
				+ MadSand.world.curywpos + ".mws";
		// saveMap();
		saveChar();
	}

	public static boolean loadWorld(String filename) {
		File f = new File(MadSand.MAPDIR + filename);
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

		File file = new File(MadSand.MAPDIR + filename + "/" + "sector-" + MadSand.world.curxwpos + "-"
				+ MadSand.world.curywpos + ".mws");
		if (file.exists()) {
			MadSand.world.clearCurLoc();
			loadMap(MadSand.MAPDIR + filename + "/" + "sector-" + MadSand.world.curxwpos + "-" + MadSand.world.curywpos
					+ ".mws");
			loadChar();
			MadSand.print("Loaded Game!");
			return true;
		} else
			return false;

	}

	public static boolean verifyNextSector(int x, int y) {
		File file = new File(MadSand.MAPDIR + MadSand.WORLDNAME + "/" + "sector-" + x + "-" + y + ".mws");
		if (file.exists()) {
			return true;
		}
		return false;
	}

	static void saveChar() {
		try {
			String fl = MadSand.MAPDIR + MadSand.WORLDNAME + "/" + MadSand.name + ".mc";
			Output output = new Output(new FileOutputStream(fl));
			MadSand.kryo.writeObject(output, MadSand.player.inventory.items);
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	static void loadChar() {
		try {
			String fl = MadSand.MAPDIR + MadSand.WORLDNAME + "/" + MadSand.name + ".mc";
			Kryo kryo = new Kryo();
			Input input = new Input(new FileInputStream(fl));
			Vector<Item> items = kryo.readObject(input, Vector.class);
			MadSand.player.inventory.items = items;
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveMap(Map map) {
		// TODO
	}

	public static void loadMap(String filename) {
		// TODO
	}

}
