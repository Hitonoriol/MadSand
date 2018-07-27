package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.scenes.scene2d.Actor;

import ru.bernarder.fallenrisefromdust.strings.Objects;
import values.PlayerStats;

public class ObjLayer {
	public static int[][][][] ObjLayer;
	public static int[] vRendMasks, hRendMasks;
	public static String altitems[][];
	public static int[] ObjToug;
	public static int mx;
	public static int my;

	public static void randPlaceObject(int id, int size) {
		int x = WorldGen.random.nextInt(size);
		int y = WorldGen.random.nextInt(size);
		AddObj(x, y, id);
	}

	public static void randPlaceObject(int[] id, int size) {
		int x = WorldGen.random.nextInt(size);
		int y = WorldGen.random.nextInt(size);
		AddObj(x, y, id[WorldGen.random.nextInt(id.length)]);
	}

	public static void randPlaceTile(int id, int size) {
		WorldGen.world[WorldGen.random.nextInt(size)][WorldGen.random.nextInt(size)][0] = id;
	}

	public static void putTileInDir(int x, int y, String dir, int id) {
		if (dir == "up")
			WorldGen.world[(y + 1)][x][MadSand.curlayer] = id;
		if (dir == "down")
			WorldGen.world[(y - 1)][x][MadSand.curlayer] = id;
		if (dir == "left")
			WorldGen.world[y][(x - 1)][MadSand.curlayer] = id;
		if (dir == "right") {
			WorldGen.world[y][(x + 1)][MadSand.curlayer] = id;
		}
	}

	public static void init() {
		ObjLayer = new int[MadSand.MAPSIZE + MadSand.BORDER][MadSand.MAPSIZE + MadSand.BORDER][2][MadSand.OBJLEVELS];
	}

	public static void dmgObj(int x, int y, String direction) {
		if (direction.equals("up")) {
			ObjLayer[x][(y + 1)][1][MadSand.curlayer] -= 1;
			if (ObjLayer[x][(y + 1)][1][MadSand.curlayer] <= 0) {
				ObjLayer[x][(y + 1)][0][MadSand.curlayer] = 0;
			}
		}
		if ((direction.equals("down")) && (y > 0)) {
			ObjLayer[x][(y - 1)][1][MadSand.curlayer] -= 1;
			if (ObjLayer[x][(y - 1)][1][MadSand.curlayer] <= 0) {
				ObjLayer[x][(y - 1)][0][MadSand.curlayer] = 0;
			}
		}
		if ((direction.equals("left")) && (x > 0)) {
			ObjLayer[(x - 1)][y][1][MadSand.curlayer] -= 1;
			if (ObjLayer[(x - 1)][y][1][MadSand.curlayer] <= 0) {
				ObjLayer[(x - 1)][y][0][MadSand.curlayer] = 0;
			}
		}
		if (direction.equals("right")) {
			ObjLayer[(x + 1)][y][1][MadSand.curlayer] -= 1;
			if (ObjLayer[(x + 1)][y][1][MadSand.curlayer] <= 0) {
				ObjLayer[(x + 1)][y][0][MadSand.curlayer] = 0;
			}
		}
		new ThreadedUtils().mapSendK.start();
	}

	public static void setObjToughness(int x, int y, int to) {
		ObjLayer[x][y][1][MadSand.curlayer] = to;
	}

	public static void AddObj(int x, int y, int id) {
		if (ObjLayer[x][y][0][MadSand.curlayer] == 0 && WorldGen.world[y][x][0] != 8) {
			ObjLayer[x][y][0][MadSand.curlayer] = id;
			int i = vRendMasks[id];
			if (y + 1 < MadSand.MAPSIZE - 1) {
				while (i > 0) {
					if (y + i < MadSand.MAPSIZE) {
						ObjLayer[x][(y + i)][0][MadSand.curlayer] = 666;
						ObjLayer[x][(y + i)][1][MadSand.curlayer] = 666;
					}
					i--;
				}
			}
			i = hRendMasks[id];
			if (x + 1 < MadSand.MAPSIZE - 1) {
				while (i > 0) {
					if (y + i < MadSand.MAPSIZE) {
						ObjLayer[x + i][(y)][0][MadSand.curlayer] = 666;
						ObjLayer[x + i][(y)][1][MadSand.curlayer] = 666;
					}
					i--;
				}
			}

			setObjToughness(x, y, ObjToug[id]);
		}
		new ThreadedUtils().mapSendK.start();
	}

