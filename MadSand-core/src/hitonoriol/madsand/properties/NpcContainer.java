package hitonoriol.madsand.properties;

import java.util.ArrayList;

import hitonoriol.madsand.enums.Faction;
import hitonoriol.madsand.enums.NpcType;

public class NpcContainer {
	public String name;
	
	public int hp, strength, accuracy;
	public int dexterity;

	public int rewardExp;
	public String loot;

	public Faction faction;
	public NpcType type;

	public ArrayList<Integer> questList;

	public boolean spawnOnce = false;
	public boolean friendly = false;

}
