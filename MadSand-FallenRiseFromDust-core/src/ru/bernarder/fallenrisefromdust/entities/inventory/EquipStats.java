package ru.bernarder.fallenrisefromdust.entities.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ru.bernarder.fallenrisefromdust.Gui;
import ru.bernarder.fallenrisefromdust.Utils;

public class EquipStats {
	public int lvl;

	public int rollMax, rollMin;

	public int constitution;
	public int dexterity;
	public int strength;
	public int accuracy;
	public int intelligence;

	private static float multiplier = 1.5f;

	public EquipStats(int lvl) {
		this.lvl = lvl;
		rollMax = (int) (lvl * multiplier + 1);
		rollMin = lvl / 2 + 1;

		roll();
	}

	@JsonIgnore
	private boolean isUnlucky() { // with each item lvl the chance to roll a debuff halves; idk how good is that,
									// we'll see
		return Utils.rand(0, lvl) == lvl;
	}

	private int rollStat() {
		int ret = Utils.rand(rollMin, rollMax);
		int f = (isUnlucky()) ? -1 : 1;
		if (isUnlucky())
			f = 0;
		return ret * f;
	}

	public EquipStats roll() {
		constitution = rollStat();
		dexterity = rollStat();
		strength = rollStat();
		accuracy = rollStat();
		intelligence = rollStat();

		return this;
	}

	@JsonIgnore
	public String getString() {
		String ret = "";
		ret += "Constitution: " + constitution + Gui.LINEBREAK;
		ret += "Dexterity: " + dexterity + Gui.LINEBREAK;
		ret += "Strength: " + strength + Gui.LINEBREAK;
		ret += "Accuracy: " + accuracy + Gui.LINEBREAK;
		ret += "Intelligence: " + intelligence + Gui.LINEBREAK;
		return ret;
	}
}
