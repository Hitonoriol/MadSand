package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class PlayerActions {
	static void equipHelmet(int id, boolean flag) {
		if (flag) {
			switch (id) {
			case 14:
				values.PlayerStats.helmet = id;
				values.PlayerStats.def[0] = 2;
				Gui.equip[0].setDrawable(new SpriteDrawable(new Sprite(Utils.item[id])));
				break;
			default:
				values.PlayerStats.helmet = 0;
			}

			MadSand.print("Helmet equipped.");
		} else {
			values.PlayerStats.helmet = 0;
			values.PlayerStats.def[0] = 0;
			MadSand.print("Helmet unequipped.");
		}
	}

	static void woodcutUp() {
		values.PlayerStats.woodcutterskill[1]++;
		Utils.out("Woodcutting skill exp++ ( LVL: " + values.PlayerStats.woodcutterskill[0] + " | "
				+ values.PlayerStats.woodcutterskill[1] + "/" + values.PlayerStats.woodcutterskill[2] + ")");
		checkSkills();
	}

	static void survivalUp() {
		values.PlayerStats.survivalskill[1]++;
		Utils.out("Survival skill exp++ ( LVL: " + values.PlayerStats.survivalskill[0] + " | "
				+ values.PlayerStats.survivalskill[1] + "/" + values.PlayerStats.survivalskill[2] + ")");
		checkSkills();
	}

	static void minerUp() {
		values.PlayerStats.miningskill[1]++;
		Utils.out("Mining skill exp++ ( LVL: " + values.PlayerStats.miningskill[0] + " | "
				+ values.PlayerStats.miningskill[1] + "/" + values.PlayerStats.miningskill[2] + ")");
		checkSkills();
	}

	static void harvestUp() {
		values.PlayerStats.harvestskill[1]++;
		Utils.out("Harvest skill exp++ ( LVL: " + values.PlayerStats.harvestskill[0] + " | "
				+ values.PlayerStats.harvestskill[1] + "/" + values.PlayerStats.harvestskill[2] + ")");
		checkSkills();
	}

	static void craftingup() {
		values.PlayerStats.craftingskill[1]++;
		Utils.out("Crafting skill exp++ ( LVL: " + values.PlayerStats.craftingskill[0] + " | "
				+ values.PlayerStats.craftingskill[1] + "/" + values.PlayerStats.craftingskill[2] + ")");
		checkSkills();
	}

	static void regen() {
		if (!MadSand.roguelike) {
			values.PlayerStats.stamina += values.PlayerStats.survivalskill[0] * 3;
			healPlayer(values.PlayerStats.survivalskill[0] * 3);
		}
	}

	static void checkSkills() {
		if (values.PlayerStats.woodcutterskill[1] >= values.PlayerStats.woodcutterskill[2]) {
			values.PlayerStats.woodcutterskill[0] += 1;
			values.PlayerStats.woodcutterskill[1] = 0;
			MadSand.print("You improved your woodcutter skill to level " + values.PlayerStats.woodcutterskill[0] + "!");
		}
		if (values.PlayerStats.miningskill[1] >= values.PlayerStats.miningskill[2]) {
			values.PlayerStats.miningskill[0] += 1;
			values.PlayerStats.miningskill[1] = 0;
			MadSand.print("You improved your mining skill to level " + values.PlayerStats.miningskill[0] + "!");
		}
		if (values.PlayerStats.survivalskill[1] >= values.PlayerStats.survivalskill[2]) {
			values.PlayerStats.survivalskill[0] += 1;
			values.PlayerStats.survivalskill[1] = 0;
			MadSand.print("You improved your survival skill to level " + values.PlayerStats.survivalskill[0] + "!");
		}
		if (values.PlayerStats.harvestskill[1] >= values.PlayerStats.harvestskill[2]) {
			values.PlayerStats.harvestskill[0] += 1;
			values.PlayerStats.harvestskill[1] = 0;
			MadSand.print("You improved your harvesting skill to level " + values.PlayerStats.survivalskill[0] + "!");
		}
		if (values.PlayerStats.craftingskill[1] >= values.PlayerStats.craftingskill[2]) {
			values.PlayerStats.craftingskill[0] += 1;
			values.PlayerStats.craftingskill[1] = 0;
			MadSand.print("You improved your crafting skill to level " + values.PlayerStats.survivalskill[0] + "!");
		}
	}

	static void equipChestplate(int id, boolean flag) {
		if (flag) {
			switch (id) {
			case 16:
				values.PlayerStats.cplate = id;
				values.PlayerStats.def[1] = 1;
				Gui.equip[1].setDrawable(new SpriteDrawable(new Sprite(Utils.item[id])));
				break;
			case 17:
				values.PlayerStats.cplate = id;
				values.PlayerStats.def[1] = 3;
				Gui.equip[1].setDrawable(new SpriteDrawable(new Sprite(Utils.item[id])));
				break;
			case 18:
				values.PlayerStats.cplate = id;
				values.PlayerStats.def[1] = 5;
				Gui.equip[1].setDrawable(new SpriteDrawable(new Sprite(Utils.item[id])));
				break;
			case 19:
				values.PlayerStats.cplate = id;
				values.PlayerStats.def[1] = 8;
				Gui.equip[1].setDrawable(new SpriteDrawable(new Sprite(Utils.item[id])));
				break;
			default:
				values.PlayerStats.cplate = 0;
			}

			MadSand.print("Chestplate equipped.");
		} else {
			values.PlayerStats.cplate = 0;
			values.PlayerStats.def[1] = 0;
			MadSand.print("Chestplate unequipped.");
		}
	}

	static void equipShield(int id, boolean flag) {
		if (flag) {
			switch (id) {
			case 15:
				values.PlayerStats.shield = id;
				values.PlayerStats.def[2] = 3;
				Gui.equip[2].setDrawable(new SpriteDrawable(new Sprite(Utils.item[id])));
				break;
			default:
				values.PlayerStats.shield = 0;
			}

			MadSand.print("Shield equipped.");
		} else {
			values.PlayerStats.shield = 0;
			values.PlayerStats.def[2] = 0;
			MadSand.print("Shield unequipped.");
		}
	}

	static void damagePlayer(int to) {
		values.PlayerStats.blood -= to;
		MadSand.sendhp();
	}

	static void healPlayer(int to) {
		if (values.PlayerStats.blood + to < values.PlayerStats.maxblood) {
			values.PlayerStats.blood += to;
		} else {
			values.PlayerStats.blood = values.PlayerStats.maxblood;
		}
		MadSand.sendhp();
	}

	static void increaseStamina(int to) {
		if (values.PlayerStats.stamina + to < values.PlayerStats.maxstamina) {
			values.PlayerStats.stamina += to;
		} else {
			values.PlayerStats.stamina = values.PlayerStats.maxstamina;
		}
		MadSand.sendhp();
	}
}
