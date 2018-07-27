package ru.bernarder.fallenrisefromdust.strings;

import java.util.HashMap;

public class InventoryNames {
	public static HashMap<Integer, String> name = new HashMap<Integer, String>();
	public static HashMap<Integer, Integer> type = new HashMap<Integer, Integer>(); // TODO enum types
	public static HashMap<Integer, Integer> altObject = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> cost = new HashMap<Integer, Integer>(); // TODO remove
	public static HashMap<Integer, Boolean> craftable = new HashMap<Integer, Boolean>();
	public static HashMap<Integer, String> recipe = new HashMap<Integer, String>();
	public static HashMap<Integer, String> heal = new HashMap<Integer, String>();
	// 1-type 2-altobject 3-cost 4-craftable 5-recipe 6-heal
}