	public static void AddObjForce(int x, int y, int id) {
		ObjLayer[x][y][0][MadSand.curlayer] = id;
		int i = vRendMasks[id];
		if (y + 1 < 99) {
			while (i > 0) {
				ObjLayer[x][(y + i)][0][MadSand.curlayer] = 666;
				ObjLayer[x][(y + i)][1][MadSand.curlayer] = 666;
				i--;
			}
		}
		i = hRendMasks[id];
		if (x + 1 < MadSand.MAPSIZE - 1) {
			while (i > 0) {
				if (y + i < MadSand.MAPSIZE) {
					ObjLayer[x + i][(y)][0][MadSand.curlayer] = 666;
					ObjLayer[x + i][(y)][1][MadSand.curlayer] = 666;
				}
				i--;
			}
		}

		setObjToughness(x, y, ObjToug[id]);

		new ThreadedUtils().mapSendK.start();
	}

	public static void AddObjForce(int x, int y, int id, int layer) {
		ObjLayer[x][y][0][layer] = id;
		int i = vRendMasks[id];
		if (y + 1 < 99) {
			while (i > 0) {
				ObjLayer[x][(y + i)][0][layer] = 666;
				ObjLayer[x][(y + i)][1][layer] = 666;
				i--;
			}
		}
		i = hRendMasks[id];
		if (x + 1 < MadSand.MAPSIZE - 1) {
			while (i > 0) {
				if (y + i < MadSand.MAPSIZE) {
					ObjLayer[x + i][(y)][0][MadSand.curlayer] = 666;
					ObjLayer[x + i][(y)][1][MadSand.curlayer] = 666;
				}
				i--;
			}
		}

		setObjToughness(x, y, ObjToug[id]);

		new ThreadedUtils().mapSendK.start();
	}

	public static void AddObj(int x, int y, int id, int xwpos, int ywpos, int layer) {
		if (ObjLayer[x][y][0][layer] == 0) {
			ObjLayer[x][y][0][layer] = id;
			int i = vRendMasks[id];
			if (y + 1 < 99) {
				while (i > 0) {
					ObjLayer[x][(y + i)][0][layer] = 666;
					ObjLayer[x][(y + i)][1][layer] = 666;
					i--;
				}
			}
			i = hRendMasks[id];
			if (x + 1 < MadSand.MAPSIZE - 1) {
				while (i > 0) {
					if (y + i < MadSand.MAPSIZE) {
						ObjLayer[x + i][(y)][0][MadSand.curlayer] = 666;
						ObjLayer[x + i][(y)][1][MadSand.curlayer] = 666;
					}
					i--;
				}
			}

			ObjLayer[x][y][1][layer] = ObjToug[id];
		}
		new ThreadedUtils().mapSendK.start();
	}

	public static int getBlock(int x, int y, String direction, int layer) {
		int id = 0;
		try {
			if (direction.equals("up"))
				id = ObjLayer[x][(y + 1)][0][layer];
			if ((direction.equals("down")) && (y > 0))
				id = ObjLayer[x][(y - 1)][0][layer];
			if ((direction.equals("left")) && (x > 0))
				id = ObjLayer[(x - 1)][y][0][layer];
			if (direction.equals("right"))
				id = ObjLayer[(x + 1)][y][0][layer];
			return id;
		} catch (Exception e) {
			return 0;
		}
	}

	public static int getBlock(int x, int y, int layer) {
		int id = 0;
		if ((x < MadSand.MAPSIZE + MadSand.BORDER) && (y < MadSand.MAPSIZE + MadSand.BORDER) && (x >= 0) && (y >= 0))
			id = ObjLayer[x][y][0][layer];
		if (id == 666)
			return 0;
		return id;
	}

	public static int distantGetBlock(int x, int y, String direction, int dist) {
		int id = 0;
		if (direction.equals("up"))
			id = ObjLayer[x][(y + dist)][0][MadSand.curlayer];
		if ((direction.equals("down")) && (y > 0))
			id = ObjLayer[x][(y - dist)][0][MadSand.curlayer];
		if ((direction.equals("left")) && (x > 0))
			id = ObjLayer[(x - dist)][y][0][MadSand.curlayer];
		if (direction.equals("right"))
			id = ObjLayer[(x + dist)][y][0][MadSand.curlayer];
		return id;
	}

