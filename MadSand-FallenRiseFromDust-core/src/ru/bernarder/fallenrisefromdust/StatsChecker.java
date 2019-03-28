package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Gdx;
import java.io.File;
import values.PlayerStats;

public class StatsChecker {
	public static void checkStats() {
		if (PlayerStats.exp >= PlayerStats.requiredexp) {
			PlayerStats.lvl += 1;
			PlayerStats.exp = 0;
			PlayerStats.requiredexp *= 2;

			MadSand.print("You've leveled up!");
			PlayerStats.atk += 1;
			PlayerStats.maxblood += PlayerStats.survivalskill[0] * 3;
			PlayerStats.maxstamina += PlayerStats.survivalskill[0] * 3;
		}

		if (PlayerStats.stamina > PlayerStats.maxstamina) {
			PlayerStats.stamina = PlayerStats.maxstamina;
		}
		if (PlayerStats.stamina < 0.0F) {
			PlayerStats.stamina = 0.0F;
		}

		if (PlayerStats.blood > PlayerStats.maxblood) {
			PlayerStats.blood = PlayerStats.maxblood;
		}

		if (PlayerStats.blood <= 0) {
			if (MadSand.roguelike) {
				new File("MadSand_Saves//" + MadSand.name + ".mc").delete();
				WorldGen.Generate(false);
				MadSand.state = "NMENU";
				PlayerStats.blood = PlayerStats.maxblood;
				Gdx.input.setInputProcessor(Gui.stage);
			} else {
				PlayerStats.blood = 0;
				Gdx.input.setInputProcessor(Gui.dead);
				MadSand.state = "DEAD";
				InvUtils.emptyInv();
			}
		}
	}
}
