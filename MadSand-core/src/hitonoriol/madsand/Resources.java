package hitonoriol.madsand;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import org.w3c.dom.Document;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.fasterxml.jackson.databind.type.MapType;

import hitonoriol.madsand.containers.Tuple;
import hitonoriol.madsand.entities.SkillContainer;
import hitonoriol.madsand.enums.ItemType;
import hitonoriol.madsand.enums.Skill;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.properties.CropProp;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.NpcContainer;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.properties.QuestList;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.properties.Tutorial;
import hitonoriol.madsand.properties.WorldGenProp;
import hitonoriol.madsand.world.worldgen.WorldGenPreset;

public class Resources {

	static final int PLAYER_ANIM_WIDTH = 35;
	static final int PLAYER_ANIM_HEIGHT = 74;

	static Document itemDoc;

	static Document skilldoc;

	public static Texture[] item;
	static Texture[] objects;
	static Texture[] tile;
	static Texture visitedMask;
	public static Texture[] npc;

	static TextureRegion[][] tmpAnim;

	static Texture animsheet;

	static float playerAnimDuration = 0.2f;

	static TextureRegion[] animup = new TextureRegion[2];
	static TextureRegion[] animdown = new TextureRegion[2];
	static TextureRegion[] animleft = new TextureRegion[2];
	static TextureRegion[] animright = new TextureRegion[2];

	static Animation<TextureRegion> uanim;
	static Animation<TextureRegion> danim;
	static Animation<TextureRegion> lanim;
	static Animation<TextureRegion> ranim;

	static Texture placeholder;
	public static TextureRegionDrawable noEquip;

	public static Sprite playerRightSpr;
	public static Sprite playerLeftSpr;
	public static Sprite playerUpSpr;
	public static Sprite playerDownSpr;

	public static int CRAFTABLES;
	public static int LASTITEMID;
	public static int LASTOBJID;
	public static int NPCSPRITES;
	public static int LASTTILEID;
	public static int BIOMES;
	public static int QUESTS;
	public static int CROPS;
	public static int[] craftableid;

	public static final String emptyField = "-1";
	public static final int emptyId = -1;
	public static String Space = " ", Colon = ":";

	static final String XML_ITEM_NODE = "item";
	static final String XML_CROP_STAGES_NODE = "stages";
	static final String XML_RECIPE_NODE = "recipe";
	static final String XML_BIOME_NODE = "biome";

