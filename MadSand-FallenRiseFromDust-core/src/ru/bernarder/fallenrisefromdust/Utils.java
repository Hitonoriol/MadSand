package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.enums.GameState;
import ru.bernarder.fallenrisefromdust.strings.InventoryNames;
import ru.bernarder.fallenrisefromdust.strings.Objects;
import ru.bernarder.fallenrisefromdust.strings.Tiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Utils {
	public static boolean tester = true;
	public int ubound;
	public int lbound;
	public static float x = MadSand.player.x * 33;
	public static float y = MadSand.player.y * 33;
	public static Vector2 ppos;
	public static boolean invent = false;
	public static float pspeed = 33.0F;
	com.badlogic.gdx.graphics.g2d.SpriteBatch batch;
	Texture hp;
	Texture dtex;
	Texture utex;
	Texture ltex;
	Texture rtex;
	Texture inv;
	static Texture dark;
	Texture curs;
	Texture placeholder;
	static Sprite cursor;
	Sprite Splayer;
	Texture[] lgt = new Texture[7];
	static Texture[] item;
	Texture[] objects;
	Texture[] tile;
	Texture[] npc;
	static boolean admin = true;
	TextureRegion[] animdown = new TextureRegion[2];
	TextureRegion[][] tmpAnim;
	Texture animsheet;
	TextureRegion[] animup = new TextureRegion[2];
	TextureRegion[] animleft = new TextureRegion[2];
	TextureRegion[] animright = new TextureRegion[2];
	static Animation<TextureRegion> uanim;
	static Animation<TextureRegion> danim;
	static Animation<TextureRegion> lanim;
	Texture mapcursor;
	public static Random random = new Random();
	static Animation<TextureRegion> ranim;
	static int selected;
	static String RES;

	public static Document XMLString(String xml) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xml));
			return builder.parse(is);
		} catch (Exception e) {
			out("Something has fucked up (" + e.getMessage() + ")");
			e.printStackTrace(Resource.eps);
			return null;
		}
	}

	static int countKeys(String res, String list) {
		try {
			Document doc = XMLString(res);// dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(list);
			int temp, c = 0;
			for (temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					c++;
				}
			}
			return c;
		} catch (Exception e) {
			e.printStackTrace(Resource.eps);
			return -1;
		}
	}

	static String getKey(String res, String list, String id, String element) {
		try {
			Document doc = XMLString(res);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(list);
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (eElement.getAttribute("id").equals(id)) {
						return eElement.getElementsByTagName(element).item(0).getTextContent();
					}
				}
			}
			return "-1";
		} catch (Exception e) {
			return "-1";
		}
	}

	public void Initf() {
		MadSand.gameVrf = getSHA1(new File(MadSand.RESFILE));
		out("Resfile hash: " + MadSand.gameVrf);
		RES = GameSaver.getExternalNl(MadSand.RESFILE);
		MadSand.QUESTS = countKeys(RES, "quest");
		MadSand.quests = new int[MadSand.QUESTS][2];
		this.mapcursor = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/cur.png"));
		this.animsheet = new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/anim.png"));
		this.tmpAnim = TextureRegion.split(this.animsheet, 35, 74);
		this.animdown[0] = this.tmpAnim[0][0];
		this.animdown[1] = this.tmpAnim[0][1];
		this.animleft[0] = this.tmpAnim[1][0];
		this.animleft[1] = this.tmpAnim[1][1];
		this.animright[0] = this.tmpAnim[2][0];
		this.animright[1] = this.tmpAnim[2][1];
		this.animup[0] = this.tmpAnim[3][0];
		this.animup[1] = this.tmpAnim[3][1];
		uanim = new Animation<TextureRegion>(0.2F, this.animup);
		danim = new Animation<TextureRegion>(0.2F, this.animdown);
		lanim = new Animation<TextureRegion>(0.2F, this.animleft);
		ranim = new Animation<TextureRegion>(0.2F, this.animright);
		com.badlogic.gdx.graphics.Cursor customCursor = Gdx.graphics
				.newCursor(new com.badlogic.gdx.graphics.Pixmap(Gdx.files.local(MadSand.SAVEDIR + "cursor.png")), 0, 0);
		Gdx.graphics.setCursor(customCursor);
		MadSand.LASTITEMID = countKeys(RES, "item");
		MadSand.CROPS = countKeys(RES, "stages");

		out(MadSand.CROPS + " crops");
		MadSand.LASTOBJID = countKeys(RES, "object");
		out(MadSand.LASTOBJID + " objects");
		CropLayer.stages = new String[MadSand.LASTOBJID];
		MadSand.LASTTILEID = countKeys(RES, "tile");
		MadSand.NPCSPRITES = countKeys(RES, "npc");
		MadSand.CRAFTABLES = countKeys(RES, "recipe");
		out(MadSand.CRAFTABLES + " craftable items");
		out(MadSand.LASTTILEID + " tiles");
		out(MadSand.NPCSPRITES + " npcs");
		MadSand.craftableid = new int[MadSand.CRAFTABLES];
		item = new Texture[MadSand.LASTITEMID + 1];
		this.objects = new Texture[MadSand.LASTOBJID];
		this.tile = new Texture[MadSand.LASTTILEID + 1];
		this.npc = new Texture[MadSand.NPCSPRITES + 1];
		int counter = 1, cc = 0;
		while (counter <= MadSand.LASTITEMID) {
			item[counter] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "inv/" + counter + ".png"));
			if (!getKey(RES, "item", "" + counter, "recipe").equals("-1")) {
				MadSand.craftableid[cc] = counter;
				cc++;
			}
			if (!getKey(RES, "item", "" + counter, "stages").equals("-1")) {
				CropLayer.stages[counter] = getKey(RES, "item", "" + counter, "stages");
			}
			InventoryNames.name.put(counter, getKey(RES, "item", "" + counter, "name"));
			InventoryNames.type.put(counter, Integer.parseInt(getKey(RES, "item", "" + counter, "type")));
			InventoryNames.altObject.put(counter, Integer.parseInt(getKey(RES, "item", "" + counter, "altobject")));
			InventoryNames.cost.put(counter, Integer.parseInt(getKey(RES, "item", "" + counter, "cost")));
			InventoryNames.craftable.put(counter,
					Integer.parseInt(getKey(RES, "item", "" + counter, "craftable")) != 0);
			InventoryNames.recipe.put(counter, getKey(RES, "item", "" + counter, "recipe"));
			InventoryNames.heal.put(counter, getKey(RES, "item", "" + counter, "heal"));
			counter++;
		}
		counter = 0;
		Tuple<Integer, String> tmp = new Tuple<Integer, String>(0, "");
		while (counter < MadSand.LASTOBJID) {
			this.objects[counter] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "obj/" + counter + ".png"));
			Objects.name.put(counter, getKey(RES, "object", "" + counter, "name"));
			Objects.hp.put(counter, Integer.parseInt(getKey(RES, "object", "" + counter, "tough")));
			Objects.altitems.put(tmp.set(counter, "altitem"), getKey(RES, "object", "" + counter, "altitem"));
			Objects.altitems.put(tmp.set(counter, "hand"), getKey(RES, "object", "" + counter, "hand"));
			Objects.altitems.put(tmp.set(counter, "skillbonus"), getKey(RES, "object", "" + counter, "skillbonus"));
			Objects.vRendMasks.put(counter, Integer.parseInt(getKey(RES, "object", "" + counter, "vmask")));
			Objects.hRendMasks.put(counter, Integer.parseInt(getKey(RES, "object", "" + counter, "hmask")));
			counter++;
		}
		counter = 0;
		while (counter < MadSand.LASTTILEID) {
			this.tile[counter] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "terrain/" + counter + ".png"));
			Tiles.name.put(counter, getKey(RES, "tile", "" + counter, "name"));
			Tiles.damage.put(counter, Integer.parseInt(getKey(RES, "tile", "" + counter, "damage")));
			counter++;
		}
		counter = 0;
		while (counter < MadSand.NPCSPRITES) {
			this.npc[counter] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "npc/" + counter + ".png"));
			getKey(RES, "npc", "" + counter, "hp");
			getKey(RES, "npc", "" + counter, "maxhp");
			getKey(RES, "npc", "" + counter, "rewardexp");
			getKey(RES, "npc", "" + counter, "drop");
			getKey(RES, "npc", "" + counter, "name");
			getKey(RES, "npc", "" + counter, "atk");
			getKey(RES, "npc", "" + counter, "accuracy");
			getKey(RES, "npc", "" + counter, "friendly");
			getKey(RES, "npc", "" + counter, "fraction");
			getKey(RES, "npc", "" + counter, "spawnonce");
			getKey(RES, "npc", "" + counter, "qids");
			counter++;
		}

		this.hp = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/heart.png"));
		dark = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/darkness.png"));
		this.curs = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/cursor.png"));
		this.placeholder = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/placeholder.png"));
		this.inv = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/invsheet.png"));
		ppos = new Vector2(x, y);
		com.badlogic.gdx.files.FileHandle pfhandle = Gdx.files.local(MadSand.SAVEDIR + "player/d1.png");
		this.batch = new com.badlogic.gdx.graphics.g2d.SpriteBatch();
		this.dtex = new Texture(pfhandle);
		pfhandle = Gdx.files.local(MadSand.SAVEDIR + "player/u1.png");
		this.utex = new Texture(pfhandle);
		pfhandle = Gdx.files.local(MadSand.SAVEDIR + "player/r1.png");
		this.rtex = new Texture(pfhandle);
		pfhandle = Gdx.files.local(MadSand.SAVEDIR + "player/l1.png");
		this.ltex = new Texture(pfhandle);
		cursor = new Sprite(this.curs);
		this.Splayer = new Sprite(this.dtex);
	}

	public String getItem(int id) {
		return InventoryNames.name.get(id);
	}

	public static void useKeyAction() { // TODO: usekey actions using BuildScript
		int id = MadSand.player.hand;
		int ptile = MadSand.world.getTileId(MadSand.player.x, MadSand.player.y);
		MadSand.player.checkHands(id);
		if ((id == 41) && (ptile == 0)) {
			MadSand.print("You plowed a dirt");
			MadSand.world.putMapTile(MadSand.player.x, MadSand.player.y, 15);
		}
		if ((ptile == 6) || (ptile == 16)) {
			MadSand.print("You entered the dungeon.");
			MadSand.curlayer += 1;
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
		if (Item.getType(id) == 9) {
			MadSand.print("You ate one " + InventoryNames.name.get(id));
			MadSand.player.heal(Integer.parseInt(InventoryNames.heal.get(id).split(":")[0]));
			MadSand.player.increaseStamina(Integer.parseInt(InventoryNames.heal.get(id).split(":")[1]));
		}
		if ((id == 9) && (MadSand.player.inventory.getSameCell(9, 1) != -1)
				&& (MadSand.player.inventory.getSameCell(1, 5) != -1)) {
			MadSand.print("You placed a campfire");
			MadSand.player.inventory.delItem(9, 1);
			MadSand.player.inventory.delItem(1, 5);
			MadSand.world.getCurLoc().addObject(MadSand.player.x, MadSand.player.y, MadSand.player.look, 6);
		}
		if (Item.getType(id) == 4) {
			// equip helmet
		}
		if (Item.getType(id) == 5) {
			// equip chestplate
		}
		if (Item.getType(id) == 6) {
			// equip shield
		}
		if (Item.getType(id) == 3) { // crop
			MadSand.player.inventory.delItem(id, 1);
			MadSand.world.getCurLoc().addObject(MadSand.player.x, MadSand.player.y, MadSand.player.look,
					Item.getAltObject(id));
			// put crop in direction
		}
		if (Item.getType(id) == 1) {
			MadSand.player.inventory.delItem(id, 1);
			MadSand.world.getCurLoc().addObject(MadSand.player.x, MadSand.player.y, MadSand.player.look,
					Item.getAltObject(id));
		}
		if (Item.getType(id) == 2) {
			MadSand.player.inventory.delItem(id, 1);
			MadSand.world.getCurLoc().addTile(MadSand.player.x, MadSand.player.y, MadSand.player.look,
					Item.getAltObject(id));
		}

	}

	public void InvKeyCheck() {
		if (Gdx.input.isKeyJustPressed(33)) {
			if (invent) {
				funcButtonsSet(false);
				Gui.invcontext.setVisible(false);
				MadSand.contextopened = false;
				MadSand.state = GameState.GAME;
				Gui.mousemenu.setVisible(true);
				invent = false;
				MadSand.tradeflag = false;
			} else {
				Gui.gamecontext.setVisible(false);
				MadSand.contextopened = false;
				Gui.mousemenu.setVisible(false);
				funcButtonsSet(true);
				Gdx.input.setInputProcessor(Gui.overlay);
				MadSand.state = GameState.INVENTORY;
				invent = true;
			}
		}
	}

	public static void funcButtonsSet(boolean visible) {
		Gui.craftButton.setVisible(visible);
		Gui.exitButton.setVisible(visible);
	}

	public void inInvKeyCheck() {
		//TODO remove dis
		if (Gdx.input.isKeyJustPressed(66)) {
			inventoryAction();
		}
	}

	public static void inventoryAction() {
		//TODO
	}

	public void isInFront() {
		int obj = MadSand.world.getObjID(MadSand.player.x, MadSand.player.y, MadSand.player.look);
		if ((obj != 666) && (obj != 0)) {
			MadSand.print("You see: " + Objects.name.get(obj));
		}
	}

	public void KeyCheck() {
		if (Gdx.input.isKeyJustPressed(Keys.Q)) {
			Gui.showStatsWindow();
		}
		if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
			MadSand.player.interact(MadSand.player.look);
		}
		if (Gdx.input.isKeyPressed(145))
			MadSand.ZOOM = (float) (MadSand.ZOOM + 0.01D);
		if (Gdx.input.isKeyPressed(147))
			MadSand.ZOOM = (float) (MadSand.ZOOM - 0.01D);
		if (Gdx.input.isKeyPressed(148))
			MadSand.camoffset -= 2;
		if (Gdx.input.isKeyPressed(150))
			MadSand.camoffset += 2;
		if (Gdx.input.isKeyPressed(152))
			MadSand.camyoffset += 2;
		if (Gdx.input.isKeyPressed(146))
			MadSand.camyoffset -= 2;
		if (Gdx.input.isKeyPressed(149)) {
			MadSand.camyoffset = 0;
			MadSand.camoffset = 0;
		}
		if (Gdx.input.isKeyJustPressed(Keys.U)) {
			useKeyAction();
		}
		if (Gdx.input.isKeyJustPressed(Keys.Z)) {
			if (tester) {
				tester = false;
				MadSand.showMsg("Tester mode turned off!");
			} else {
				tester = true;
				MadSand.showMsg("Tester mode turned on!\nCheck the console output!");
			}
		}
		if (Gdx.input.isKeyJustPressed(Keys.UP) && (!MadSand.stepping)) {
			turn(Direction.UP);
		}
		if (Gdx.input.isKeyJustPressed(Keys.DOWN) && (!MadSand.stepping)) {
			turn(Direction.DOWN);
		}
		if (Gdx.input.isKeyJustPressed(Keys.LEFT) && (!MadSand.stepping)) {
			turn(Direction.LEFT);
		}
		if (Gdx.input.isKeyJustPressed(Keys.RIGHT) && (!MadSand.stepping)) {
			turn(Direction.RIGHT);
		}
		if (Gdx.input.isKeyJustPressed(Keys.N) && MadSand.curxwpos != 0 && MadSand.curywpos != 0) {
			if (MadSand.player.x == MadSand.MAPSIZE - 1 && MadSand.player.look == Direction.RIGHT)
				gotoSector("right");
			if (MadSand.player.y == MadSand.MAPSIZE - 1 && MadSand.player.look == Direction.UP)
				gotoSector("up");
			if (MadSand.player.x == MadSand.BORDER && MadSand.player.look == Direction.LEFT)
				gotoSector("left");
			if (MadSand.player.y == MadSand.BORDER && MadSand.player.look == Direction.DOWN)
				gotoSector("down");

		}

		if ((Gdx.input.isKeyJustPressed(Keys.Y)) && (tester)) {
			MadSand.teleport(MadSand.wmx, MadSand.wmy);
		}
		if ((Gdx.input.isKeyPressed(129)) && (Gdx.input.isKeyPressed(46)) && (tester)) {
			MadSand.world.Generate();
		}
		if ((Gdx.input.isKeyPressed(129)) && (Gdx.input.isKeyPressed(20)) && (tester)) {
			MadSand.curlayer = 1;
		}
		if ((Gdx.input.isKeyPressed(129)) && (Gdx.input.isKeyPressed(19)) && (tester)) {
			MadSand.curlayer = 0;
		}
		if (Gdx.input.isKeyJustPressed(Keys.F11)) {
			Boolean fullScreen = Boolean.valueOf(Gdx.graphics.isFullscreen());
			Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
			if (fullScreen.booleanValue()) {
				Gdx.graphics.setWindowedMode(1280, 720);
			} else
				Gdx.graphics.setFullscreenMode(currentMode);
		}
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			Gui.resumeButton.setVisible(true);
			Gdx.input.setInputProcessor(Gui.stage);
			MadSand.state = GameState.NMENU;
		}
		if ((Gdx.input.isKeyJustPressed(Keys.G)) && (tester)) {
			GameSaver saver = new GameSaver();
			saver.saver.start();
		}
		if ((Gdx.input.isKeyJustPressed(Keys.L)) && (tester)) {
			GameSaver.loadWorld(MadSand.WORLDNAME);
		}
		if ((Gdx.input.isKeyJustPressed(Keys.H)) && (tester)) {
			MadSand.player.damage(10);
		}
		if ((Gdx.input.isKeyJustPressed(Keys.F)) && (MadSand.player.hand != 0)) {
			MadSand.player.hand = 0;
			MadSand.print("You freed your hands.");
			Gui.equip[4].setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable(new Sprite(cursor)));
		}
		if ((Gdx.input.isKeyPressed(29)) && (!MadSand.stepping)) {
			MadSand.player.look = Direction.LEFT;
			turn(MadSand.player.look);
			if (!VerifyPosition(MadSand.player.look))
				move(MadSand.player.look);
			isInFront();
		}
		if ((Gdx.input.isKeyPressed(32)) && (!MadSand.stepping)) {
			MadSand.player.look = Direction.RIGHT;
			turn(MadSand.player.look);
			if (!VerifyPosition(MadSand.player.look))
				move(MadSand.player.look);
			isInFront();
		}
		if ((Gdx.input.isKeyPressed(51)) && (!MadSand.stepping)) {
			MadSand.player.look = Direction.UP;
			turn(MadSand.player.look);
			if (!VerifyPosition(MadSand.player.look))
				move(MadSand.player.look);
			isInFront();
		}
		if ((Gdx.input.isKeyPressed(47)) && (!MadSand.stepping)) {
			MadSand.player.look = Direction.DOWN;
			turn(MadSand.player.look);
			if (!VerifyPosition(MadSand.player.look))
				move(MadSand.player.look);
			isInFront();
		}
	}

	void mouseMovement() {
		if ((Gdx.input.isButtonPressed(0)) && (MadSand.state == GameState.GAME) && (!MadSand.stepping)
				&& (!MadSand.contextopened)) {
			if (MadSand.wmx > MadSand.player.x) {
				MadSand.player.look = Direction.RIGHT;
				turn(MadSand.player.look);
				move(MadSand.player.look);
				isInFront();
			} else if (MadSand.wmx < MadSand.player.x) {
				MadSand.player.look = Direction.LEFT;
				turn(MadSand.player.look);
				move(MadSand.player.look);
				isInFront();
			} else if (MadSand.wmy > MadSand.player.y) {
				MadSand.player.look = Direction.UP;
				turn(MadSand.player.look);
				move(MadSand.player.look);
				isInFront();
			} else if (MadSand.wmy < MadSand.player.y) {
				MadSand.player.look = Direction.DOWN;
				turn(MadSand.player.look);
				move(MadSand.player.look);
				isInFront();
			}
		}
	}

	public void turn(Direction dir) {
		MadSand.player.look = dir;
		if (!MadSand.stepping) {
			if (dir == Direction.UP) {
				this.Splayer = new Sprite(this.utex);
			}
			if (dir == Direction.DOWN) {
				this.Splayer = new Sprite(this.dtex);
			}
			if (dir == Direction.LEFT) {
				this.Splayer = new Sprite(this.ltex);
			}
			if (dir == Direction.RIGHT) {
				this.Splayer = new Sprite(this.rtex);
			}
		}
	}

	public static int rand(int min, int max) {
		return Utils.random.nextInt((max - min) + 1) + min;
	}

	public static void passHour() {
		MadSand.worldtime += 1;
		if (((MadSand.worldtime >= 0) && (MadSand.worldtime <= 5))
				|| ((MadSand.worldtime >= 21) && (MadSand.worldtime <= 23))) {
			// spawn mobs
		}
	}

	public static void makeTurn() {
		tileDmg();
		passHour();
		CropLayer.updCrops();
		if (MadSand.worldtime == 24)
			MadSand.worldtime = 0;
		if (((MadSand.worldtime >= 0) && (MadSand.worldtime <= 5))
				|| ((MadSand.worldtime >= 21) && (MadSand.worldtime <= 23))) {
			Gui.darkness.setVisible(true);
		} else {
			Gui.darkness.setVisible(false);
		}
	}

	public static void tileDmg() {
		int tid = MadSand.world.getTileId(MadSand.player.x, MadSand.player.y);
		int dmg = Tiles.damage.get(tid);
		if (dmg > 0) {
			MadSand.print("You took " + dmg + " damage from " + (Tiles.name.get(tid)));
			MadSand.player.damage(dmg);
		}
	}

	public void move(Direction dir) {
		if ((!MadSand.player.isCollision(dir, 0)) && (MadSand.dialogflag)) {
			if ((dir == Direction.UP) && (!VerifyPosition(dir))) {
				MadSand.player.y += 1;
				ppos.y += 33.0F;
			}
			if ((dir == Direction.DOWN) && (!VerifyPosition(dir))) {
				MadSand.player.y -= 1;
				ppos.y -= 33.0F;
			}
			if ((dir == Direction.LEFT) && (!VerifyPosition(dir))) {
				MadSand.player.x -= 1;
				ppos.x -= 33.0F;
			}
			if ((dir == Direction.RIGHT) && (!VerifyPosition(dir))) {
				MadSand.player.x += 1;
				ppos.x += 33.0F;
			}
			if (MadSand.player.x == MadSand.MAPSIZE - 1 || MadSand.player.y == MadSand.MAPSIZE - 1
					|| MadSand.player.x == MadSand.BORDER || MadSand.player.y == MadSand.BORDER) {
				MadSand.print("Press [GRAY]N[WHITE] to move to the next sector.");
			}
			MadSand.stepping = true;
		}
	}

	public boolean VerifyPosition(Direction dir) {
		boolean ret = false;
		if (MadSand.player.x >= MadSand.MAPSIZE - 1 && (dir == Direction.RIGHT)) {
			ret = true;
		}
		if (MadSand.player.y >= MadSand.MAPSIZE - 1 && (dir == Direction.UP)) {
			ret = true;
		}
		if (MadSand.player.x <= 1 && (dir == Direction.LEFT)) {
			ret = true;
		}
		if (MadSand.player.y <= 1 && (dir == Direction.DOWN)) {
			ret = true;
		}
		return ret;
	}

	static void updCoords() {
		ppos.x = (MadSand.player.x * 33);
		ppos.y = (MadSand.player.y * 33);
	}

	public static String gotodir = "";

	public void gotoSector(String dir) {
		gotodir = dir;
		MadSand.state = GameState.GOT;
		new ThreadedUtils().gotoSector.start();
	}

	public static void updMouseCoords() {
		MadSand.mx = Gdx.input.getX();
		MadSand.my = Gdx.graphics.getHeight() - Gdx.input.getY();
		MadSand.wmx = (int) Math.floor(MadSand.mouseinworld.x / 33.0F);
		MadSand.wmy = (int) Math.floor(MadSand.mouseinworld.y / 33.0F);
		Gui.mousemenu.addAction(
				com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo(MadSand.mx + 60, MadSand.my - 70, 0.1F));
		try {
			Gui.mouselabel[0].setText("World coords: " + MadSand.wmx + ", " + MadSand.wmy);
			Gui.mouselabel[1]
					.setText("Tile: " + Tiles.name.get(MadSand.world.getCurLoc().getTile(MadSand.wmx, MadSand.wmy).id));
			Gui.mouselabel[2].setText("Object: " + " ()");
			Gui.mouselabel[3].setText("Creature: " + " ()");
			Gui.mouselabel[4].setText("Turn: " + MadSand.turn + "\nWorld time: " + MadSand.worldtime
					+ "\nPlayer position: (" + MadSand.player.x + ", " + MadSand.player.y + ")\nStamina: "
					+ Math.round(MadSand.player.stamina));
		} catch (Exception e) {
			e.printStackTrace(Resource.eps);
		}
	}

	public static void out(String arg) {
		if (tester) {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			System.out.print("[" + sdf.format(cal.getTime()) + "] " + arg + "\n");
		}
	}

	public static void outnonl(String arg) {
		if (tester) {
			System.out.print(arg);
		}
	}

	public boolean VerInvPos(String dir) {
		boolean ret = false;
		int b = 480;
		if ((MadSand.cx >= 290) && (dir == "right")) {
			ret = true;
		}
		if ((MadSand.cy > b - 80) && (dir == "up")) {
			ret = true;
		}
		if ((MadSand.cx <= 80) && (dir == "left")) {
			ret = true;
		}
		if ((MadSand.cy < 80) && (dir == "down")) {
			ret = true;
		}
		return ret;
	}

	static String getSHA1(File file) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			FileInputStream fis = new FileInputStream(file);
			byte[] byteArray = new byte[1024];
			int bytesCount = 0;
			while ((bytesCount = fis.read(byteArray)) != -1) {
				digest.update(byteArray, 0, bytesCount);
			}
			;
			fis.close();
			byte[] bytes = digest.digest();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace(Resource.eps);
			return "";
		}
	}

	public void checkMsgKeys() {
		if (Gdx.input.isKeyJustPressed(66)) {
			MadSand.state = GameState.GAME;
		}
	}

	public void checkFocus() {
		if (Gdx.input.isKeyJustPressed(48)) {
			Gui.overlay.setKeyboardFocus(Gui.msgf);
		}

		if ((Gdx.input.isKeyJustPressed(66)) && (Gui.overlay.getKeyboardFocus() == Gui.msgf)
				&& (!Gui.msgf.getText().trim().equals(""))) {
			if ((admin) || (tester)) {
				try {
					if (Gui.msgf.getText().split(" ")[0].equals("give")) {
						MadSand.player.inventory.putItem(new Integer(Gui.msgf.getText().split(" ")[1]).intValue(),
								new Integer(Gui.msgf.getText().split(" ")[2]).intValue(), true);
						MadSand.print("Obtained " + Gui.msgf.getText().split(" ")[2] + " "
								+ InventoryNames.name.get(new Integer(Gui.msgf.getText().split(" ")[1])));
					} else if (Gui.msgf.getText().equalsIgnoreCase("erase")) {
						MadSand.world.clearCurLoc();
					} else if (Gui.msgf.getText().split(" ")[0].equals("exec")) {
						BuildScript
								.execute(GameSaver.getExternalNl("MadSand_Saves" + Gui.msgf.getText().split(" ")[1]));
					} else if (Gui.msgf.getText().split(" ")[0].equals("spawn")) {
						MadSand.print("Spawned mob");
					} else if (Gui.msgf.getText().split(" ")[0].equals("despawn")) {
						MadSand.print("Despawned mob");

					} else if (Gui.msgf.getText().split(" ")[0].equals("help")) {
						MadSand.print(
								"give <id> <quantity>\nerase\nexec <scriptname>\nspawn <mobid> <x> <y>\ndespawn <x> <y>");
					} else {
						MadSand.print("[GREY]No such command! Type 'help' for command list.[WHITE]");
					}
				} catch (Exception e) {
					MadSand.print("Syntax error");
				}
				Gui.msgf.setText("");
				Gui.overlay.unfocus(Gui.msgf);
			}
		}
	}
}
