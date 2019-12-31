package ru.bernarder.fallenrisefromdust;

import java.util.HashMap;
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

import ru.bernarder.fallenrisefromdust.containers.Tuple;
import ru.bernarder.fallenrisefromdust.entities.SkillContainer;
import ru.bernarder.fallenrisefromdust.enums.Faction;
import ru.bernarder.fallenrisefromdust.enums.ItemType;
import ru.bernarder.fallenrisefromdust.enums.NpcType;
import ru.bernarder.fallenrisefromdust.enums.Skill;
import ru.bernarder.fallenrisefromdust.properties.CropProp;
import ru.bernarder.fallenrisefromdust.properties.ItemProp;
import ru.bernarder.fallenrisefromdust.properties.NpcProp;
import ru.bernarder.fallenrisefromdust.properties.ObjectProp;
import ru.bernarder.fallenrisefromdust.properties.QuestList;
import ru.bernarder.fallenrisefromdust.properties.TileProp;
import ru.bernarder.fallenrisefromdust.properties.Tutorial;
import ru.bernarder.fallenrisefromdust.properties.WorldGenProp;

public class Resources {

	static final int PLAYER_ANIM_WIDTH = 35;
	static final int PLAYER_ANIM_HEIGHT = 74;

	static Document tileDoc;
	static Document objectDoc;
	static Document npcDoc;
	static Document itemDoc;

	static Document questdoc;
	static Document gendoc;
	static Document skilldoc;
	static Document tutorialdoc;

	public static Texture[] item;
	static Texture[] objects;
	static Texture[] tile;
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

	static final String XML_QUEST_NODE = "quest";
	static final String XML_ITEM_NODE = "item";
	static final String XML_CROP_STAGES_NODE = "stages";
	static final String XML_OBJECT_NODE = "object";
	static final String XML_TILE_NODE = "tile";
	static final String XML_NPC_NODE = "npc";
	static final String XML_RECIPE_NODE = "recipe";
	static final String XML_BIOME_NODE = "biome";