	public static void init() throws Exception {
		Utils.out("Loading resources...");

		itemDoc = XMLUtils.XMLString(GameSaver.getExternal(MadSand.ITEMSFILE));

		mapcursor = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/cur.png"));
		animsheet = new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/anim.png"));
		visitedMask = new Texture(Gdx.files.local(MadSand.SAVEDIR + "light/light_visited.png"));

		tmpAnim = TextureRegion.split(animsheet, PLAYER_ANIM_WIDTH, PLAYER_ANIM_HEIGHT);

		animdown[0] = tmpAnim[0][0];
		animdown[1] = tmpAnim[0][1];
		animleft[0] = tmpAnim[1][0];
		animleft[1] = tmpAnim[1][1];
		animright[0] = tmpAnim[2][0];
		animright[1] = tmpAnim[2][1];
		animup[0] = tmpAnim[3][0];
		animup[1] = tmpAnim[3][1];

		uanim = new Animation<TextureRegion>(playerAnimDuration, animup);
		danim = new Animation<TextureRegion>(playerAnimDuration, animdown);
		lanim = new Animation<TextureRegion>(playerAnimDuration, animleft);
		ranim = new Animation<TextureRegion>(playerAnimDuration, animright);

		Cursor mouseCursor = Gdx.graphics.newCursor(new Pixmap(Gdx.files.local(MadSand.SAVEDIR + "cursor.png")), 0, 0);
		Gdx.graphics.setCursor(mouseCursor);

		Resources.LASTITEMID = XMLUtils.countKeys(itemDoc, XML_ITEM_NODE);
		Resources.CROPS = XMLUtils.countKeys(itemDoc, XML_CROP_STAGES_NODE);

		Utils.out(CROPS + " crops");

		Resources.CRAFTABLES = XMLUtils.countKeys(itemDoc, XML_RECIPE_NODE);

		Utils.out(CRAFTABLES + " craftable items");

		Resources.craftableid = new int[Resources.CRAFTABLES];

		item = new Texture[Resources.LASTITEMID + 1];
		objects = new Texture[Resources.LASTOBJID];
		npc = new Texture[Resources.NPCSPRITES + 1];

		loadWorldGen();
		loadItems();
		loadMapObjects();
		loadMapTiles();
		loadQuests();
		loadNpcs();
		loadTutorial();
		Globals.loadGlobals();

		placeholder = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/placeholder.png"));
		noEquip = new TextureRegionDrawable(new TextureRegion(placeholder));

		playerDownSpr = new Sprite(new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/d1.png")));
		playerUpSpr = new Sprite(new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/u1.png")));
		playerRightSpr = new Sprite(new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/r1.png")));
		playerLeftSpr = new Sprite(new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/l1.png")));
		Utils.out("Done loading resources.");
	}

	private static void loadTutorial() throws Exception {
		MapType tutorialMap = MadSand.typeFactory.constructMapType(HashMap.class, String.class, String.class);
		Tutorial.strings = MadSand.mapper.readValue(new File(MadSand.TUTORIALFILE), tutorialMap);
	}

	private static void loadQuests() throws Exception {
		MapType questMap = MadSand.typeFactory.constructMapType(HashMap.class, Integer.class, Quest.class);
		QuestList.quests = MadSand.mapper.readValue(new File(MadSand.QUESTFILE), questMap);

		Utils.out(QuestList.quests.size() + " quests");

	}

	private static void loadNpcs() throws Exception {
		MapType npcMap = MadSand.typeFactory.constructMapType(HashMap.class, Integer.class, NpcContainer.class);
		NpcProp.npcs = MadSand.mapper.readValue(new File(MadSand.NPCFILE), npcMap);
		NPCSPRITES = NpcProp.npcs.size();
		Utils.out(NPCSPRITES + " NPCs");
		
		npc = new Texture[NPCSPRITES];
		
		int i = 0;
		for (Entry<Integer, NpcContainer> npcEntry : NpcProp.npcs.entrySet()) {
			i = npcEntry.getKey();
			npc[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "npc/" + i + ".png"));
		}

	}

	private static void loadMapTiles() throws Exception {
		MapType tileMap = MadSand.typeFactory.constructMapType(HashMap.class, Integer.class, Tile.class);
		TileProp.tiles = MadSand.mapper.readValue(new File(MadSand.TILEFILE), tileMap);

		LASTTILEID = TileProp.tiles.size();
		tile = new Texture[LASTTILEID + 1];
		Utils.out(LASTTILEID + " tiles");

		// Load tile textures
		int i = 0;
		for (Entry<Integer, Tile> tileEntry : TileProp.tiles.entrySet()) {
			i = tileEntry.getKey();
			tile[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "terrain/" + i + ".png"));
		}
	}

	private static void loadMapObjects() throws Exception {
		MapType objectMap = MadSand.typeFactory.constructMapType(HashMap.class, Integer.class, MapObject.class);
		ObjectProp.objects = MadSand.mapper.readValue(new File(MadSand.OBJECTFILE), objectMap);

		LASTOBJID = ObjectProp.objects.size();
		objects = new Texture[LASTOBJID + 1];
		Utils.out(LASTOBJID + " map objects");

		// Load object textures
		int i;
		for (Entry<Integer, MapObject> objectEntry : ObjectProp.objects.entrySet()) {
			i = objectEntry.getKey();
			objects[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "obj/" + i + ".png"));
		}
	}

	private static void loadItems() {
		String stgs, stglen;
		String typeStr;
		String[] cont;
		Vector<Integer> stages, slens;
		ItemType type;
		int i = 0, cc = 0;
		boolean unlockable;
		while (i < Resources.LASTITEMID) {
			item[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "inv/" + i + ".png"));
			if (!XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, XML_RECIPE_NODE).equals("-1")) {
				Resources.craftableid[cc] = i;
				cc++;
			}

			// Crops
			stgs = XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, XML_CROP_STAGES_NODE);
			if (!stgs.equals("-1")) {
				cont = stgs.split("\\,");
				stages = new Vector<Integer>();
				for (String stage : cont)
					stages.add(Integer.parseInt(stage));
				CropProp.stages.put(i, stages);
				stglen = XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, XML_CROP_STAGES_NODE);
				cont = stglen.split("\\,");
				slens = new Vector<Integer>();
				for (String slen : cont)
					slens.add(Integer.parseInt(slen));
				CropProp.stagelen.put(i, slens);
			}

			// Item properties
			ItemProp.weight.put(i, Float.parseFloat(XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "weight")));
			ItemProp.name.put(i, XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "name"));

			typeStr = XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "type");
			if (typeStr == "-1")
				typeStr = ItemType.Item.toString();
			type = ItemType.valueOf(typeStr);
			ItemProp.type.put(i, type);

			if (type.isArmor() || type.isWeapon())
				ItemProp.lvl.put(i, Utils.val(XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "lvl")));
			if (type.isWeapon())
				ItemProp.str.put(i, Utils.val(XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "strength")));

			ItemProp.altObject.put(i, Integer.parseInt(XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "altobject")));

			ItemProp.dmg.put(i, Integer.parseInt(XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "dmg", "0")));
			ItemProp.skill.put(i, Skill.valueOf(XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "skill", "None")));

			ItemProp.hp.put(i, Integer.parseInt(XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "hp")));
			ItemProp.cost.put(i, Integer.parseInt(XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "cost")));

			unlockable = Boolean.parseBoolean(XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "unlockable", "true"));
			ItemProp.unlockable.put(i, unlockable);

			ItemProp.recipe.put(i, XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, XML_RECIPE_NODE));
			ItemProp.craftQuantity.put(i,
					Utils.val(XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "craft_quantity", "1")));
			ItemProp.heal.put(i, XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "heal"));
			ItemProp.useAction.put(i, XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "onuse"));

			String reqlist = ItemProp.recipe.get(i);
			String item[];
			if (reqlist != "-1" && unlockable) {
				if (!reqlist.contains(":"))
					reqlist += ":";
				cont = reqlist.split("\\:");
				Vector<Integer> reqs = new Vector<Integer>();
				for (String req : cont) {
					if (req == "")
						break;
					item = req.split("\\/");
					reqs.add(Integer.parseInt(item[0]));
				}

				ItemProp.craftReq.put(i, reqs);
			}
			i++;
		}
	}

	private static void loadWorldGen() throws Exception {
		MapType worldGenMap = MadSand.typeFactory.constructMapType(HashMap.class, Integer.class, WorldGenPreset.class);
		WorldGenProp.biomes = MadSand.mapper.readValue(new File(MadSand.GENFILE), worldGenMap);
		BIOMES = WorldGenProp.biomes.size();

		Utils.out(BIOMES + " biomes");
	}

	static Texture mapcursor;

	public static void loadSkillReqs() {
		if (SkillContainer.reqList.size() > 0)
			return;
		skilldoc = XMLUtils.XMLString(GameSaver.getExternal(MadSand.SKILLFILE));
		int i = 0;
		int skills = XMLUtils.countKeys(skilldoc, "skill");
		Skill skill;
		String skillStr;
		int req;
		double mul;
		while (i < skills) {
			skillStr = XMLUtils.getAttr(skilldoc, "skill", Utils.str(i), "name");
			if (skillStr.equals("-1")) {
				++i;
				continue;
			}
			skill = Skill.valueOf(skillStr);
			req = Utils.val(XMLUtils.getKey(skilldoc, "skill", Utils.str(i), "required"));
			mul = Double.parseDouble(XMLUtils.getKey(skilldoc, "skill", Utils.str(i), "multiplier"));
			SkillContainer.reqList.put(skill, Tuple.makeTuple(req, mul));
			++i;
		}
	}
}
