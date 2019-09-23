package ru.bernarder.fallenrisefromdust;

import java.util.Random;

import com.badlogic.gdx.Gdx;

import ru.bernarder.fallenrisefromdust.enums.*;
import ru.bernarder.fallenrisefromdust.properties.ItemProp;
import ru.bernarder.fallenrisefromdust.properties.ObjectProp;

public class Player {

	public int x = new Random().nextInt(World.MAPSIZE);
	public int y = new Random().nextInt(World.MAPSIZE);

	public int hand = 0;
	public int accur = 2; // ACCUR
	public int hp = 200; // CONSTITUTION*10
	public int mhp = 200;
	public int str = 3;// ATK
	public int luck = 1; // LUCK
	public int dexterity = 1; // DEX
	public int intelligence = 1; // INT
	public float stamina = 50.0F; // STAMINA*5
	public float maxstamina = 50.0F;
	public int[] def = new int[3];

	public int[] rest = { -1, -1, -1, -1 };

	public int helmet = 0;
	public int cplate = 0;
	public int shield = 0;

	public int lvl = 0;
	public int exp = 0;
	public int requiredexp = 100;

	public int[] woodcutterskill = { 1, 0, 50 };
	public int[] miningskill = { 1, 0, 50 };
	public int[] survivalskill = { 1, 0, 65 };
	public int[] harvestskill = { 1, 0, 35 };
	public int[] craftingskill = { 1, 0, 30 };

	Inventory inventory;

	float speed, splim; // moves/actions per world tick
	String name;
	boolean isMain;
	Faction faction;
	Direction look = Direction.DOWN;

	public Player(String name) {
		this.name = name;
	}

	void reinit() {
		inventory = new Inventory(str * 50);
	}

	public void checkHands(int id) {
		if (inventory.getSameCell(id, 1) == -1)
			MadSand.player.hand = 0;
	}

	public boolean dropItem(int id, int quantity) {
		boolean r = false;
		// TODO
		return r;
	}

	void interact(final Direction direction) { // TODO: BuildScript onInteract events
		int id = MadSand.world.getCurLoc().getObject(x, y, look).id;
		Utils.out("Interacting with " + id);
		String action = ObjectProp.interactAction.get(id);
		if (action != "-1") {
			BuildScript.execute(action);
			return;
		}
		Tuple<Integer, String> pair = new Tuple<Integer, String>(id, "hand");
		int item = MapObject.getAltItem(id);
		int hand = Integer.parseInt(ObjectProp.altitems.get(pair));
		if (item != -1) {
			if (hand == -1 || hand == MadSand.player.hand) {
				MadSand.player.inventory.putItem(item, 1);
				MadSand.world.getCurLoc().dmgObjInDir(x, y, direction);
			}
		}
	}

	public boolean isCollision(Direction direction, int flag) {
		boolean collision = false;
		int oid = MadSand.world.getCurLoc().getObject(x, y, direction).id;
		if (((flag == 0) && (oid == 12)) || (oid == 0) || (oid == 666)) {
			collision = false;
		} else
			collision = true;
		return collision;
	}

	public static boolean isCollisionMask(int x, int y) {
		if (x < World.MAPSIZE && y < World.MAPSIZE) {
			if (MadSand.world.getCurLoc().getObject(x, y).id == 666) {
				return true;
			}
		}
		return false;
	}

	static boolean standingOnLoot(int x, int y) {
		if (MadSand.world.getCurLoc().getLoot(x, y).equals(Map.nullLoot))
			return false;
		else
			return true;
	}

	void skillBonusItems(int x, int y, String direction, int id) {
		// Idk what's this thing
	}

	void damage(int to) {
		hp -= to;
	}

	void heal(int to) {
		if (hp + to < mhp) {
			hp += to;
		} else {
			hp = mhp;
		}
	}

	void increaseStamina(int to) {
		if (stamina + to < maxstamina) {
			stamina += to;
		} else {
			stamina = maxstamina;
		}
	}