	public static String getObjName(int x, int y, String direction) {
		String name = "";
		int id = 0;
		if ((direction.equals("up")) && (y < MadSand.MAPSIZE + MadSand.BORDER))
			id = ObjLayer[x][(y + 1)][0][MadSand.curlayer];
		if ((direction.equals("down")) && (y > 0))
			id = ObjLayer[x][(y - 1)][0][MadSand.curlayer];
		if ((direction.equals("left")) && (x > 0))
			id = ObjLayer[(x - 1)][y][0][MadSand.curlayer];
		if ((direction.equals("right")) && (x < MadSand.MAPSIZE + MadSand.BORDER)
				&& (y < MadSand.MAPSIZE + MadSand.BORDER))
			id = ObjLayer[(x + 1)][y][0][MadSand.curlayer];
		if (id != 0) {
			name = Objects.name.get(id);
		}
		return name;
	}

	static void skillBonusItems(int x, int y, String direction, int id) {
		if (id == -1) {
			if (altitems[getBlock(x, y, direction, MadSand.curlayer)][2].equals("mining")) {
				InvUtils.putItem(Integer.parseInt(altitems[getBlock(x, y, direction, MadSand.curlayer)][0]),
						PlayerStats.miningskill[0], false);
			} else if (altitems[getBlock(x, y, direction, MadSand.curlayer)][2].equals("wood")) {
				int bonus = PlayerStats.woodcutterskill[0];
				if (PlayerStats.hand == 3)
					bonus *= 2;
				InvUtils.putItem(Integer.parseInt(altitems[getBlock(x, y, direction, MadSand.curlayer)][0]), bonus,
						false);

			} else if (altitems[getBlock(x, y, direction, MadSand.curlayer)][2].equals("crafting")) {
				InvUtils.putItem(Integer.parseInt(altitems[getBlock(x, y, direction, MadSand.curlayer)][0]),
						PlayerStats.craftingskill[0], false);
			} else if (altitems[getBlock(x, y, direction, MadSand.curlayer)][2].equals("survival")) {
				InvUtils.putItem(Integer.parseInt(altitems[getBlock(x, y, direction, MadSand.curlayer)][0]),
						PlayerStats.survivalskill[0], false);
			} else if (altitems[getBlock(x, y, direction, MadSand.curlayer)][2].equals("harvest")) {
				InvUtils.putItem(Integer.parseInt(altitems[getBlock(x, y, direction, MadSand.curlayer)][0]),
						PlayerStats.harvestskill[0], false);
			} else
				InvUtils.putItem(Integer.parseInt(altitems[getBlock(x, y, direction, MadSand.curlayer)][0]), 1, false);
		} else {
			if (altitems[getBlock(x, y, direction, MadSand.curlayer)][2].equals("mining")) {
				InvUtils.putItem(id, PlayerStats.miningskill[0], false);
			}
			if (altitems[getBlock(x, y, direction, MadSand.curlayer)][2].equals("wood")) {
				int bonus = PlayerStats.woodcutterskill[0];
				if (PlayerStats.hand == 3)
					bonus *= 2;
				InvUtils.putItem(id, bonus, false);
			}
			if (altitems[getBlock(x, y, direction, MadSand.curlayer)][2].equals("crafting")) {
				InvUtils.putItem(id, PlayerStats.craftingskill[0], false);
			}
			if (altitems[getBlock(x, y, direction, MadSand.curlayer)][2].equals("survival")) {
				InvUtils.putItem(id, PlayerStats.survivalskill[0], false);
			}
			if (altitems[getBlock(x, y, direction, MadSand.curlayer)][2].equals("harvest")) {
				InvUtils.putItem(id, PlayerStats.harvestskill[0], false);
			} else
				InvUtils.putItem(id, 1, false);
		}
	}

