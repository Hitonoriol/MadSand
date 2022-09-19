package hitonoriol.madsand.world;

import static hitonoriol.madsand.resources.Resources.LINEBREAK;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.MadSand.Screens;
import hitonoriol.madsand.commons.exception.Exceptions;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.resources.Serializer;
import hitonoriol.madsand.util.Log;
import hitonoriol.madsand.util.Utils;

public class GameSaver {
	public static final String SECTOR_DELIM = "!";
	public static final long saveFormatVersion = 9;

	public static final String SAVE_EXT = ".msf";
	public static final String SAVEDIR = "MadSand_Saves/";
	public static final String MAPDIR = SAVEDIR + "worlds/";
	public static final String LOGFILE = "/log" + SAVE_EXT;
	public static final String NPCSFILE = "NPCs";
	public static final String WORLDFILE = "/World" + SAVE_EXT;

	private final static List<Runnable> loaderTasks = new ArrayList<>();
	private final static Serializer serializer = new Serializer(DefaultTyping.EVERYTHING);

	private String saveDir;
	private World world;
	private WorldMapSaver worldMapSaver = new WorldMapSaver(this);

	public GameSaver(String worldName) {
		saveDir = getCurSaveDir(worldName);
	}

	public GameSaver(World world) {
		this.world = world;
	}

	public WorldMapSaver getWorldMapSaver() {
		return worldMapSaver;
	}

	public WorldMap getWorldMap() {
		return world.getWorldMap();
	}

	public void setWorldMap(WorldMap worldMap) {
		worldMapSaver.setWorldMap(worldMap);
	}
	
	public void save() {
		if (world.inEncounter()) {
			Gui.drawOkDialog("You can't save during an encounter!");
			return;
		}
		createDirs();
		world.logout();
		saveLog();
		if (saveLocation() && saveWorld())
			MadSand.print("Game saved!");
		else
			MadSand.print("Couldn't save the game. Check logs.");
	}
	
	public boolean saveLocation(int wx, int wy) {
		WorldMapSaver saver = world.getMapSaver();
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

	public boolean saveLocation() {
		return saveLocation(world.wx(), world.wy());
	}
	
	private boolean saveWorld() {
		try {
			File worldFile = new File(getCurSaveDir() + GameSaver.WORLDFILE);
			Player player = MadSand.player();
			player.stats.equipment.setStatBonus(false);
			serializer.writeValue(worldFile, world);
			player.stats.equipment.setStatBonus(true);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean saveLog() {
		try {
			FileWriter fw = new FileWriter(getCurSaveDir() + GameSaver.LOGFILE);

			for (Label logLabel : Gui.overlay.getGameLog().getLabels())
				fw.write(logLabel.getText().toString() + LINEBREAK);

			fw.close();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void load() throws IOException {
		Utils.out("Loading world [%s]...", getCurSaveDir());
		Utils.printMemoryInfo();
		File worldFile = new File(getCurSaveDir());

		if (!worldFile.exists() || !worldFile.isDirectory()) {
			MadSand.switchScreen(Screens.MainMenu);
			Gui.drawOkDialog("Couldn't load this world");
			return;
		}

		Gui.overlay.getGameLog().clear();
		MadSand.world().close(); // Close the current world (might not be the same as this.world)

		loadWorld();
		Lua.init();
		world.updateLight();
		Utils.dbg("Loaded world map: %X", MadSand.world().getWorldMap().hashCode());
		loadLog();
		world.getPlayer().postLoadInit(world.getCurLoc());
		Utils.out("Initialized player: %s", world.getPlayer());
		MadSand.game().setWorld(world);
		Utils.out("Loaded [%s] successfully!", getCurSaveDir());
		System.gc();
		Utils.printMemoryInfo();
	}

	public void loadLocation(int wx, int wy) throws IOException {
		WorldMapSaver saver = world.getMapSaver();
		byte[] data = Files.readAllBytes(Paths.get(getSectorFile(wx, wy).toURI()));
		Utils.out("Loading location data for [%d, %d]...", wx, wy);
		saver.loadLocationInfo(wx, wy);
		Utils.out("Loading location layers...", wx, wy);
		saver.bytesToLocation(data, wx, wy);
		finalizeLoading();
		System.gc();
	}

	public void loadLocation() throws IOException {
		loadLocation(world.wx(), world.wy());
	}

	private void loadWorld() throws IOException {
		String worldFile = getCurSaveDir() + GameSaver.WORLDFILE;
		Utils.out("Loading world data from `%s`...", worldFile);
		world = serializer.readValue(readFile(worldFile), World.class);
		System.gc();
		Utils.out("Done loading world data.");
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

	public String getTimeDependentFile(int wx, int wy, int layer) {
		return getCurSaveDir() + "timedependent" + getSectorString(wx, wy, layer)
				+ GameSaver.SAVE_EXT;
	}

	public static String getSectorString(int wx, int wy, int layer) {
		return getSectorString(wx, wy) + SECTOR_DELIM + layer;
	}

	public static String getSectorString(int wx, int wy) {
		return SECTOR_DELIM + wx + SECTOR_DELIM + wy;
	}

	public String getCurSaveDir(String worldName) {
		return GameSaver.MAPDIR + worldName + "/";
	}

	public String getCurSaveDir() {
		if (saveDir == null)
			saveDir = getCurSaveDir(world.getName());
		return saveDir;
	}

	public String getWorldXYPath(String file, int wx, int wy) {
		return getCurSaveDir() + file + getSectorString(wx, wy) + GameSaver.SAVE_EXT;
	}

	public File getLocationFile(int wx, int wy) {
		return new File(getWorldXYPath("location", wx, wy));
	}

	public File getSectorFile(int wx, int wy) {
		return new File(getWorldXYPath("sector", wx, wy));
	}

	public String getNpcFile(int wx, int wy, int layer) {
		return getCurSaveDir() + GameSaver.NPCSFILE + getSectorString(wx, wy, layer)
				+ GameSaver.SAVE_EXT;
	}

	public boolean verifyNextSector(int x, int y) {
		File sectorFile = getSectorFile(x, y);
		return sectorFile.exists();
	}

	private boolean loadLog() {
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(getCurSaveDir() + GameSaver.LOGFILE));
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

	public void createDirs() {
		createSaveDir();
		new File(getCurSaveDir()).mkdirs();
	}

	public static void postLoadAction(Runnable action) {
		loaderTasks.add(action);
	}

	private static void finalizeLoading() {
		Utils.dbg("Running post-loading tasks (%s)...", loaderTasks.size());
		loaderTasks.forEach(Runnable::run);
	}

	static Serializer serializer() {
		return serializer;
	}

	public static void createSaveDir() {
		new File(MAPDIR).mkdirs();
	}

	public static Void loadingError(Throwable e) {
		Gui.drawOkDialog(
				"Couldn't load this world. \n\n"
						+ ExceptionUtils.getStackTrace(e) + "\n\n"
						+ "Check " + Log.OUT_FILE + " for details.");
		return Exceptions.printStackTrace(e);
	}
}
