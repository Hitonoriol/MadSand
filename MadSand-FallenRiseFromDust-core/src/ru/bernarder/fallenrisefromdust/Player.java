package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.scenes.scene2d.Actor;

import ru.bernarder.fallenrisefromdust.enums.*;
import values.PlayerStats;

public class Player
{
	int hp,mhp,exp,nexp,lvl;
	float speed, splim;	//moves/actions per world tick
	String name;
	boolean isMain;
	Faction faction;
	Direction direction;

	public Player(String name) {
		this.name = name;
	}

	public static boolean interact(final int x, final int y, final String direction) {
		try {
			boolean ret = false;
			if ((Player.isCollision(x, y, direction, 1)) || (MobLayer.isMobCollision(MadSand.look))) {
				if (MobLayer.getMobId(MadSand.x, MadSand.y, MadSand.look) == 8) {
					Utils.invent = true;
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
				if (ObjLayer.getBlock(x, y, direction, MadSand.curlayer) == 6) {
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
				if ((ObjLayer.getBlock(x, y, direction, MadSand.curlayer) == 14) && (MadSand.curlayer > 0)) {
					MadSand.curlayer -= 1;
					PlayerStats.exp += 2;
				}
				if (!ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][0].equals("-1")) {
					PlayerStats.exp += 3;
					if (!ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][2].equals("-1")) {
						if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][2].equals("mining")) {
							PlayerActions.minerUp();
						}
						if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][2].equals("wood")) {
							PlayerActions.woodcutUp();
						}
						if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][2].equals("crafting")) {
							PlayerActions.craftingup();
						}
						if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][2].equals("survival")) {
							PlayerActions.survivalUp();
						}
						if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][2].equals("harvest")) {
							PlayerActions.harvestUp();
						}
					}
	
					if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][1].equals("-1")) {
						ObjLayer.dmgObj(x, y, direction);
						if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][0].indexOf(",") == -1) {
							Player.skillBonusItems(x, y, direction, -1);
						} else {
							ObjLayer.dmgObj(x, y, direction);
							String b[] = ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][0].split(",");
							int i = 0;
							while (i < b.length) {
								Player.skillBonusItems(x, y, direction, Integer.parseInt(b[i]));
								i++;
							}
						}
					} else {
						if (PlayerStats.hand == Integer
								.parseInt(ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][1])) {
							if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][0].indexOf(",") == -1) {
								Player.skillBonusItems(x, y, direction, -1);
								ObjLayer.dmgObj(x, y, direction);
							} else {
								ObjLayer.dmgObj(x, y, direction);
								String b[] = ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][0].split(",");
								int i = 0;
								while (i < b.length) {
									Player.skillBonusItems(x, y, direction, Integer.parseInt(b[i]));
									i++;
	
								}
	
							}
						}
					}
				}
				if (PlayerStats.hand == 11) {
					PlayerStats.exp += 1;
					ObjLayer.dmgObj(x, y, direction);
					MadSand.print("You hit a thing with a pickaxe.");
				}
				if (ObjLayer.getBlock(x, y, direction, MadSand.curlayer) == 11) {
					ObjLayer.delObject(MadSand.x, MadSand.y, MadSand.look);
					ObjLayer.placeObjInDirection(MadSand.x, MadSand.y, 12, MadSand.look);
					return ret;
				}
				if (ObjLayer.getBlock(x, y, direction, MadSand.curlayer) == 12) {
					ObjLayer.delObject(MadSand.x, MadSand.y, MadSand.look);
					ObjLayer.placeObjInDirection(MadSand.x, MadSand.y, 11, MadSand.look);
					return ret;
				}
			}
			new ThreadedUtils().mapSendK.start();
			return ret;
		} catch (Exception e) {
			Utils.out("Fail on ObjectInteraction: " + e.getMessage());
			return false;
		}
	}

	public static boolean isCollision(int x, int y, String direction, int flag) {
		boolean collision = false;
		if (((flag == 0) && (ObjLayer.getBlock(x, y, direction, MadSand.curlayer) == 12))
				|| (ObjLayer.getBlock(x, y, direction, MadSand.curlayer) == 0)
				|| (ObjLayer.getBlock(x, y, direction, MadSand.curlayer) == 666)) {
			collision = false;
		} else
			collision = true;
		return collision;
	}

	public static boolean isCollisionMask(int x, int y) {
		if (x < MadSand.MAPSIZE && y < MadSand.MAPSIZE) {
			if (ObjLayer.ObjLayer[x][y][0][0] == 666) {
				return true;
			}
		}
		return false;
	}

	static boolean standingOnLoot(int x, int y) {
		String ass = LootLayer.getCell(x, y);
		int n = ass.length();
		if (n == "n".length()) {
			return false;
		}
		return true;
	}

	static void skillBonusItems(int x, int y, String direction, int id) {
		if (id == -1) {
			if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][2].equals("mining")) {
				InvUtils.putItem(Integer.parseInt(ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][0]),
						PlayerStats.miningskill[0], false);
			} else if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][2].equals("wood")) {
				int bonus = PlayerStats.woodcutterskill[0];
				if (PlayerStats.hand == 3)
					bonus *= 2;
				InvUtils.putItem(Integer.parseInt(ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][0]), bonus,
						false);
	
			} else if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][2].equals("crafting")) {
				InvUtils.putItem(Integer.parseInt(ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][0]),
						PlayerStats.craftingskill[0], false);
			} else if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][2].equals("survival")) {
				InvUtils.putItem(Integer.parseInt(ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][0]),
						PlayerStats.survivalskill[0], false);
			} else if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][2].equals("harvest")) {
				InvUtils.putItem(Integer.parseInt(ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][0]),
						PlayerStats.harvestskill[0], false);
			} else
				InvUtils.putItem(Integer.parseInt(ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][0]), 1, false);
		} else {
			if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][2].equals("mining")) {
				InvUtils.putItem(id, PlayerStats.miningskill[0], false);
			}
			if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][2].equals("wood")) {
				int bonus = PlayerStats.woodcutterskill[0];
				if (PlayerStats.hand == 3)
					bonus *= 2;
				InvUtils.putItem(id, bonus, false);
			}
			if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][2].equals("crafting")) {
				InvUtils.putItem(id, PlayerStats.craftingskill[0], false);
			}
			if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][2].equals("survival")) {
				InvUtils.putItem(id, PlayerStats.survivalskill[0], false);
			}
			if (ObjLayer.altitems[ObjLayer.getBlock(x, y, direction, MadSand.curlayer)][2].equals("harvest")) {
				InvUtils.putItem(id, PlayerStats.harvestskill[0], false);
			} else
				InvUtils.putItem(id, 1, false);
		}
	}
}