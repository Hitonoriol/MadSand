package hitonoriol.madsand.properties;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonSetter;

import hitonoriol.madsand.entities.Faction;
import hitonoriol.madsand.entities.LootTable;
import hitonoriol.madsand.entities.NpcState;
import hitonoriol.madsand.entities.NpcType;
import hitonoriol.madsand.enums.TradeCategory;
import hitonoriol.madsand.world.WorkerType;

public class NpcContainer {
	public String name;

	public int lvl = 0;
	public int hp, strength, accuracy;
	public int dexterity = 1, defense;

	public int rewardExp;
	public LootTable loot;

	public Faction faction = Faction.None;
	public NpcType type = NpcType.Regular;
	public TradeCategory tradeCategory;
	public NpcState defaultState;
	public WorkerType worker;

	public ArrayList<Integer> questList;

	public boolean spawnOnce = false;
	public boolean friendly = true;
	public boolean canTrade = false;

	@JsonSetter("loot")
	public void setLoot(String loot) {
		if (loot != null)
			this.loot = LootTable.parse(loot);
	}
}
