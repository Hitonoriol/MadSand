package hitonoriol.madsand.properties;

import java.util.Map;

import hitonoriol.madsand.entities.npc.AbstractNpc;

public class NpcProp {
	public static Map<Integer, NpcContainer> npcs;
	public static AbstractNpc spawnNpc(int id, int x, int y) {
		AbstractNpc npc = spawnNpc(id);
		npc.teleport(x, y);
		return npc;
	}

	/* Spawns a new NPC into the cold dark digital limbo */
	public static AbstractNpc spawnNpc(int id) {
		return npcs.get(id).spawn();
	}
}