	public void checkStats() {
		if (exp >= requiredexp) {
			lvl += 1;
			exp = 0;
			requiredexp *= 2;
			MadSand.print("You've leveled up!");
			str += 1;
			mhp += 50;
			maxstamina += 15;
		}

		if (stamina > maxstamina) {
			stamina = maxstamina;
		}
		if (stamina < 0.0F) {
			stamina = 0.0F;
		}

		if (hp > mhp) {
			hp = mhp;
		}

		if (hp <= 0) {
			hp = 0;
			Gdx.input.setInputProcessor(Gui.dead);
			MadSand.state = GameState.DEAD;
		}
	}

	public void useItem() {
		int id = hand;
		int ptile = MadSand.world.getTileId(x, y);
		checkHands(id);
		String action = ItemProp.useAction.get(id);
		if (action != "-1") {
			BuildScript.execute(action);
			return;
		}
		if ((ptile == 6) || (ptile == 16)) {
			MadSand.print("You entered the dungeon.");
			MadSand.world.curlayer += 1;
			MadSand.world.delObj(MadSand.player.x, MadSand.player.y);
		}
		if (id == 6) {
			if (ptile == 0) {
				MadSand.world.putMapTile(MadSand.player.y, MadSand.player.x, 6);
				MadSand.print("You dug a hole.");
			}
			if (ptile == 3) {
				MadSand.world.putMapTile(MadSand.player.y, MadSand.player.x, 16);
				MadSand.print("You dug a hole.");
			}
			if (ptile == 1) {
				MadSand.player.inventory.putItem(5, 1, true);
				MadSand.world.putMapTile(MadSand.player.y, MadSand.player.x, 0);
				MadSand.print("You dug some clay");
			}
			if (ptile == 2) {
				MadSand.player.inventory.putItem(9, 1, true);
				MadSand.world.putMapTile(MadSand.player.y, MadSand.player.x, 0);
				MadSand.print("You dug some flint");
			}
		}
		if (Item.getType(id) == ItemType.Consumable.get()) {
			MadSand.print("You ate one " + ItemProp.name.get(id));
			MadSand.player.heal(Integer.parseInt(ItemProp.heal.get(id).split(":")[0]));
			MadSand.player.increaseStamina(Integer.parseInt(ItemProp.heal.get(id).split(":")[1]));
		}
		if ((id == 9) && (MadSand.player.inventory.getSameCell(9, 1) != -1)
				&& (MadSand.player.inventory.getSameCell(1, 5) != -1)) {
			MadSand.print("You placed a campfire");
			MadSand.player.inventory.delItem(9, 1);
			MadSand.player.inventory.delItem(1, 5);
			MadSand.world.getCurLoc().addObject(MadSand.player.x, MadSand.player.y, MadSand.player.look, 6);
		}
		if (Item.getType(id) == ItemType.HeadArmor.get()) {
			// equip helmet
		}
		if (Item.getType(id) == ItemType.ChestArmor.get()) {
			// equip chestplate
		}
		if (Item.getType(id) == ItemType.Shield.get()) {
			// equip shield
		}
		if (Item.getType(id) == ItemType.Crop.get()) { // crop
			MadSand.player.inventory.delItem(id, 1);
			MadSand.world.getCurLoc().addObject(MadSand.player.x, MadSand.player.y, MadSand.player.look,
					Item.getAltObject(id));
			// put crop in direction
		}
		if (Item.getType(id) == ItemType.PlaceableObject.get()) {
			MadSand.player.inventory.delItem(id, 1);
			MadSand.world.getCurLoc().addObject(MadSand.player.x, MadSand.player.y, MadSand.player.look,
					Item.getAltObject(id));
		}
		if (Item.getType(id) == ItemType.PlaceableTile.get()) {
			MadSand.player.inventory.delItem(id, 1);
			MadSand.world.getCurLoc().addTile(MadSand.player.x, MadSand.player.y, MadSand.player.look,
					Item.getAltObject(id));
		}

	}
}