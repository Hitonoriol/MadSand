package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.enums.GameState;

public class ThreadedUtils {
	public static boolean mapstop = false;

	public Thread worldGen = new Thread(new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {
			if (MadSand.tonext) {
				if (Utils.rand(0, MadSand.ENCOUNTERCHANCE) == MadSand.ENCOUNTERCHANCE) {
					try {
						MadSand.world.curxwpos = MadSand.tempwx;
						MadSand.world.curywpos = MadSand.tempwy;
						MadSand.encounter = true;
						MadSand.print("You came to a strange place...");
						BuildScript.execute((GameSaver.getExternal("MadSand_Saves/scripts/encounter.msl")));
					} catch (Exception e) {
						e.printStackTrace(Resource.eps);
						Utils.out("Error on random encounter start: " + e.getMessage());
					}
				} else
					MadSand.world.Generate();
			} else {
				MadSand.world.Generate();
			}
			MadSand.tonext = false;
			MadSand.state = GameState.GAME;
			ThreadedUtils.this.worldGen.stop();
		}
	});

	public Thread initialWorldGen = new Thread(new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {
			MadSand.world.Generate();
			MadSand.state = GameState.GAME;
			ThreadedUtils.this.worldGen.stop();
		}
	});

	public Thread gotoSector = new Thread(new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {
			MadSand.tempwx = MadSand.world.curxwpos;
			MadSand.tempwy = MadSand.world.curywpos;
			MadSand.tonext = true;
			MadSand.print("Going to (" + MadSand.world.curxwpos + ", " + MadSand.world.curywpos + ")");
			MadSand.encounter = false;

			if ((Utils.gotodir == "left") && (MadSand.world.curxwpos > 0)) {
				MadSand.world.curxwpos -= 1;
				MadSand.player.x = World.MAPSIZE - 2;
				Utils.updCoords();
			}
			if ((Utils.gotodir == "right") && (MadSand.world.curxwpos < 9)) {
				MadSand.world.curxwpos += 1;
				MadSand.player.x = 0;
				Utils.updCoords();
			}
			if ((Utils.gotodir == "down") && (MadSand.world.curywpos > 0)) {
				MadSand.world.curywpos -= 1;
				MadSand.player.y = World.MAPSIZE - 2;
				Utils.updCoords();
			}
			if ((Utils.gotodir == "up") && (MadSand.world.curywpos < 9)) {
				MadSand.world.curywpos += 1;
				MadSand.player.y = 0;
				Utils.updCoords();
			}
			MadSand.world.clearCurLoc();
			if (GameSaver.verifyNextSector(MadSand.world.curxwpos, MadSand.world.curywpos)) {
				GameSaver.loadMap("MadSand_Saves/worlds/" + MadSand.WORLDNAME + "/" + "sector-" + MadSand.world.curxwpos + "-"
						+ MadSand.world.curywpos + ".mws");
			} else {
				MadSand.state = GameState.WORLDGEN;
				new ThreadedUtils().worldGen.start();
			}

			ThreadedUtils.this.gotoSector.stop();
		}
	});

	public Thread wontlisten = new Thread(new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace(Resource.eps);
			}
			MadSand.dontlisten = false;
			ThreadedUtils.this.wontlisten.stop();
		}
	});
}
