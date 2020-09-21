package hitonoriol.madsand.properties;

import java.util.ArrayList;

import hitonoriol.madsand.entities.LootTable;
import hitonoriol.madsand.enums.Faction;
import hitonoriol.madsand.enums.NpcType;
import hitonoriol.madsand.enums.TradeCategory;

public class NpcContainer {
	public String name;
	
	public int hp, strength, accuracy;
	public int dexterity;

	public int rewardExp;
	public LootTable loot;

	public Faction faction;
	public NpcType type;
	public TradeCategory tradeCategory;

	public ArrayList<Integer> questList;

	public boolean spawnOnce = false;
	public boolean friendly = false;

}