	public static boolean interact(final int x, final int y, final String direction) {
		try {
			boolean ret = false;
			if ((isCollision(x, y, direction, 1)) || (MobLayer.isMobCollision(MadSand.look))) {
				if (MobLayer.getMobId(MadSand.x, MadSand.y, MadSand.look) == 8) {
					SysMethods.invent = true;
					MadSand.showDialog(2, "Shopkeeper: Hello, traveller. Would you like to buy or sell something?", 0);
					Gui.acceptD.setText("Buy");
					Gui.refuseD.setText("Sell");
					Gui.acceptD.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
						public void changed(com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent event,
								Actor actor) {
							InvUtils.emptyInvT();
							LootLayer.addLootToTInv(MobLayer.getMobLoot(x, y, direction));
							MadSand.state = "BUY";
							MadSand.maindialog.setVisible(false);
							Gui.mousemenu.setVisible(false);
							MadSand.dontlisten = true;
							new ThreadedUtils().wontlisten.start();
						}
					});
					Gui.refuseD.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
						public void changed(com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent event,
								Actor actor) {
							MadSand.state = "INVENTORY";
							MadSand.dontlisten = true;
							MadSand.tradeflag = true;
							new ThreadedUtils().wontlisten.start();
							MadSand.maindialog.setVisible(false);
						}
					});
				}
				if (getBlock(x, y, direction, MadSand.curlayer) == 6) {
					if (PlayerStats.rest[0] == -1)
						PlayerStats.exp += 50;
					PlayerStats.rest[0] = x;
					PlayerStats.rest[1] = y;
					PlayerStats.rest[2] = MadSand.curxwpos;
					PlayerStats.rest[3] = MadSand.curywpos;
					MadSand.print("This place is now your rest point.");
				}
				if (MobLayer.isQuestMob(MobLayer.getMobId(x, y, MadSand.look))) {
					PlayerStats.exp += 5;
					String qlist[] = MobLayer.getMobStat(x, y, 11, MadSand.look).split(",");
					int i = 0;
					while (i < qlist.length) {
						if (MadSand.quests[i][0] != 1) {
							QuestUtils.invokeQuest(i);
							break;
						}
						if (QuestUtils.questList[i][QuestUtils.REPEATEBLE] == "1") {
							QuestUtils.invokeQuest(i);
							break;
						}
						i++;
					}
				}
				if ((getBlock(x, y, direction, MadSand.curlayer) == 14) && (MadSand.curlayer > 0)) {
					MadSand.curlayer -= 1;
					PlayerStats.exp += 2;
				}
				if (!altitems[getBlock(x, y, direction, MadSand.curlayer)][0].equals("-1")) {
					PlayerStats.exp += 3;
					if (!altitems[getBlock(x, y, direction, MadSand.curlayer)][2].equals("-1")) {
						if (altitems[getBlock(x, y, direction, MadSand.curlayer)][2].equals("mining")) {
							PlayerActions.minerUp();
						}
						if (altitems[getBlock(x, y, direction, MadSand.curlayer)][2].equals("wood")) {
							PlayerActions.woodcutUp();
						}
						if (altitems[getBlock(x, y, direction, MadSand.curlayer)][2].equals("crafting")) {
							PlayerActions.craftingup();
						}
						if (altitems[getBlock(x, y, direction, MadSand.curlayer)][2].equals("survival")) {
							PlayerActions.survivalUp();
						}
						if (altitems[getBlock(x, y, direction, MadSand.curlayer)][2].equals("harvest")) {
							PlayerActions.harvestUp();
						}
					}

					if (altitems[getBlock(x, y, direction, MadSand.curlayer)][1].equals("-1")) {
						dmgObj(x, y, direction);
						if (altitems[getBlock(x, y, direction, MadSand.curlayer)][0].indexOf(",") == -1) {
							skillBonusItems(x, y, direction, -1);
						} else {
							dmgObj(x, y, direction);
							String b[] = altitems[getBlock(x, y, direction, MadSand.curlayer)][0].split(",");
							int i = 0;
							while (i < b.length) {
								skillBonusItems(x, y, direction, Integer.parseInt(b[i]));
								i++;
							}
						}
					} else {
						if (PlayerStats.hand == Integer
								.parseInt(altitems[getBlock(x, y, direction, MadSand.curlayer)][1])) {
							if (altitems[getBlock(x, y, direction, MadSand.curlayer)][0].indexOf(",") == -1) {
								skillBonusItems(x, y, direction, -1);
								dmgObj(x, y, direction);
							} else {
								dmgObj(x, y, direction);
								String b[] = altitems[getBlock(x, y, direction, MadSand.curlayer)][0].split(",");
								int i = 0;
								while (i < b.length) {
									skillBonusItems(x, y, direction, Integer.parseInt(b[i]));
									i++;

								}

							}
						}
					}
				}
				if (PlayerStats.hand == 11) {
					PlayerStats.exp += 1;
					dmgObj(x, y, direction);
					MadSand.print("You hit a thing with a pickaxe.");
				}
				if (getBlock(x, y, direction, MadSand.curlayer) == 11) {
					delObject(MadSand.x, MadSand.y, MadSand.look);
					placeObjInDirection(MadSand.x, MadSand.y, 12, MadSand.look);
					return ret;
				}
				if (getBlock(x, y, direction, MadSand.curlayer) == 12) {
					delObject(MadSand.x, MadSand.y, MadSand.look);
					placeObjInDirection(MadSand.x, MadSand.y, 11, MadSand.look);
					return ret;
				}
			}
			new ThreadedUtils().mapSendK.start();
			return ret;
		} catch (Exception e) {
			SysMethods.out("Fail on ObjectInteraction: " + e.getMessage());
			return false;
		}
	}

	public static boolean isCollision(int x, int y, String direction, int flag) {
		boolean collision = false;
		if (((flag == 0) && (getBlock(x, y, direction, MadSand.curlayer) == 12))
				|| (getBlock(x, y, direction, MadSand.curlayer) == 0)
				|| (getBlock(x, y, direction, MadSand.curlayer) == 666)) {
			collision = false;
		} else
			collision = true;
		return collision;
	}

	public static boolean isCollisionMask(int x, int y) {
		if (x < MadSand.MAPSIZE && y < MadSand.MAPSIZE) {
			if (ObjLayer[x][y][0][0] == 666) {
				return true;
			}
		}
		return false;
	}

	public static boolean isDistCollision(int x, int y, String direction, int dist) {
		boolean collision = false;
		if (distantGetBlock(x, y, direction, dist) != 0)
			collision = true;
		return collision;
	}

	public static void delObject(int x, int y, String direction) {
		if (direction.equals("up"))
			ObjLayer[x][(y + 1)][0][MadSand.curlayer] = 0;
		if ((direction.equals("down")) && (y > 0))
			ObjLayer[x][(y - 1)][0][MadSand.curlayer] = 0;
		if ((direction.equals("left")) && (x > 0))
			ObjLayer[(x - 1)][y][0][MadSand.curlayer] = 0;
		if (direction.equals("right"))
			ObjLayer[(x + 1)][y][0][MadSand.curlayer] = 0;
		new ThreadedUtils().mapSendK.start();
	}

	public static void delObjectL(int x, int y, int layer) {
		ObjLayer[x][y][0][layer] = 0;
		new ThreadedUtils().mapSendK.start();
	}

	public static void placeObjInDirection(int x, int y, int id, String direction) {
		System.out.println(id);
		if (direction.equals("up")) {
			AddObj(x, y + 1, id, MadSand.curxwpos, MadSand.curywpos, MadSand.curlayer);
		}
		if ((direction.equals("down")) && (y > 0)) {
			AddObj(x, y - 1, id, MadSand.curxwpos, MadSand.curywpos, MadSand.curlayer);
		}
		if ((direction.equals("left")) && (x > 0)) {
			AddObj(x - 1, y, id, MadSand.curxwpos, MadSand.curywpos, MadSand.curlayer);
		}
		if (direction.equals("right")) {
			AddObj(x + 1, y, id, MadSand.curxwpos, MadSand.curywpos, MadSand.curlayer);
		}
		new ThreadedUtils().mapSendK.start();
	}

	public static int rend(int w, int h) {
		int tile = 0;
		if (w <= MadSand.MAPSIZE && h <= MadSand.MAPSIZE && w >= 0 && h >= 0)
			tile = ObjLayer[w][h][0][MadSand.curlayer];
		if (tile <= MadSand.LASTOBJID || tile == 666)
			return tile;
		else
			return 0;
	}
}
