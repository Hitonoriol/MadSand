package ru.bernarder.fallenrisefromdust.properties;

import java.util.HashMap;

import ru.bernarder.fallenrisefromdust.enums.Faction;
import ru.bernarder.fallenrisefromdust.enums.NpcType;

public class NpcProp {
	public static HashMap<Integer, Integer> hp = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> maxhp = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> atk = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> accuracy = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> rewardexp = new HashMap<Integer,Integer>();
	public static HashMap<Integer, Faction> faction = new HashMap<Integer,Faction>();
	public static HashMap<Integer, NpcType> type = new HashMap<Integer,NpcType>();
	public static HashMap<Integer, String> drop = new HashMap<Integer,String>();
	public static HashMap<Integer, String> name = new HashMap<Integer,String>();
	public static HashMap<Integer, String> qids = new HashMap<Integer,String>();
	public static HashMap<Integer, Boolean> spawnonce = new HashMap<Integer,Boolean>();
	public static HashMap<Integer, Boolean> friendly = new HashMap<Integer,Boolean>();

}
