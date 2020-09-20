package hitonoriol.madsand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.enums.GameState;
import hitonoriol.madsand.world.World;

public class GameSaver {
	static String SECTOR_DELIM = "!";
	public final static long saveFormatVersion = 4;

	public static byte[] concat(byte[]... arrays) {
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

	public static byte[] encode2(int val) {
		byte data[] = new byte[2];
		data[1] = (byte) (val & 0xFF);
		data[0] = (byte) ((val >> 8) & 0xFF);
		return data;
	}

	public static int decode2(byte[] bytes) {
		return (bytes[0] << 8) | (bytes[1] & 0xFF);
	}

	public static void saveToExternal(String name, String text) {
		try {
			File file = new File(name);
			PrintWriter pw = new PrintWriter(file);
			pw.print(text);
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getExternal(String name, boolean withNewline) {
		try {
			File file = new File(name);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String lk = "";
			String l;
			while ((l = br.readLine()) != null) {
				lk += l;
				if (withNewline)
					lk += System.lineSeparator();
			}
			br.close();
			return lk.trim();
		} catch (Exception e) {
			return "";
		}
	}

	public static String getExternal(String name) {
		return getExternal(name, false);
	}

	static File getSectorFile(int wx, int wy) {
		return new File(MadSand.MAPDIR + MadSand.WORLDNAME + "/sector" + SECTOR_DELIM + wx + SECTOR_DELIM + wy
				+ MadSand.SAVE_EXT);
	}

	public static String getNpcFile(int wx, int wy, int layer) {
		return MadSand.MAPDIR + MadSand.WORLDNAME + "/" + MadSand.NPCSFILE + wx + SECTOR_DELIM + wy + SECTOR_DELIM
				+ layer + SECTOR_DELIM + MadSand.SAVE_EXT;
	}

	public static void saveWorld() {
		GameSaver.createDirs();
		saveLog();
		if (saveLocation() && saveChar())
			MadSand.print("Game saved!");
		else
			MadSand.print("Couldn't save the game. Check logs.");
	}

	public static boolean loadWorld(String filename) {
		MadSand.WORLDNAME = filename;
		File f = new File(MadSand.MAPDIR + filename);

		if (!f.exists() || !f.isDirectory()) {
			MadSand.switchStage(GameState.NMENU, Gui.mainMenu);
			Gui.drawOkDialog("Couldn't load this world", Gui.mainMenu);
			return false;
		}

		MadSand.world = new World();

		if (!loadChar()) {
			loadErrMsg();
			return false;
		}

		if (loadLocation()) {
			MadSand.world.updateLight();
			loadLog();
			MadSand.print("Loaded Game!");
			return true;
		} else {
			loadErrMsg();
			return false;
		}

	}

	public static void loadErrMsg() {
		MadSand.switchStage(GameState.NMENU, Gui.mainMenu);
		Gui.drawOkDialog(
				"Couldn't to load this world. \n"
						+ "Maybe it was saved in older/newer version of the game or some files are corrupted.\n"
						+ "Check " + MadSand.ERRFILE + " for details.",
				Gui.mainMenu);
		// MadSand.justStarted = false;
	}

	public static boolean verifyNextSector(int x, int y) {
		File sectorFile = getSectorFile(x, y);
		return sectorFile.exists();
	}

	static boolean saveChar() {
		try {
			String fl = MadSand.MAPDIR + MadSand.WORLDNAME + MadSand.PLAYERFILE;
			String wfl = MadSand.MAPDIR + MadSand.WORLDNAME + MadSand.WORLDFILE;
			MadSand.mapper.writeValue(new File(fl), World.player);
			MadSand.mapper.writeValue(new File(wfl), MadSand.world);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	static boolean loadChar() {
		try {
			Utils.out("Loading character...");
			String fl = MadSand.MAPDIR + MadSand.WORLDNAME + MadSand.PLAYERFILE;
			String wfl = MadSand.MAPDIR + MadSand.WORLDNAME + MadSand.WORLDFILE;

			World.player = MadSand.mapper.readValue(getExternal(fl), Player.class);

			Player player = World.player;

			player.inventory.initUI();
			player.inventory.refreshContents();
			player.initStatActions();
			player.quests.setPlayer(player);
			player.turn(player.stats.look);
			player.stats.equipment.refreshUI();

			World w;
			w = MadSand.mapper.readValue(getExternal(wfl), World.class);
			MadSand.world.curxwpos = w.curxwpos;
			MadSand.world.curywpos = w.curywpos;
			MadSand.world.curlayer = w.curlayer;
			MadSand.world.worldtime = w.worldtime;
			MadSand.world.tick = w.tick;

			Utils.out("Done loading character.");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean saveLocation(int wx, int wy) {
		try {
			OutputStream os = new FileOutputStream(getSectorFile(wx, wy));
			os.write(MadSand.world.worldMap.locationToBytes(wx, wy));
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
			MadSand.world.worldMap.bytesToLocation(data, wx, wy);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean loadLocation() {
		return loadLocation(MadSand.world.curxwpos, MadSand.world.curywpos);
	}

	private static boolean saveLog() {
		try {
			FileWriter fw = new FileWriter(MadSand.MAPDIR + MadSand.WORLDNAME + MadSand.LOGFILE);

			for (Label logLabel : Gui.overlay.getLogLabels())
				fw.write(logLabel.getText().toString() + Resources.LINEBREAK);

			fw.close();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static boolean loadLog() {
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(MadSand.MAPDIR + MadSand.WORLDNAME + MadSand.LOGFILE));
			String line;
			int i = 0;
			Label[] labels = Gui.overlay.getLogLabels();

			while ((line = br.readLine()) != null)
				labels[i++].setText(line);

			br.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

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
