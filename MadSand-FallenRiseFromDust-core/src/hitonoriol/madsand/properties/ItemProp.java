package hitonoriol.madsand.properties;

import java.util.HashMap;
import java.util.Vector;

import hitonoriol.madsand.enums.ItemType;
import hitonoriol.madsand.enums.Skill;

public class ItemProp {
	public static HashMap<Integer, String> name = new HashMap<Integer, String>();
	public static HashMap<Integer, Float> weight = new HashMap<Integer, Float>();
	public static HashMap<Integer, Integer> dmg = new HashMap<Integer, Integer>();

	public static HashMap<Integer, Integer> lvl = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> str = new HashMap<Integer, Integer>();

	public static HashMap<Integer, Skill> skill = new HashMap<Integer, Skill>();

	public static HashMap<Integer, Integer> hp = new HashMap<Integer, Integer>();
	public static HashMap<Integer, ItemType> type = new HashMap<Integer, ItemType>();
	public static HashMap<Integer, Integer> altObject = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> cost = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Boolean> unlockable = new HashMap<Integer, Boolean>();
	public static HashMap<Integer, String> recipe = new HashMap<Integer, String>();
	public static HashMap<Integer, Integer> craftQuantity = new HashMap<Integer, Integer>();
	public static HashMap<Integer, String> heal = new HashMap<Integer, String>();
	public static HashMap<Integer, String> useAction = new HashMap<Integer, String>();

	public static HashMap<Integer, Vector<Integer>> craftReq = new HashMap<Integer, Vector<Integer>>();
}