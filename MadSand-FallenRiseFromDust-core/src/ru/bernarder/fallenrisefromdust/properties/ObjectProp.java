package ru.bernarder.fallenrisefromdust.properties;

import java.util.HashMap;
import java.util.Vector;

import ru.bernarder.fallenrisefromdust.Tuple;

public class ObjectProp {
	public static HashMap<Integer, String> name =  new HashMap<Integer, String>();
	public static HashMap<Tuple<Integer, String>, HashMap<Integer, Vector<Integer>>> altitems =  new HashMap<Tuple<Integer, String>, HashMap<Integer, Vector<Integer>>>();
	public static HashMap<Integer, Integer> hp = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> harvestHp = new HashMap<Integer, Integer>();
	public static HashMap<Integer, String> interactAction = new HashMap<Integer, String>();
	public static HashMap<Integer, Integer> vRendMasks = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> hRendMasks = new HashMap<Integer, Integer>();
}
