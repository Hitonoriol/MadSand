package ru.bernarder.fallenrisefromdust.properties;

import java.util.HashMap;
import java.util.Vector;

public class TileProp {
	public static HashMap<Integer, String> name = new HashMap<Integer, String>();
	public static HashMap<Integer, Integer> damage = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> cover = new HashMap<Integer, Integer>();
	public static HashMap<Integer, HashMap<Integer, Vector<Integer>>> altitems = new HashMap<Integer, HashMap<Integer, Vector<Integer>>>();
	public static HashMap<Integer, String> oninteract = new HashMap<Integer, String>();
}