package ru.bernarder.fallenrisefromdust;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

import ru.bernarder.fallenrisefromdust.enums.GameState;

public class GameSaver {

	Thread saver = new Thread(new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {
			MadSand.print("World saving... Don't quit the game!");
			GameSaver.saveWorld(MadSand.WORLDNAME);
			MadSand.print("World saved!");
			GameSaver.this.saver.stop();
		}
	});

	public void init() {
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
			e.printStackTrace(Resource.eps);
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
		if (!new File("MadSand_Saves/worlds/" + filename).exists()) {
			new File("MadSand_Saves/worlds/" + filename).mkdirs();
		}
		if (!new File("MadSand_Saves/scripts").exists()) {
			new File("MadSand_Saves/scripts").mkdirs();
		}
		String curf = "MadSand_Saves/worlds/" + filename + "/" + "sector-" + MadSand.curxwpos + "-" + MadSand.curywpos
				+ ".mws";
		saveChar();
		// saveMap();
		saveChar();
	}

	public static boolean loadWorld(String filename) {
		File f = new File("MadSand_Saves/worlds/" + filename);
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

		File file = new File("MadSand_Saves/worlds/" + filename + "/" + "sector-" + MadSand.curxwpos + "-"
				+ MadSand.curywpos + ".mws");
		if (file.exists()) {
			MadSand.world.clearCurLoc();
			loadMap("MadSand_Saves/worlds/" + filename + "/" + "sector-" + MadSand.curxwpos + "-" + MadSand.curywpos
					+ ".mws");
			loadChar(1);
			MadSand.print("Loaded Game!");
			return true;
		} else
			return false;

	}

	public static boolean verifyNextSector(int x, int y) {
		File file = new File("MadSand_Saves/worlds/" + MadSand.WORLDNAME + "/" + "sector-" + x + "-" + y + ".mws");
		if (file.exists()) {
			return true;
		}
		return false;
	}

	static void saveChar() {
		String fl = "MadSand_Saves/worlds/" + MadSand.WORLDNAME + "/" + MadSand.name + ".mc";
		String query = "";

		String global = ":";
		String invblock = "-@-";
		String idblock = "-!-";
		int i = 0;
		while (i < 30) {
			query = query + MadSand.inv[i][0] + idblock + MadSand.inv[i][1] + invblock;
			i++;
		}

		query = query + global + MadSand.player.hp + invblock + MadSand.player.mhp + invblock + MadSand.player.atk
				+ invblock + MadSand.player.accur + invblock + MadSand.player.stamina + invblock + MadSand.player.exp + invblock
				+ MadSand.player.requiredexp + invblock + MadSand.player.lvl + invblock + MadSand.player.helmet + invblock
				+ MadSand.player.cplate + invblock + MadSand.player.shield + invblock + MadSand.player.maxstamina + invblock
				+ MadSand.player.woodcutterskill[0] + invblock + MadSand.player.woodcutterskill[1] + invblock
				+ MadSand.player.woodcutterskill[2] + invblock + MadSand.player.miningskill[0] + invblock
				+ MadSand.player.miningskill[1] + invblock + MadSand.player.miningskill[2] + invblock
				+ MadSand.player.survivalskill[0] + invblock + MadSand.player.survivalskill[1] + invblock
				+ MadSand.player.survivalskill[2] + invblock + MadSand.player.harvestskill[0] + invblock
				+ MadSand.player.harvestskill[1] + invblock + MadSand.player.harvestskill[2] + invblock
				+ MadSand.player.craftingskill[0] + invblock + MadSand.player.craftingskill[1] + invblock
				+ MadSand.player.craftingskill[2] + invblock + MadSand.player.rest[0] + invblock + MadSand.player.rest[1]
				+ invblock + MadSand.player.rest[2] + invblock + MadSand.player.rest[3] + invblock + MadSand.player.dexterity
				+ invblock + MadSand.player.intelligence + global + MadSand.player.x + invblock + MadSand.player.y + global
				+ MadSand.curxwpos + invblock + MadSand.curywpos + global;
		saveToExternal(fl, query);
	}

	static String saveInv() {
		String query = "";

		String global = ":";
		String invblock = "-@-";
		String idblock = "-!-";
		int i = 0;
		while (i < 30) {
			query = query + MadSand.inv[i][0] + idblock + MadSand.inv[i][1] + invblock;
			i++;
		}
		query = query + global;
		return query;
	}

	static void loadInv(String query) {
		String global = ":";
		String invblock = "-@-";
		String idblock = "-!-";
		int i = 0;
		String[] glob = query.split(global);
		while (i < 30) {
			MadSand.inv[i][0] = Integer.parseInt(glob[0].split(invblock)[i].split(idblock)[0]);
			MadSand.inv[i][1] = Integer.parseInt(glob[0].split(invblock)[i].split(idblock)[1]);
			i++;
		}
	}

	static void loadChar(int flag) {
		String fl = "MadSand_Saves/worlds/" + MadSand.WORLDNAME + "/" + MadSand.name + ".mc";
		File file = new File(fl);
		if (!file.exists()) {
			return;
		}
		String query = "";
		query = getExternal(fl);
		String global = ":";
		String invblock = "-@-";
		String idblock = "-!-";
		int i = 0;
		String[] glob = query.split(global);
		while (i < 30) {
			MadSand.inv[i][0] = Integer.parseInt(glob[0].split(invblock)[i].split(idblock)[0]);
			MadSand.inv[i][1] = Integer.parseInt(glob[0].split(invblock)[i].split(idblock)[1]);
			i++;
		}
		String[] hpb = glob[1].split(invblock);
		// STATS
		MadSand.player.hp = Integer.parseInt(hpb[0]);
		MadSand.player.mhp = Integer.parseInt(hpb[1]);
		MadSand.player.atk = Integer.parseInt(hpb[2]);
		MadSand.player.accur = Integer.parseInt(hpb[2]);
		MadSand.player.luck = Integer.parseInt(hpb[3]);
		MadSand.player.stamina = Float.parseFloat(hpb[4]);
		MadSand.player.maxstamina = Float.parseFloat(hpb[11]);
		MadSand.player.exp = Integer.parseInt(hpb[5]);
		MadSand.player.requiredexp = Integer.parseInt(hpb[6]);
		MadSand.player.lvl = Integer.parseInt(hpb[7]);
		MadSand.player.dexterity = Integer.parseInt(hpb[31]);
		MadSand.player.intelligence = Integer.parseInt(hpb[32]);
		// EQUIPMENT
		MadSand.player.helmet = Integer.parseInt(hpb[8]);
		MadSand.player.cplate = Integer.parseInt(hpb[9]);
		MadSand.player.shield = Integer.parseInt(hpb[10]);
		// SKILLS
		MadSand.player.woodcutterskill[0] = Integer.parseInt(hpb[12]);
		MadSand.player.woodcutterskill[1] = Integer.parseInt(hpb[13]);
		MadSand.player.woodcutterskill[2] = Integer.parseInt(hpb[14]);
		MadSand.player.miningskill[0] = Integer.parseInt(hpb[15]);
		MadSand.player.miningskill[1] = Integer.parseInt(hpb[16]);
		MadSand.player.miningskill[2] = Integer.parseInt(hpb[17]);
		MadSand.player.survivalskill[0] = Integer.parseInt(hpb[18]);
		MadSand.player.survivalskill[1] = Integer.parseInt(hpb[19]);
		MadSand.player.survivalskill[2] = Integer.parseInt(hpb[20]);
		MadSand.player.harvestskill[0] = Integer.parseInt(hpb[21]);
		MadSand.player.harvestskill[1] = Integer.parseInt(hpb[22]);
		MadSand.player.harvestskill[2] = Integer.parseInt(hpb[23]);
		MadSand.player.craftingskill[0] = Integer.parseInt(hpb[24]);
		MadSand.player.craftingskill[1] = Integer.parseInt(hpb[25]);
		MadSand.player.craftingskill[2] = Integer.parseInt(hpb[26]);
		// REST POINT
		MadSand.player.rest[0] = Integer.parseInt(hpb[27]);
		MadSand.player.rest[1] = Integer.parseInt(hpb[28]);
		MadSand.player.rest[2] = Integer.parseInt(hpb[29]);
		MadSand.player.rest[3] = Integer.parseInt(hpb[30]);

		String[] hpbb = glob[2].split(invblock);
		String[] ph = glob[3].split(invblock);
		if (flag == 1) {
			MadSand.curxwpos = Integer.parseInt(ph[0]);
			MadSand.curywpos = Integer.parseInt(ph[1]);
			MadSand.player.x = Integer.parseInt(hpbb[0]);
			MadSand.player.y = Integer.parseInt(hpbb[1]);
			Utils.updCoords();
		}
	}

	public static void saveMap(Map map) {
		// TODO
	}

	public static String saveMapSec() {
		// TODO ((i dont even remember what this one was supposed to do))
		return "";
	}

	public static void loadMap(String filename) {
		// TODO
	}

	public static String[] mapDataBlock(String filename, int num) {
		String query = (getExternal(filename));
		String[] buf = query.split("@");
		return buf;
	}

	public static void loadMapSec(String query, int x, int y) {
		// TODO
	}

}
