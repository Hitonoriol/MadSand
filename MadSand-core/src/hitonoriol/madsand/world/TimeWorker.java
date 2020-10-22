package hitonoriol.madsand.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.MadSand;

public class TimeWorker {

	List<Node> time = new ArrayList<>();

	public int ticksPerHour = 150; // ticks per one hourTick() trigger
	public int worldtime = 6; // time (00 - 23)
	public int tick = 0; // tick counter, resets every <ticksPerHour> ticks
	public double globalTick = 0; // global tick counter, never resets

	public void processTime(double ticks) {
		globalTick += ticks;
		MadSand.world.ticks((int) ticks);
		Collections.sort(time);

		Iterator<Node> i = time.iterator();
		Node action;
		while (i.hasNext()) {
			action = i.next();

			if (action.finishedBy <= globalTick)
				action.act();

			i.remove();
		}
	}

	public void queueAction(int speed, double length, Action action) { //length = % of max AP consumed by action
		time.add(new Node(speed, globalTick + length, action));
	}

	private float M_HOUR = 60;
	private int H_DAY = 24;

	@JsonIgnore
	public int getWorldTimeMinute() {
		return (int) (M_HOUR * ((float) tick / (float) ticksPerHour));
	}

	@JsonIgnore
	public int getWorldTimeDay() {
		return (int) (globalTick / ticksPerHour) / H_DAY;
	}

	public class Node implements java.lang.Comparable<Node> {
		int speed;
		double finishedBy;
		Action action;

		public Node(int speed, double finishedBy, Action action) {
			this.speed = speed;
			this.finishedBy = finishedBy;
			this.action = action;
		}

		public void act() {
			action.act();
		}

		@Override
		public int compareTo(Node o) {
			return this.speed - o.speed;
		}
	}

	public interface Action {
		void act();
	}
}