	public static void init() {
		Utils.out("Loading resources...");
		tileDoc = XMLUtils.XMLString(GameSaver.getExternal(MadSand.TILEFILE));
		objectDoc = XMLUtils.XMLString(GameSaver.getExternal(MadSand.OBJECTFILE));
		npcDoc = XMLUtils.XMLString(GameSaver.getExternal(MadSand.NPCFILE));
		itemDoc = XMLUtils.XMLString(GameSaver.getExternal(MadSand.ITEMSFILE));

		questdoc = XMLUtils.XMLString(GameSaver.getExternal(MadSand.QUESTFILE));
		gendoc = XMLUtils.XMLString(GameSaver.getExternal(MadSand.GENFILE));
		tutorialdoc = XMLUtils.XMLString(GameSaver.getExternal(MadSand.TUTORIALFILE));

		mapcursor = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/cur.png"));
		animsheet = new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/anim.png"));

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

		MadSand.QUESTS = XMLUtils.countKeys(questdoc, XML_QUEST_NODE);
		Utils.out(MadSand.QUESTS + " quests");

		MadSand.LASTITEMID = XMLUtils.countKeys(itemDoc, XML_ITEM_NODE);
		MadSand.CROPS = XMLUtils.countKeys(itemDoc, XML_CROP_STAGES_NODE);

		Utils.out(MadSand.CROPS + " crops");

		MadSand.LASTOBJID = XMLUtils.countKeys(objectDoc, XML_OBJECT_NODE);

		Utils.out(MadSand.LASTOBJID + " objects");

		MadSand.LASTTILEID = XMLUtils.countKeys(tileDoc, XML_TILE_NODE);
		MadSand.NPCSPRITES = XMLUtils.countKeys(npcDoc, XML_NPC_NODE);
		MadSand.CRAFTABLES = XMLUtils.countKeys(itemDoc, XML_RECIPE_NODE);
		MadSand.BIOMES = XMLUtils.countKeys(gendoc, XML_BIOME_NODE);

		Utils.out(MadSand.BIOMES + " biomes");
		Utils.out(MadSand.CRAFTABLES + " craftable items");
		Utils.out(MadSand.LASTTILEID + " tiles");
		Utils.out(MadSand.NPCSPRITES + " npcs");

		MadSand.craftableid = new int[MadSand.CRAFTABLES];

		item = new Texture[MadSand.LASTITEMID + 1];
		objects = new Texture[MadSand.LASTOBJID];
		tile = new Texture[MadSand.LASTTILEID + 1];
		npc = new Texture[MadSand.NPCSPRITES + 1];

		loadWorldGen();
		loadItems();
		loadMapObjects();
		loadMapTiles();
		loadQuests();
		loadNpcs();
		loadTutorial();

		placeholder = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/placeholder.png"));
		noEquip = new TextureRegionDrawable(new TextureRegion(placeholder));

		playerDownSpr = new Sprite(new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/d1.png")));
		playerUpSpr = new Sprite(new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/u1.png")));
		playerRightSpr = new Sprite(new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/r1.png")));
		playerLeftSpr = new Sprite(new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/l1.png")));
		Utils.out("Done loading resources.");
	}

	private static void loadTutorial() {
		int i = 0;
		String si, name, text;
		int tips = XMLUtils.countKeys(tutorialdoc, "tip");
		while (i < tips) {
			si = Utils.str(i);
			name = XMLUtils.getKey(tutorialdoc, "tip", si, "name");
			text = XMLUtils.getKey(tutorialdoc, "tip", si, "text");
			Tutorial.strings.put(name, text);
			++i;
		}
	}

	private static void loadQuests() {
		int i = 0;
		String si;
		Quest quest;
		while (i < MadSand.QUESTS) {
			quest = new Quest(i);
			si = Utils.str(i);
			quest.startMsg = XMLUtils.getKey(questdoc, XML_QUEST_NODE, si, "start");
			quest.endMsg = XMLUtils.getKey(questdoc, XML_QUEST_NODE, si, "complete");
			quest.reqMsg = XMLUtils.getKey(questdoc, XML_QUEST_NODE, si, "requirement_str");
			quest.reqItems = XMLUtils.getKey(questdoc, XML_QUEST_NODE, si, "requirement");
			quest.giveItems = XMLUtils.getKey(questdoc, XML_QUEST_NODE, si, "give_items");
			quest.removeOnCompletion = XMLUtils.getKey(questdoc, XML_QUEST_NODE, si, "remove_on_completion");
			quest.repeatable = Boolean.parseBoolean(XMLUtils.getKey(questdoc, XML_QUEST_NODE, si, "repeatable"));
			quest.next = Utils.val(XMLUtils.getKey(questdoc, XML_QUEST_NODE, si, "next"));
			quest.rewardItems = XMLUtils.getKey(questdoc, XML_QUEST_NODE, si, "reward");
			quest.exp = Utils.val(XMLUtils.getKey(questdoc, XML_QUEST_NODE, si, "reward_exp"));

			QuestList.quests.put(i, quest);
			++i;
		}
	}

	private static void loadNpcs() {
		int i = 0;
		String si, type;
		while (i < MadSand.NPCSPRITES) {
			npc[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "npc/" + i + ".png"));
			si = Utils.str(i);
			NpcProp.hp.put(i, Utils.val(XMLUtils.getKey(npcDoc, XML_NPC_NODE, si, "hp")));
			NpcProp.maxhp.put(i, Utils.val(XMLUtils.getKey(npcDoc, XML_NPC_NODE, si, "maxhp")));
			NpcProp.rewardexp.put(i, Utils.val(XMLUtils.getKey(npcDoc, XML_NPC_NODE, si, "rewardexp")));
			NpcProp.drop.put(i, (XMLUtils.getKey(npcDoc, XML_NPC_NODE, si, "drop")));
			NpcProp.atk.put(i, Utils.val(XMLUtils.getKey(npcDoc, XML_NPC_NODE, si, "atk")));
			NpcProp.accuracy.put(i, Utils.val(XMLUtils.getKey(npcDoc, XML_NPC_NODE, si, "accuracy")));
			NpcProp.faction.put(i, Faction.valueOf(XMLUtils.getKey(npcDoc, XML_NPC_NODE, si, "faction")));
			type = XMLUtils.getKey(npcDoc, XML_NPC_NODE, si, "type");
			if (type == "-1")
				type = NpcType.Regular.toString();
			NpcProp.type.put(i, NpcType.valueOf(type));
			NpcProp.qids.put(i, (XMLUtils.getKey(npcDoc, XML_NPC_NODE, si, "qids")));
			NpcProp.name.put(i, (XMLUtils.getKey(npcDoc, XML_NPC_NODE, si, "name")));
			NpcProp.spawnonce.put(i, Boolean.parseBoolean(XMLUtils.getKey(npcDoc, XML_NPC_NODE, si, "spawnonce")));
			NpcProp.friendly.put(i, Boolean.parseBoolean(XMLUtils.getKey(npcDoc, XML_NPC_NODE, si, "friendly")));
			i++;
		}
	}

	private static void loadMapTiles() {
		int i = 0;
		while (i < MadSand.LASTTILEID) {
			tile[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "terrain/" + i + ".png"));
			TileProp.name.put(i, XMLUtils.getKey(tileDoc, XML_TILE_NODE, "" + i, "name"));
			TileProp.cover.put(i, Utils.val(XMLUtils.getKey(tileDoc, XML_TILE_NODE, "" + i, "cover")));
			TileProp.damage.put(i, Utils.val(XMLUtils.getKey(tileDoc, XML_TILE_NODE, "" + i, "damage")));
			TileProp.altitems.put(i, Utils.getAitem(i, XML_TILE_NODE));
			TileProp.oninteract.put(i, XMLUtils.getKey(tileDoc, XML_TILE_NODE, "" + i, "oninteract"));
			i++;
		}
	}

	private static void loadMapObjects() {
		String skill;
		int i = 0;
		while (i < MadSand.LASTOBJID) {
			objects[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "obj/" + i + ".png"));
			ObjectProp.name.put(i, XMLUtils.getKey(objectDoc, XML_OBJECT_NODE, Utils.str(i), "name"));
			ObjectProp.hp.put(i, Integer.parseInt(XMLUtils.getKey(objectDoc, XML_OBJECT_NODE, Utils.str(i), "tough")));
			ObjectProp.harvestHp.put(i,
					Integer.parseInt(XMLUtils.getKey(objectDoc, XML_OBJECT_NODE, Utils.str(i), "harvesthp")));
			skill = XMLUtils.getKey(objectDoc, XML_OBJECT_NODE, Utils.str(i), "skill");
			if (!skill.equals("-1"))
				ObjectProp.skill.put(i, Skill.valueOf(skill));
			ObjectProp.altitems.put(i, Utils.getAitem(i, XML_OBJECT_NODE));

			ObjectProp.minLvl.put(i,
					Integer.parseInt(XMLUtils.getKey(objectDoc, XML_OBJECT_NODE, Utils.str(i), "lvl")));
			ObjectProp.nocollide.put(i,
					Utils.val(XMLUtils.getKey(objectDoc, XML_OBJECT_NODE, Utils.str(i), "nocollide")));

			ObjectProp.vRendMasks.put(i,
					Integer.parseInt(XMLUtils.getKey(objectDoc, XML_OBJECT_NODE, Utils.str(i), "vmask")));
			ObjectProp.hRendMasks.put(i,
					Integer.parseInt(XMLUtils.getKey(objectDoc, XML_OBJECT_NODE, Utils.str(i), "hmask")));
			ObjectProp.interactAction.put(i, XMLUtils.getKey(objectDoc, XML_OBJECT_NODE, Utils.str(i), "oninteract"));
			i++;
		}
	}

	private static void loadItems() {
		String stgs, stglen;
		String typeStr;
		String[] cont;
		Vector<Integer> stages, slens;
		ItemType type;
		int i = 0, cc = 0;
		while (i < MadSand.LASTITEMID) {
			item[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "inv/" + i + ".png"));
			if (!XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, XML_RECIPE_NODE).equals("-1")) {
				MadSand.craftableid[cc] = i;
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
			ItemProp.craftable.put(i,
					Integer.parseInt(XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "craftable")) != 0);
			ItemProp.recipe.put(i, XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, XML_RECIPE_NODE));
			ItemProp.heal.put(i, XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "heal"));
			ItemProp.useAction.put(i, XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "onuse"));

			String reqlist = XMLUtils.getKey(itemDoc, XML_ITEM_NODE, "" + i, "craftreq");
			cont = reqlist.split("\\,");
			Vector<Integer> reqs = new Vector<Integer>();
			for (String req : cont)
				reqs.add(Integer.parseInt(req));
			if (!reqlist.contains(","))
				reqs.add(Integer.parseInt(reqlist));
			ItemProp.craftReq.put(i, reqs);
			i++;
		}
	}

	private static void loadWorldGen() {
		int i = 0;
		// Loading worldgen config
		Vector<Integer> def;
		Vector<String> group;
		HashMap<String, Integer> lake;

		Vector<String> objGroup;
		Vector<String> ore = new Vector<String>();
		String defT, defO;
		HashMap<String, Integer> vdungeon;
		Utils.out("Initializing worldgen...");
		while (i < MadSand.BIOMES) {
			def = new Vector<Integer>();
			lake = new HashMap<String, Integer>();

			WorldGenProp.name.add(XMLUtils.getAttr(gendoc, XML_BIOME_NODE, Utils.str(i), "name"));
			group = XMLUtils.getGroup(i, "tile_group");
			objGroup = XMLUtils.getGroup(i, "object_group");

			def.add(Integer
					.parseInt(XMLUtils.getAttrValues(gendoc, XML_BIOME_NODE, Utils.str(i), "def_tile", Utils.str(-1))));
			lake = XMLUtils
					.nodeMapToHashMap(XMLUtils.getNested(gendoc, XML_BIOME_NODE, Utils.str(i), "lake", Utils.str(-1)));

			WorldGenProp.loadTileBlock(i, def, group, lake);
			WorldGenProp.loadObjectBlock(i, objGroup);
			defT = XMLUtils.getAttrValues(gendoc, XML_BIOME_NODE, Utils.str(i), "cave_tile", Utils.str(-1));
			defO = XMLUtils.getAttrValues(gendoc, XML_BIOME_NODE, Utils.str(i), "cave_object", Utils.str(-1));
			ore.add(XMLUtils.getAttrValues(gendoc, XML_BIOME_NODE, Utils.str(i), "ore", Utils.str(-1)));
			vdungeon = XMLUtils.nodeMapToHashMap(
					XMLUtils.getNested(gendoc, XML_BIOME_NODE, Utils.str(i), "dungeon", Utils.str(-1)));
			WorldGenProp.loadUnderworldBlock(i, defT, defO, ore, vdungeon);
			++i;
		}
		Utils.out("Done initializing WorldGen!");
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
