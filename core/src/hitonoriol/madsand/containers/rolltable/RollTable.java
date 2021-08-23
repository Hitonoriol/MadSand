package hitonoriol.madsand.containers.rolltable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import hitonoriol.madsand.util.Utils;

public class RollTable<T> {
	public boolean exclusiveRoll = false; // Stop after the first successful roll
	public int rollCount = 1;
	public SortedMap<Float, Entry<T>> lootTable = new TreeMap<>(new Comparator<Float>() {
		@Override
		public int compare(Float o1, Float o2) {
			return o1.compareTo(o2);
		}
	});

	@SafeVarargs
	public final RollTable<T> add(float probability, T... items) {
		Entry<T> entry;
		if (lootTable.containsKey(probability))
			entry = lootTable.get(probability);
		else
			entry = new Entry<T>();
		entry.items.addAll(Arrays.asList(items));
		return this;
	}

	private ArrayList<T> roll(ArrayList<T> itemList, boolean exclusiveRoll) {
		T item;
		double roll = Utils.randPercent();

		for (Map.Entry<Float, Entry<T>> entry : lootTable.entrySet()) {
			if (!Utils.percentRoll(roll, entry.getKey()))
				continue;
			else {
				item = entry.getValue().rollItem();
				if (item != null) {
					itemList.add(item);
					roll = Utils.randPercent();
				}

				if (exclusiveRoll)
					break;
			}
		}

		return itemList;
	}

	private ArrayList<T> roll(ArrayList<T> itemList) {
		return roll(itemList, exclusiveRoll);
	}

	public ArrayList<T> roll(int times) {
		ArrayList<T> itemList = new ArrayList<>();

		for (int i = 0; i < times; ++i)
			roll(itemList);

		return itemList;
	}

	public ArrayList<T> roll() {
		return roll(rollCount);
	}

	public T rollSingle() {
		return roll(new ArrayList<>(1), true).get(0);
	}

	public static class Entry<T> {
		public ArrayList<T> items;

		public Entry(ArrayList<T> items) {
			this.items = items;
		}

		public Entry() {
			items = new ArrayList<>();
		}

		public T rollItem() {
			return Utils.randElement(items);
		}
	}
}
