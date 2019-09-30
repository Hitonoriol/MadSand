package ru.bernarder.fallenrisefromdust.properties;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

public class WorldGenProp {
	public static Vector<String> name = new Vector<String>();

	private static HashMap<Integer, Vector<Vector<Integer>>> tile = new HashMap<Integer, Vector<Vector<Integer>>>();
	private static HashMap<Integer, Vector<Vector<Integer>>> object = new HashMap<Integer, Vector<Vector<Integer>>>();
	private static HashMap<Integer, Vector<Integer>> lake = new HashMap<Integer, Vector<Integer>>();
	private static HashMap<Integer, Vector<Integer>> underworld = new HashMap<Integer, Vector<Integer>>();
	private static HashMap<Integer, Vector<Integer>> dungeon = new HashMap<Integer, Vector<Integer>>();

	public static Vector<Vector<Integer>> getBiomeTiles(int id) {
		return tile.get(id);
	}

	public static Vector<Vector<Integer>> getBiomeObjects(int id) {
		return tile.get(id);
	}

	public static Vector<Integer> getBiomeLake(int id) {
		return lake.get(id);
	}

	public static Vector<Integer> getBiomeUnderworld(int id) {
		return underworld.get(id);
	}

	public static Vector<Integer> getBiomedungeon(int id) {
		return dungeon.get(id);
	}

	static Vector<Vector<Integer>> loadGroup(Vector<String> group) {
		Vector<Vector<Integer>> sblock = new Vector<Vector<Integer>>();
		Vector<Integer> tblock;
		for (int i = 0; i < group.size(); ++i) {
			tblock = new Vector<Integer>();
			StringTokenizer tok = new StringTokenizer(group.get(i), ",");
			while (tok.hasMoreTokens()) {
				tblock.add(Integer.parseInt(tok.nextToken()));
			}
			sblock.add(tblock);
		}
		return sblock;
	}

	public static void loadTileBlock(int id, Vector<Integer> def, Vector<String> group, Vector<String> vlake) {
		Vector<Vector<Integer>> sblock = loadGroup(group);
		sblock.add(0, def);
		tile.put(id, sblock);

		Vector<Integer> reslake = loadGroup(vlake).get(0);
		lake.put(id, reslake);
	}

	public static void loadObjectBlock(int id, Vector<String> group) {
		object.put(id, loadGroup(group));
	}

	static void loadUnderworldBlock(int id, String defT, String defO, Vector<String> ore, Vector<String> vdungeon) {
		Vector<Integer> dblock = new Vector<Integer>();
		for (int i = 0; i < vdungeon.size(); ++i) {
			dblock.add(Integer.parseInt(vdungeon.get(i)));
		}
		dungeon.put(id, dblock);

		Vector<Integer> sblock = loadGroup(ore).get(0);
		sblock.add(0, Integer.parseInt(defT));
		sblock.add(1, Integer.parseInt(defO));
		underworld.put(id, sblock);
	}
}
