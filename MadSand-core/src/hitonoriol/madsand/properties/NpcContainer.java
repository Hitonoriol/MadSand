package hitonoriol.madsand.properties;

import java.util.ArrayList;

import hitonoriol.madsand.entities.LootTable;
import hitonoriol.madsand.enums.Faction;
import hitonoriol.madsand.enums.NpcState;
import hitonoriol.madsand.enums.NpcType;
import hitonoriol.madsand.enums.TradeCategory;

public class NpcContainer {
	public String name;
	
	public int lvl;
	public int hp, strength, accuracy;
	public int dexterity, defense;

	public int rewardExp;
	public LootTable loot;

	public Faction faction;
	public NpcType type;
	public TradeCategory tradeCategory;
	public NpcState defaultState;

	public ArrayList<Integer> questList;

	public boolean spawnOnce = false;
	public boolean friendly = false;

}
