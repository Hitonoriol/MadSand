package hitonoriol.madsand.entities;

import java.util.HashMap;

public class Reputation {
	public final static float RANGE = 100;
	final static float HOSTILE_THR = -5;

	public static float LEAVE_PENALTY = 0.15f;
	
	public static float KILL_PENALTY = -0.25f;
	public static float QUEST_REWARD = 0.3f;

	public HashMap<Faction, Float> values = new HashMap<>();

	public void set(Faction faction, float value) {
		if (Math.abs(value) > RANGE)
			value = (value < 0 ? -1 : 1) * RANGE;
		values.put(faction, value);
	}

	public float get(Faction faction) {
		return values.getOrDefault(faction, 0f);
	}

	public void change(Faction faction, float by) {
		set(faction, get(faction) + by);
	}

	public boolean isHostile(Faction faction) {
		return get(faction) <= HOSTILE_THR;
	}
}
