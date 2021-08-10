package hitonoriol.madsand;

import static hitonoriol.madsand.resources.Resources.*;
import static hitonoriol.madsand.resources.Resources.ERR_FILE;
import static hitonoriol.madsand.resources.Resources.LINEBREAK;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.World;
import hitonoriol.madsand.world.WorldMapSaver;

public class GameSaver {
	public static String SECTOR_DELIM = "!";
	public final static long saveFormatVersion = 9;

	public static void save() {
		World world = MadSand.world();
		if (world.inEncounter()) {
			Gui.drawOkDialog("You can't save during an encounter!");
			return;
		}
		GameSaver.createDirs();
		world.logout();
		saveLog();
		if (saveLocation() && saveWorld())
			MadSand.print("Game saved!");
		else
			MadSand.print("Couldn't save the game. Check logs.");
	}

	public static boolean load(String filename) {
		Utils.dbg("Loading world [%s]...", filename);
		Utils.printMemoryInfo();
		MadSand.WORLDNAME = filename;
		File f = new File(MadSand.MAPDIR + filename);

		if (!f.exists() || !f.isDirectory()) {
			MadSand.switchScreen(MadSand.mainMenu);
			Gui.drawOkDialog("Couldn't load this world");
			return false;
		}

		Gui.overlay.getGameLog().clear();
		MadSand.world().close();
		createDirs();

		if (!loadWorld()) {
			loadErrMsg();
			return false;
		}

		if (loadLocation()) {
			Lua.init();
			MadSand.world().updateLight();
			loadLog();
			MadSand.print("Loaded Game!");
			Utils.dbg("Loaded [%s] successfully!", filename);
			System.gc();
			Utils.printMemoryInfo();
			return true;
		} else {
			loadErrMsg();
			return false;
		}
	}

	public static boolean saveLocation(int wx, int wy) {
		WorldMapSaver saver = MadSand.world().getMapSaver();
		try {
			OutputStream os = new FileOutputStream(getSectorFile(wx, wy));
			os.write(saver.locationToBytes(wx, wy));
			os.close();
			saver.saveLocationInfo(wx, wy);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean saveLocation() {
		return saveLocation(MadSand.world().wx(), MadSand.world().wy());
	}

	public static boolean loadLocation(int wx, int wy) {
		WorldMapSaver saver = MadSand.world().getMapSaver();

		try {
			byte[] data = Files.readAllBytes(Paths.get(getSectorFile(wx, wy).toURI()));
			Utils.out("Loading location [%d, %d]", wx, wy);

			saver.loadLocationInfo(wx, wy);
			saver.bytesToLocation(data, wx, wy);
			MadSand.world().setWorldMap(saver.getWorldMap());
			System.gc();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean loadLocation() {
		World world = MadSand.world();
		return loadLocation(world.wx(), world.wy());
	}

	private static boolean saveWorld() {
		try {
			File worldFile = new File(getCurSaveDir() + MadSand.WORLDFILE);
			Player player = MadSand.player();

			player.stats.equipment.setStatBonus(false);
			/*Resources.mapper.writeValue(new File(fl), player);*/
			getMapper().writeValue(worldFile, MadSand.world());
			player.stats.equipment.setStatBonus(true);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static boolean loadWorld() {
		try {
			Utils.out("Loading world info...");
			String worldFile = getCurSaveDir() + MadSand.WORLDFILE;

			World world = getMapper().readValue(readFile(worldFile), World.class);
			world.getPlayer().postLoadInit();
			MadSand.instance().setWorld(world);
			System.gc();
			Utils.out("Done loading world info.");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean deleteDirectory(File dir) {
		File[] allContents = dir.listFiles();
		if (allContents != null) {
			for (File file : allContents)
				deleteDirectory(file);
		}
		return dir.delete();
	}

	public static void writeFile(String name, String text) {
		try {
			File file = new File(name);
			PrintWriter pw = new PrintWriter(file);
			pw.print(text);
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String readFile(File file, boolean withNewline) {
		try {
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

	public static String readFile(String name, boolean withNewline) {
		return readFile(new File(name), withNewline);
	}

	public static String readFile(String name) {
		return readFile(name, false);
	}

	public static String getTimeDependentFile(int wx, int wy, int layer) {
		return getCurSaveDir() + "timedependent" + getSectorString(wx, wy, layer)
				+ MadSand.SAVE_EXT;
	}

	static String getSectorString(int wx, int wy, int layer) {
		return getSectorString(wx, wy) + SECTOR_DELIM + layer;
	}

	public static String getSectorString(int wx, int wy) {
		return SECTOR_DELIM + wx + SECTOR_DELIM + wy;
	}

	public static String getCurSaveDir() {
		return MadSand.MAPDIR + MadSand.WORLDNAME + "/";
	}

	static String getWorldXYPath(String file, int wx, int wy) {
		return getCurSaveDir() + file + getSectorString(wx, wy) + MadSand.SAVE_EXT;
	}

	public static File getLocationFile(int wx, int wy) {
		return new File(getWorldXYPath("location", wx, wy));
	}

	static File getSectorFile(int wx, int wy) {
		return new File(getWorldXYPath("sector", wx, wy));
	}

	public static String getNpcFile(int wx, int wy, int layer) {
		return getCurSaveDir() + MadSand.NPCSFILE + getSectorString(wx, wy, layer)
				+ MadSand.SAVE_EXT;
	}

	public static void loadErrMsg() {
		MadSand.switchScreen(MadSand.mainMenu);
		Gui.drawOkDialog(
				"Couldn't to load this world. \n"
						+ "Maybe it was saved in older/newer version of the game or some files are corrupted.\n"
						+ "Check " + ERR_FILE + " for details.");
	}

	public static boolean verifyNextSector(int x, int y) {
		File sectorFile = getSectorFile(x, y);
		return sectorFile.exists();
	}

	private static boolean saveLog() {
		try {
			FileWriter fw = new FileWriter(getCurSaveDir() + MadSand.LOGFILE);

			for (Label logLabel : Gui.overlay.getGameLog().getLabels())
				fw.write(logLabel.getText().toString() + LINEBREAK);

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
					new FileReader(getCurSaveDir() + MadSand.LOGFILE));
			String line;
			int i = 0;
			Label[] labels = Gui.overlay.getGameLog().getLabels();

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
		new File(MadSand.SAVEDIR).mkdirs();
		new File(MadSand.MAPDIR).mkdirs();
		new File(getCurSaveDir()).mkdirs();
	}

}
