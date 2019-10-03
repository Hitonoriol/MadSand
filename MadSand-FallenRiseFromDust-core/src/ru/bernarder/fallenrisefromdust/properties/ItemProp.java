package ru.bernarder.fallenrisefromdust.properties;

import java.util.HashMap;

import ru.bernarder.fallenrisefromdust.enums.ItemType;

public class ItemProp {
	public static HashMap<Integer, String> name = new HashMap<Integer, String>();
	public static HashMap<Integer, Integer> weight = new HashMap<Integer, Integer>();
	public static HashMap<Integer, ItemType> type = new HashMap<Integer, ItemType>();
	public static HashMap<Integer, Integer> altObject = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> cost = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Boolean> craftable = new HashMap<Integer, Boolean>();
	public static HashMap<Integer, String> recipe = new HashMap<Integer, String>();
	public static HashMap<Integer, String> heal = new HashMap<Integer, String>();
	public static HashMap<Integer, String> useAction = new HashMap<Integer, String>();
}