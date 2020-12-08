package hitonoriol.madsand.world.worldgen;

import java.util.ArrayList;

public class LakePreset {
	public int lakeRadius;
	public float lakeModifier;
	public ArrayList<Interval> intervals = new ArrayList<>();

	public static class Interval {
		public float from, to;
		public int tile;

		public Interval(float from, float to, int tile) {
			this.from = from;
			this.to = to;
			this.tile = tile;
		}

		public Interval() {
			this(0, 0, 0);
		}
	}
}