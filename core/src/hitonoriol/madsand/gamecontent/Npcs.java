package hitonoriol.madsand.gamecontent;

import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.resources.GameAssetManager;

public class Npcs extends ContentStorage<NpcDescriptor> {
	private final static Npcs instance = new Npcs();

	protected Npcs() {
		super(new NpcDescriptor());
	}

	public AbstractNpc spawnNpc(int id, int x, int y) {
		AbstractNpc npc = spawnNpc(id);
		npc.teleport(x, y);
		return npc;
	}

	/* Spawns a new NPC into the cold dark digital limbo */
	public AbstractNpc spawnNpc(int id) {
		return get(id).spawn();
	}

	@Override
	public void registerLoader(GameAssetManager manager) {
		manager.contentLoader(instance, NpcDescriptor.class);
	}

	public static Npcs all() {
		return instance;
	}
}
