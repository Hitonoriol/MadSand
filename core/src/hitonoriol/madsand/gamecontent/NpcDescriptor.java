package hitonoriol.madsand.gamecontent;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.annotation.JsonSetter;

import hitonoriol.madsand.Enumerable;
import hitonoriol.madsand.containers.rolltable.LootTable;
import hitonoriol.madsand.entities.Faction;
import hitonoriol.madsand.entities.inventory.item.category.ItemCategory;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.entities.npc.Npc;
import hitonoriol.madsand.util.Utils;

public class NpcDescriptor implements Enumerable {
	private int id = -1;
	public String name;

	public int lvl = 0;
	public int hp, strength, accuracy;
	public int dexterity = 1, defense;

	public int rewardExp;
	public LootTable loot;
	public ArrayList<Integer> projectiles;

	public Faction faction = Faction.None;
	public ItemCategory tradeCategory;
	public AbstractNpc.State defaultState;
	public ArrayList<Integer> questList;

	public boolean spawnOnce = false;
	public boolean friendly = true;
	public boolean canTrade = false;
	public Class<? extends AbstractNpc> type = Npc.class;

	private static NpcClassLoader npcLoader = new NpcClassLoader();

	@Override
	public int id() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String name() {
		return name;
	}

	public AbstractNpc spawn() {
		try {
			return type.getDeclaredConstructor(this.getClass()).newInstance(this);
		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
			return null;
		}
	}

	@JsonSetter("type")
	public void setType(String type) {
		if (type != null)
			this.type = npcLoader.loadClass(type);
	}

	private static class NpcClassLoader extends ClassLoader {
		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends AbstractNpc> loadClass(String name) {
			name = Utils.getPackageName(AbstractNpc.class) + "." + name;
			try {
				return (Class<? extends AbstractNpc>) getParent().loadClass(name);
			} catch (Exception e) {
				e.printStackTrace();
				return Npc.class;
			}
		}
	}
}
