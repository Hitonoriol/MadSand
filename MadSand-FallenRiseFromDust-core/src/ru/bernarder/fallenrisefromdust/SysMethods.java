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

import ru.bernarder.fallenrisefromdust.strings.InventoryNames;
import ru.bernarder.fallenrisefromdust.strings.Objects;
import ru.bernarder.fallenrisefromdust.strings.Tiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import values.PlayerStats;

public class SysMethods {
	public static boolean tester = true;
	static DateTimeFormatter dtf;
	static LocalDateTime now;
	public int ubound;
	public int lbound;
	public static float x = MadSand.x * 33;
	public static float y = MadSand.y * 33;
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
		ObjLayer.ObjToug = new int[MadSand.LASTOBJID];
		ObjLayer.vRendMasks = new int[MadSand.LASTOBJID];
		ObjLayer.hRendMasks = new int[MadSand.LASTOBJID];
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
		counter = 1;
		ObjLayer.altitems = new String[MadSand.LASTOBJID][3];
		while (counter < MadSand.LASTOBJID) {
			this.objects[counter] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "obj/" + counter + ".png"));
			Objects.name.put(counter, getKey(RES, "object", "" + counter, "name"));
			Objects.hp.put(counter, Integer.parseInt(getKey(RES, "object", "" + counter, "tough")));
			ObjLayer.altitems[counter][0] = getKey(RES, "object", "" + counter, "altitem");
			ObjLayer.altitems[counter][1] = getKey(RES, "object", "" + counter, "hand");
			ObjLayer.altitems[counter][2] = getKey(RES, "object", "" + counter, "skillbonus");
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
		MobLayer.start();
		while (counter < MadSand.NPCSPRITES) {
			this.npc[counter] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "npc/" + counter + ".png"));
			MobLayer.mobStats[counter][0] = getKey(RES, "npc", "" + counter, "hp");
			MobLayer.mobStats[counter][1] = getKey(RES, "npc", "" + counter, "maxhp");
			MobLayer.mobStats[counter][2] = getKey(RES, "npc", "" + counter, "rewardexp");
			MobLayer.mobStats[counter][3] = getKey(RES, "npc", "" + counter, "drop");
			MobLayer.mobStats[counter][4] = getKey(RES, "npc", "" + counter, "name");
			MobLayer.mobStats[counter][5] = counter + "";
			MobLayer.mobStats[counter][6] = getKey(RES, "npc", "" + counter, "atk");
			MobLayer.mobStats[counter][7] = getKey(RES, "npc", "" + counter, "accuracy");
			MobLayer.mobStats[counter][8] = getKey(RES, "npc", "" + counter, "friendly");
			MobLayer.mobStats[counter][9] = getKey(RES, "npc", "" + counter, "fraction");
			MobLayer.mobStats[counter][10] = getKey(RES, "npc", "" + counter, "spawnonce");
			MobLayer.mobStats[counter][11] = getKey(RES, "npc", "" + counter, "qids");
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

	public void tradeCheckKeys() {
		if (Gdx.input.isKeyJustPressed(33)) {
			Gui.gamecontext.setVisible(false);
			MadSand.contextopened = false;
			InvUtils.emptyInvT();
			invent = false;
			MadSand.state = "GAME";
			funcButtonsSet(false);
			return;
		}
		if (Gdx.input.isKeyJustPressed(66)) {
			buyAction();
		}
	}

	public static void buyAction() {
		selected = InvUtils.getItemIdByCursorCoordT();
		if (selected != 0) {
			MadSand.showDialog(2, "You are about to buy " + InvUtils.getItemName(selected) + " for "
					+ InvUtils.getItemPriceByCursorCoord(selected) + " gold.", 0);
			Gui.acceptD.addListener(new ChangeListener() {
				public void changed(ChangeListener.ChangeEvent event, Actor actor) {
					if (InvUtils.getSameCell(21, InvUtils.getItemPriceByCursorCoord(SysMethods.selected)) != -1) {
						InvUtils.delItem(21, InvUtils.getItemPriceByCursorCoord(SysMethods.selected));
						InvUtils.putItem(SysMethods.selected, 1, false);
						InvUtils.delItemT(SysMethods.selected, 1);
						LootLayer.tInvToLoot(ObjLayer.mx, ObjLayer.my);
					}
					SysMethods.selected = 0;
					MadSand.maindialog.setVisible(false);
					MadSand.state = "BUY";
				}

			});
			Gui.refuseD.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
				public void changed(ChangeListener.ChangeEvent event, Actor actor) {
					SysMethods.selected = 0;
					MadSand.maindialog.setVisible(false);
					MadSand.state = "BUY";
				}
			});
		}
	}

	static void putTile(int x, int y, int id, int layer) {
		WorldGen.world[x][y][layer] = id;
	}

	public static void useKeyAction() {
		int id = PlayerStats.hand;
		InvUtils.checkHands(id);
		if ((id == 41) && (gettileId(MadSand.x, MadSand.y) == 0)) {
			MadSand.print("You plowed a dirt");
			putTile(MadSand.y, MadSand.x, 15, 0);
		}
		if ((gettileId(MadSand.x, MadSand.y) == 6) || (gettileId(MadSand.x, MadSand.y) == 16)) {
			MadSand.print("You entered the dungeon.");
			MadSand.curlayer += 1;
			ObjLayer.delObjectL(MadSand.x, MadSand.y, MadSand.curlayer);
			WorldGen.world[MadSand.x][MadSand.y][MadSand.curlayer] = 7;
		}
		if (id == 6) {
			if (gettileId(MadSand.x, MadSand.y) == 0) {
				putTile(MadSand.y, MadSand.x, 6, 0);
				MadSand.print("You dug a hole.");
			}
			if (gettileId(MadSand.x, MadSand.y) == 3) {
				putTile(MadSand.y, MadSand.x, 16, 0);
				MadSand.print("You dug a hole.");
			}
			if (gettileId(MadSand.x, MadSand.y) == 1) {
				InvUtils.putItem(5, 1, true);
				putTile(MadSand.y, MadSand.x, 0, 0);
				MadSand.print("You dug some clay");
			}
			if (gettileId(MadSand.x, MadSand.y) == 2) {
				InvUtils.putItem(9, 1, true);
				putTile(MadSand.y, MadSand.x, 0, 0);
				MadSand.print("You dug some flint");
			}
		}
		if (InvUtils.getType(id) == 9) {
			MadSand.print("You ate one " + InventoryNames.name.get(id));
			PlayerActions.healPlayer(Integer.parseInt(InventoryNames.heal.get(id).split(":")[0]));
			PlayerActions.increaseStamina(Integer.parseInt(InventoryNames.heal.get(id).split(":")[1]));
			PlayerActions.survivalUp();
		}
		if ((id == 9) && (InvUtils.getSameCell(9, 1) != -1) && (InvUtils.getSameCell(1, 5) != -1)) {
			MadSand.print("You placed a campfire");
			InvUtils.delItem(9, 1);
			InvUtils.delItem(1, 5);
			ObjLayer.placeObjInDirection(MadSand.x, MadSand.y, 6, MadSand.look);
			PlayerActions.survivalUp();
		}
		if (InvUtils.getType(id) == 4) {
			PlayerActions.equipHelmet(id, true);
		}
		if (InvUtils.getType(id) == 5) {
			PlayerActions.equipChestplate(id, true);
		}
		if (InvUtils.getType(id) == 6) {
			PlayerActions.equipShield(id, true);
		}
		if (InvUtils.getType(id) == 3) {
			InvUtils.delItem(id, 1);
			ObjLayer.placeObjInDirection(MadSand.x, MadSand.y, InvUtils.getAltObject(id), MadSand.look);
			CropLayer.putCropInDir(MadSand.x, MadSand.y, id, MadSand.look);
		}
		if (InvUtils.getType(id) == 1) {
			InvUtils.delItem(id, 1);
			ObjLayer.placeObjInDirection(MadSand.x, MadSand.y, InvUtils.getAltObject(id), MadSand.look);
		}
		if (InvUtils.getType(id) == 2) {
			InvUtils.delItem(id, 1);
			ObjLayer.putTileInDir(MadSand.x, MadSand.y, MadSand.look, InvUtils.getAltObject(id));
		}

		new ThreadedUtils().mapSendK.start();
	}

	public void InvKeyCheck() {
		if (Gdx.input.isKeyJustPressed(33)) {
			if (invent) {
				funcButtonsSet(false);
				Gui.invcontext.setVisible(false);
				MadSand.contextopened = false;
				MadSand.state = "GAME";
				Gui.mousemenu.setVisible(true);
				invent = false;
				MadSand.tradeflag = false;
			} else {
				Gui.gamecontext.setVisible(false);
				MadSand.contextopened = false;
				Gui.mousemenu.setVisible(false);
				funcButtonsSet(true);
				Gdx.input.setInputProcessor(Gui.overlay);
				MadSand.state = "INVENTORY";
				invent = true;
			}
		}
	}

	public static void funcButtonsSet(boolean visible) {
		Gui.craftButton.setVisible(visible);
		Gui.exitButton.setVisible(visible);
	}

	public void inInvKeyCheck() {
		if ((Gdx.input.isKeyJustPressed(46)) && (InvUtils.getItemIdByCursorCoord() > 0)) {
			InvUtils.dropItem(InvUtils.getItemIdByCursorCoord(), 1);
			MadSand.print("You've just dropped 1 " + InvUtils.getItemName(InvUtils.getItemIdByCursorCoord()));
		}
		if ((Gdx.input.isKeyJustPressed(45)) && (tester)) {
			WorldGen.makeEmpty();
		}

		if (Gdx.input.isKeyJustPressed(66)) {
			inventoryAction();
		}
	}

	public static void inventoryAction() {
		int selected = InvUtils.getItemIdByCursorCoord();
		if (selected != 0) {
			if (!MadSand.tradeflag) {
				PlayerStats.hand = selected;
				MadSand.print("You took " + InvUtils.getItemName(selected) + " in your hand");
				Gui.equip[4].setDrawable(
						new com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable(new Sprite(item[selected])));
				invent = false;
				MadSand.state = "GAME";
				Gui.mousemenu.setVisible(true);
				Gui.invcontext.setVisible(false);
				funcButtonsSet(false);
			} else {
				int prc = InvUtils.getItemPriceByCursorCoord(selected) / 2;
				MadSand.print("You sold " + InvUtils.getItemName(selected) + " for " + prc + " gold");
				InvUtils.putItem(21, prc, true);
				InvUtils.delItem(selected, 1);
			}
		}
	}

	public void isInFront() {
		if ((ObjLayer.getBlock(MadSand.x, MadSand.y, MadSand.look, MadSand.curlayer) != 666)
				&& (ObjLayer.getBlock(MadSand.x, MadSand.y, MadSand.look, MadSand.curlayer) != 0)) {
			MadSand.print("You see: " + ObjLayer.getObjName(MadSand.x, MadSand.y, MadSand.look));
		}
	}

	public void KeyCheck() {
		if (Gdx.input.isKeyJustPressed(Keys.Q)) {
			Gui.showStatsWindow();
		}
		if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
			ObjLayer.interact(MadSand.x, MadSand.y, MadSand.look);
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
			turn("up");
			String dir = MadSand.look;
			if (MobLayer.isMobCollision(dir)) {
				if (dir == "up")
					MobLayer.fight(MadSand.x, MadSand.y + 1);
				if (dir == "down")
					MobLayer.fight(MadSand.x, MadSand.y - 1);
				if (dir == "left")
					MobLayer.fight(MadSand.x - 1, MadSand.y);
				if (dir == "right")
					MobLayer.fight(MadSand.x + 1, MadSand.y);
			}
		}
		if (Gdx.input.isKeyJustPressed(Keys.DOWN) && (!MadSand.stepping)) {
			turn("down");
			String dir = MadSand.look;
			if (MobLayer.isMobCollision(dir)) {
				if (dir == "up")
					MobLayer.fight(MadSand.x, MadSand.y + 1);
				if (dir == "down")
					MobLayer.fight(MadSand.x, MadSand.y - 1);
				if (dir == "left")
					MobLayer.fight(MadSand.x - 1, MadSand.y);
				if (dir == "right")
					MobLayer.fight(MadSand.x + 1, MadSand.y);
			}
		}
		if (Gdx.input.isKeyJustPressed(Keys.LEFT) && (!MadSand.stepping)) {
			turn("left");
			String dir = MadSand.look;
			if (MobLayer.isMobCollision(dir)) {
				if (dir == "up")
					MobLayer.fight(MadSand.x, MadSand.y + 1);
				if (dir == "down")
					MobLayer.fight(MadSand.x, MadSand.y - 1);
				if (dir == "left")
					MobLayer.fight(MadSand.x - 1, MadSand.y);
				if (dir == "right")
					MobLayer.fight(MadSand.x + 1, MadSand.y);
			}
		}
		if (Gdx.input.isKeyJustPressed(Keys.RIGHT) && (!MadSand.stepping)) {
			turn("right");
			String dir = MadSand.look;
			if (MobLayer.isMobCollision(dir)) {
				if (dir == "up")
					MobLayer.fight(MadSand.x, MadSand.y + 1);
				if (dir == "down")
					MobLayer.fight(MadSand.x, MadSand.y - 1);
				if (dir == "left")
					MobLayer.fight(MadSand.x - 1, MadSand.y);
				if (dir == "right")
					MobLayer.fight(MadSand.x + 1, MadSand.y);
			}
		}
		if (Gdx.input.isKeyJustPressed(Keys.N) && MadSand.curxwpos != 0 && MadSand.curywpos != 0) {
			if (MadSand.x == MadSand.MAPSIZE - 1 && MadSand.look.equals("right"))
				gotoSector("right");
			if (MadSand.y == MadSand.MAPSIZE - 1 && MadSand.look.equals("up"))
				gotoSector("up");
			if (MadSand.x == MadSand.BORDER && MadSand.look.equals("left"))
				gotoSector("left");
			if (MadSand.y == MadSand.BORDER && MadSand.look.equals("down"))
				gotoSector("down");

		}

		if ((Gdx.input.isKeyJustPressed(Keys.Y)) && (tester)) {
			MadSand.teleport(MadSand.wmx, MadSand.wmy);
		}
		if ((Gdx.input.isKeyPressed(129)) && (Gdx.input.isKeyPressed(46)) && (tester)) {
			WorldGen.Generate(true);
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
			MadSand.state = "NMENU";
		}
		if ((Gdx.input.isKeyJustPressed(Keys.G)) && (tester)) {
			GameSaver saver = new GameSaver();
			saver.saver.start();
		}
		if ((Gdx.input.isKeyJustPressed(Keys.L)) && (tester)) {
			GameSaver.loadWorld(MadSand.WORLDNAME);
		}
		if ((Gdx.input.isKeyJustPressed(Keys.H)) && (tester)) {
			PlayerActions.damagePlayer(10);
		}
		if ((Gdx.input.isKeyJustPressed(Keys.F)) && (PlayerStats.hand != 0)) {
			PlayerStats.hand = 0;
			MadSand.print("You freed your hands.");
			Gui.equip[4].setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable(new Sprite(cursor)));
		}
		if ((Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) && (Gdx.input.isKeyPressed(29)) && (PlayerStats.stamina > 0.0F)) {
			MadSand.movespeed = MadSand.runspeed;
			PlayerStats.stamina = (float) (PlayerStats.stamina - 0.1D);
		} else if ((Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) && (Gdx.input.isKeyPressed(47))
				&& (PlayerStats.stamina > 0.0F)) {
			MadSand.movespeed = MadSand.runspeed;
			PlayerStats.stamina = (float) (PlayerStats.stamina - 0.1D);
		} else if ((Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) && (Gdx.input.isKeyPressed(32))
				&& (PlayerStats.stamina > 0.0F)) {
			MadSand.movespeed = MadSand.runspeed;
			PlayerStats.stamina = (float) (PlayerStats.stamina - 0.1D);
		} else if ((Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) && (Gdx.input.isKeyPressed(51))
				&& (PlayerStats.stamina > 0.0F)) {
			MadSand.movespeed = MadSand.runspeed;
			PlayerStats.stamina = (float) (PlayerStats.stamina - 0.1D);
		} else {
			MadSand.movespeed = 2;
		}
		if ((Gdx.input.isKeyPressed(29)) && (!MadSand.stepping)) {
			MadSand.look = "left";
			turn(MadSand.look);
			if (!VerifyPosition(MadSand.look))
				move(MadSand.look);
			isInFront();
		}
		if ((Gdx.input.isKeyPressed(32)) && (!MadSand.stepping)) {
			MadSand.look = "right";
			turn(MadSand.look);
			if (!VerifyPosition(MadSand.look))
				move(MadSand.look);
			isInFront();
		}
		if ((Gdx.input.isKeyPressed(51)) && (!MadSand.stepping)) {
			MadSand.look = "up";
			turn(MadSand.look);
			if (!VerifyPosition(MadSand.look))
				move(MadSand.look);
			isInFront();
		}
		if ((Gdx.input.isKeyPressed(47)) && (!MadSand.stepping)) {
			MadSand.look = "down";
			turn(MadSand.look);
			if (!VerifyPosition(MadSand.look))
				move(MadSand.look);
			isInFront();
		}
	}

	void mouseMovement() {
		if ((Gdx.input.isButtonPressed(0)) && (MadSand.state == "GAME") && (!MadSand.stepping)
				&& (!MadSand.contextopened)) {
			if (MadSand.wmx > MadSand.x) {
				MadSand.look = "right";
				turn(MadSand.look);
				move(MadSand.look);
				isInFront();
			} else if (MadSand.wmx < MadSand.x) {
				MadSand.look = "left";
				turn(MadSand.look);
				move(MadSand.look);
				isInFront();
			} else if (MadSand.wmy > MadSand.y) {
				MadSand.look = "up";
				turn(MadSand.look);
				move(MadSand.look);
				isInFront();
			} else if (MadSand.wmy < MadSand.y) {
				MadSand.look = "down";
				turn(MadSand.look);
				move(MadSand.look);
				isInFront();
			}
		}
	}

	public void turn(String dir) {
		MadSand.look = dir;
		if (!MadSand.stepping) {
			if (dir == "up") {
				this.Splayer = new Sprite(this.utex);
			}
			if (dir == "down") {
				this.Splayer = new Sprite(this.dtex);
			}
			if (dir == "left") {
				this.Splayer = new Sprite(this.ltex);
			}
			if (dir == "right") {
				this.Splayer = new Sprite(this.rtex);
			}
		}
	}

	public static void passHour() {
		MadSand.worldtime += 1;
		if (((MadSand.worldtime >= 0) && (MadSand.worldtime <= 5))
				|| ((MadSand.worldtime >= 21) && (MadSand.worldtime <= 23))) {
			if (WorldGen.rand(0, 5) == WorldGen.rand(0, 5))
				MobLayer.placeMob(WorldGen.random.nextInt(MadSand.MAPSIZE), WorldGen.random.nextInt(MadSand.MAPSIZE),
						WorldGen.random.nextInt(MadSand.NPCSPRITES) + "");
		}
	}

	public static void makeTurn() {
		tileDmg();
		passHour();
		PlayerActions.regen();
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
		int dmg = Tiles.damage.get(WorldGen.world[MadSand.y][MadSand.x][MadSand.curlayer]);
		if (dmg > 0) {
			MadSand.print("You took " + dmg + " damage from "
					+ (Tiles.name.get(WorldGen.world[MadSand.y][MadSand.x][MadSand.curlayer])));
			PlayerActions.damagePlayer(dmg);
		}
	}

	public void move(String dir) {
		if ((!ObjLayer.isCollision(MadSand.x, MadSand.y, dir, 0)) && (!MobLayer.isMobCollision(MadSand.look))
				&& (!PlayerLayer.isCollision(MadSand.x, MadSand.y, dir)) && (MadSand.dialogflag)) {
			MobLayer.updateMobLogic();
			if ((dir == "up") && (!VerifyPosition(dir)) && (!MobLayer.isMobCollision(MadSand.look))) {
				MadSand.y += 1;
				ppos.y += 33.0F;
			}
			if ((dir == "down") && (!VerifyPosition(dir)) && (!MobLayer.isMobCollision(MadSand.look))) {
				MadSand.y -= 1;
				ppos.y -= 33.0F;
			}
			if ((dir == "left") && (!VerifyPosition(dir)) && (!MobLayer.isMobCollision(MadSand.look))) {
				MadSand.x -= 1;
				ppos.x -= 33.0F;
			}
			if ((dir == "right") && (!VerifyPosition(dir)) && (!MobLayer.isMobCollision(MadSand.look))) {
				MadSand.x += 1;
				ppos.x += 33.0F;
			}
			if (MadSand.x == MadSand.MAPSIZE - 1 || MadSand.y == MadSand.MAPSIZE - 1 || MadSand.x == MadSand.BORDER
					|| MadSand.y == MadSand.BORDER) {
				MadSand.print("Press [GRAY]N[WHITE] to move to the next sector.");
			}
			MadSand.stepping = true;
		}
	}

	public boolean VerifyPosition(String dir) {
		boolean ret = false;
		if (MadSand.x >= MadSand.MAPSIZE - 1 && (dir == "right")) {
			ret = true;
		}
		if (MadSand.y >= MadSand.MAPSIZE - 1 && (dir == "up")) {
			ret = true;
		}
		if (MadSand.x <= 1 && (dir == "left")) {
			ret = true;
		}
		if (MadSand.y <= 1 && (dir == "down")) {
			ret = true;
		}
		return ret;
	}

	static void updCoords() {
		ppos.x = (MadSand.x * 33);
		ppos.y = (MadSand.y * 33);
	}

	public static String gotodir = "";

	public void gotoSector(String dir) {
		gotodir = dir;
		MadSand.state = "GOTO";
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
			Gui.mouselabel[1].setText("Tile: " + Tiles.name.get(gettileId(MadSand.wmx, MadSand.wmy)));
			Gui.mouselabel[2].setText(
					"Object: " + Objects.name.get(ObjLayer.getBlock(MadSand.wmx, MadSand.wmy, MadSand.curlayer)) + " ("
							+ ObjLayer.getBlock(MadSand.wmx, MadSand.wmy, MadSand.curlayer) + ")");
			Gui.mouselabel[3].setText("Creature: " + MobLayer.getMobStat(MadSand.wmx, MadSand.wmy, 4) + " ("
					+ MobLayer.getMobStat(MadSand.wmx, MadSand.wmy, 5) + " "
					+ MobLayer.getMobStat(MadSand.wmx, MadSand.wmy, 8) + ")");
			Gui.mouselabel[4].setText("Turn: " + MadSand.turn + "\nWorld time: " + MadSand.worldtime + "\nMob count: "
					+ MobLayer.mobcount + "\nPlayer position: (" + MadSand.x + ", " + MadSand.y + ")\nStamina: "
					+ Math.round(PlayerStats.stamina));
		} catch (Exception e) {
			e.printStackTrace(Resource.eps);
		}
	}

	public static int gettileId(int x, int y) {
		if (x < MadSand.MAPSIZE && y < MadSand.MAPSIZE && x >= 0 && y >= 0)
			return WorldGen.world[y][x][MadSand.curlayer];
		else
			return 0;
	}

	public void checkInvKeys() {
		MadSand.mx = Gdx.input.getX();
		MadSand.my = Gdx.graphics.getHeight() - Gdx.input.getY();
		MadSand.cx = (int) InvUtils
				.getItemCellCoord(InvUtils.getItemCell((int) MadSand.mouseinworld.x, (int) MadSand.mouseinworld.y)).x;
		MadSand.cy = (int) InvUtils
				.getItemCellCoord(InvUtils.getItemCell((int) MadSand.mouseinworld.x, (int) MadSand.mouseinworld.y)).y;
	}

	public static void out(String arg) {
		if (tester) {
			dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
			now = LocalDateTime.now();
			System.out.print("[" + dtf.format(now) + "] " + arg + "\n");
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
			MadSand.state = "GAME";
		}
	}

	public void checkFocus() {
		if (Gdx.input.isKeyJustPressed(48)) {
			Gui.overlay.setKeyboardFocus(Gui.msgf);
		}

		if ((Gdx.input.isKeyJustPressed(66)) && (Gui.overlay.getKeyboardFocus() == Gui.msgf)
				&& (!Gui.msgf.getText().trim().equals(""))) {
			if (MadSand.multiplayer) {
				try {
					MadSand.out.writeUTF("sendmsg");
					MadSand.out.writeUTF(Gui.msgf.getText());
					MadSand.out.flush();
					Gui.msgf.setText("");
					Gui.overlay.unfocus(Gui.msgf);
				} catch (IOException e) {
					e.printStackTrace(Resource.eps);
				}
			}
			if ((admin) || (tester)) {
				try {
					if (Gui.msgf.getText().split(" ")[0].equals("give")) {
						InvUtils.putItem(new Integer(Gui.msgf.getText().split(" ")[1]).intValue(),
								new Integer(Gui.msgf.getText().split(" ")[2]).intValue(), true);
						MadSand.print("Obtained " + Gui.msgf.getText().split(" ")[2] + " "
								+ InventoryNames.name.get(new Integer(Gui.msgf.getText().split(" ")[1])));
					} else if (Gui.msgf.getText().equalsIgnoreCase("erase")) {
						WorldGen.makeEmpty();
					} else if (Gui.msgf.getText().split(" ")[0].equals("exec")) {
						BuildScript
								.execute(GameSaver.getExternalNl("MadSand_Saves" + Gui.msgf.getText().split(" ")[1]));
					} else if (Gui.msgf.getText().split(" ")[0].equals("spawn")) {
						MobLayer.placeMob(new Integer(Gui.msgf.getText().split(" ")[2]).intValue(),
								new Integer(Gui.msgf.getText().split(" ")[3]).intValue(),
								Gui.msgf.getText().split(" ")[1]);
						MadSand.print("Spawned mob");
					} else if (Gui.msgf.getText().split(" ")[0].equals("despawn")) {
						MobLayer.delMob(new Integer(Gui.msgf.getText().split(" ")[1]).intValue(),
								new Integer(Gui.msgf.getText().split(" ")[2]).intValue(), MadSand.curlayer);
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
