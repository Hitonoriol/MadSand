package ru.bernarder.fallenrisefromdust;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

public class ThreadedUtils {
	public static boolean mapstop = false;

	public Thread worldGen = new Thread(new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {
			if (MadSand.tonext) {
				if (WorldGen.rand(0, MadSand.ENCOUNTERCHANCE) == MadSand.ENCOUNTERCHANCE) {
					try {
						MadSand.curxwpos = MadSand.tempwx;
						MadSand.curywpos = MadSand.tempwy;
						MadSand.encounter = true;
						MadSand.print("You came to a strange place...");
						BuildScript.execute((GameSaver.getExternal("MadSand_Saves/scripts/encounter.msl")));
					} catch (Exception e) {
						e.printStackTrace(Resource.eps);
						Utils.out("Error on random encounter start: " + e.getMessage());
					}
				} else
					WorldGen.world = WorldGen.Generate(false);
			} else {
				WorldGen.world = WorldGen.Generate(false);
			}
			MadSand.tonext = false;
			MadSand.state = "GAME";
			ThreadedUtils.this.worldGen.stop();
		}
	});

	public Thread initialWorldGen = new Thread(new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {
			WorldGen.world = WorldGen.Generate(false);
			MadSand.state = "GAME";
			Gui.createCharDialog();
			ThreadedUtils.this.worldGen.stop();
		}
	});

	public Thread gotoSector = new Thread(new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {
			MadSand.tempwx = MadSand.curxwpos;
			MadSand.tempwy = MadSand.curywpos;
			MadSand.tonext = true;
			if (!MadSand.multiplayer) {
				MadSand.print("Going to (" + MadSand.curxwpos + ", " + MadSand.curywpos + ")");
				if (!MadSand.encounter)
					GameSaver.saveMap(
							"MadSand_Saves/worlds/" + MadSand.WORLDNAME + "/" + "sector-" + MadSand.curxwpos + "-"
									+ MadSand.curywpos + ".mws",
							WorldGen.world, ObjLayer.ObjLayer, LootLayer.lootLayer, MadSand.curxwpos, MadSand.curywpos,
							MobLayer.mobLayer, CropLayer.cropLayer);
				MadSand.encounter = false;

				if ((Utils.gotodir == "left") && (MadSand.curxwpos > 0)) {
					MadSand.curxwpos -= 1;
					MadSand.x = MadSand.MAPSIZE - 2;
					Utils.updCoords();
				}
				if ((Utils.gotodir == "right") && (MadSand.curxwpos < 9)) {
					MadSand.curxwpos += 1;
					MadSand.x = 0;
					Utils.updCoords();
				}
				if ((Utils.gotodir == "down") && (MadSand.curywpos > 0)) {
					MadSand.curywpos -= 1;
					MadSand.y = MadSand.MAPSIZE - 2;
					Utils.updCoords();
				}
				if ((Utils.gotodir == "up") && (MadSand.curywpos < 9)) {
					MadSand.curywpos += 1;
					MadSand.y = 0;
					Utils.updCoords();
				}
				WorldGen.makeEmpty();
				if (GameSaver.verifyNextSector(MadSand.curxwpos, MadSand.curywpos)) {
					GameSaver.loadMap("MadSand_Saves/worlds/" + MadSand.WORLDNAME + "/" + "sector-" + MadSand.curxwpos
							+ "-" + MadSand.curywpos + ".mws");
				} else {
					MadSand.state = "WORLDGEN";
					new ThreadedUtils().worldGen.start();
				}

			} else {
				if ((Utils.gotodir == "left") && (MadSand.curxwpos > 0)) {
					MadSand.curxwpos -= 1;
					MadSand.x = 98;
					Utils.updCoords();
				}
				if ((Utils.gotodir == "right") && (MadSand.curxwpos < 9)) {
					MadSand.curxwpos += 1;
					MadSand.x = 0;
					Utils.updCoords();
				}
				if ((Utils.gotodir == "down") && (MadSand.curywpos > 0)) {
					MadSand.curywpos -= 1;
					MadSand.y = 98;
					Utils.updCoords();
				}
				if ((Utils.gotodir == "up") && (MadSand.curywpos < 9)) {
					MadSand.curywpos += 1;
					MadSand.y = 0;
					Utils.updCoords();
				}
				try {
					MadSand.out.writeUTF("changewxy");
					MadSand.out.writeUTF(MadSand.curxwpos + "");
					MadSand.out.writeUTF(MadSand.curywpos + "");
					MadSand.out.flush();
					MadSand.out.writeUTF("sectorexists");

					MadSand.out.writeUTF(MadSand.curxwpos + "");
					MadSand.out.writeUTF(MadSand.curywpos + "");
					MadSand.out.flush();
					if (!MadSand.in.readBoolean()) {
						MadSand.KsendSector(true);
					}
					MadSand.state = "GAME";
				} catch (IOException e) {
					e.printStackTrace(Resource.eps);
				}
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
