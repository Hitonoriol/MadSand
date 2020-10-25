package hitonoriol.madsand.properties;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonSetter;

import hitonoriol.madsand.entities.LootTable;
import hitonoriol.madsand.enums.Faction;
import hitonoriol.madsand.enums.NpcState;
import hitonoriol.madsand.enums.NpcType;
import hitonoriol.madsand.enums.TradeCategory;

public class NpcContainer {
	public String name;

	public int lvl = 0;
	public int hp, strength, accuracy;
	public int dexterity = 2, defense;

	public int rewardExp;
	public LootTable loot;

	public Faction faction = Faction.None;
	public NpcType type = NpcType.Regular;
	public TradeCategory tradeCategory;
	public NpcState defaultState;

	public ArrayList<Integer> questList;

	public boolean spawnOnce = false;
	public boolean friendly = false;
	public boolean canTrade = false;

	@JsonSetter("loot")
	public void setLoot(String loot) {
		if (loot != null)
			this.loot = LootTable.parse(loot);
	}
}
