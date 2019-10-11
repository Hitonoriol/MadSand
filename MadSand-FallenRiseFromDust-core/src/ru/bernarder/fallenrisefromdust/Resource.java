package ru.bernarder.fallenrisefromdust;

import java.util.HashMap;
import java.util.Vector;

import org.w3c.dom.Document;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import ru.bernarder.fallenrisefromdust.enums.ItemType;
import ru.bernarder.fallenrisefromdust.enums.Skill;
import ru.bernarder.fallenrisefromdust.properties.CropProp;
import ru.bernarder.fallenrisefromdust.properties.ItemProp;
import ru.bernarder.fallenrisefromdust.properties.ObjectProp;
import ru.bernarder.fallenrisefromdust.properties.TileProp;
import ru.bernarder.fallenrisefromdust.properties.WorldGenProp;

public class Resource {

	static Document resdoc;
	static Document questdoc;
	static Document gendoc;
	static Document skilldoc;
	
	static Texture[] item;
	static Texture[] objects;
	static Texture[] tile;
	static Texture[] npc;
	
	static TextureRegion[][] tmpAnim;
	
	static Texture animsheet;
	
	static TextureRegion[] animup = new TextureRegion[2];
	static TextureRegion[] animdown = new TextureRegion[2];
	static TextureRegion[] animleft = new TextureRegion[2];
	static TextureRegion[] animright = new TextureRegion[2];
	
	static Animation<TextureRegion> uanim;
	static Animation<TextureRegion> danim;
	static Animation<TextureRegion> lanim;
	static Animation<TextureRegion> ranim;
	
	static Texture placeholder;
	static TextureRegionDrawable noEquip;
	
	static Sprite playerSprite;
	
	static Texture rtex;
	static Texture ltex;
	static Texture utex;
	static Texture dtex;

	public static void init() {
		resdoc = Utils.XMLString(GameSaver.getExternalNl(MadSand.RESFILE));
		questdoc = Utils.XMLString(GameSaver.getExternalNl(MadSand.QUESTFILE));
		gendoc = Utils.XMLString(GameSaver.getExternalNl(MadSand.GENFILE));
		mapcursor = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/cur.png"));
		animsheet = new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/anim.png"));
		tmpAnim = TextureRegion.split(animsheet, 35, 74);
		animdown[0] = tmpAnim[0][0];
		animdown[1] = tmpAnim[0][1];
		animleft[0] = tmpAnim[1][0];
		animleft[1] = tmpAnim[1][1];
		animright[0] = tmpAnim[2][0];
		animright[1] = tmpAnim[2][1];
		animup[0] = tmpAnim[3][0];
		animup[1] = tmpAnim[3][1];
		uanim = new Animation<TextureRegion>(0.2F, animup);
		danim = new Animation<TextureRegion>(0.2F, animdown);
		lanim = new Animation<TextureRegion>(0.2F, animleft);
		ranim = new Animation<TextureRegion>(0.2F, animright);
		Cursor customCursor = Gdx.graphics
				.newCursor(new com.badlogic.gdx.graphics.Pixmap(Gdx.files.local(MadSand.SAVEDIR + "cursor.png")), 0, 0);
		Gdx.graphics.setCursor(customCursor);
		MadSand.QUESTS = Utils.countKeys(questdoc, "quest");
		MadSand.quests = new int[MadSand.QUESTS][2];
		MadSand.LASTITEMID = Utils.countKeys(resdoc, "item");
		MadSand.CROPS = Utils.countKeys(resdoc, "stages");
		Utils.out(MadSand.CROPS + " crops");
		MadSand.LASTOBJID = Utils.countKeys(resdoc, "object");
		Utils.out(MadSand.LASTOBJID + " objects");
		MadSand.LASTTILEID = Utils.countKeys(resdoc, "tile");
		MadSand.NPCSPRITES = Utils.countKeys(resdoc, "npc");
		MadSand.CRAFTABLES = Utils.countKeys(resdoc, "recipe");
		MadSand.BIOMES = Utils.countKeys(gendoc, "biome");
		Utils.out(MadSand.BIOMES + " biomes");
		Utils.out(MadSand.CRAFTABLES + " craftable items");
		Utils.out(MadSand.LASTTILEID + " tiles");
		Utils.out(MadSand.NPCSPRITES + " npcs");
		MadSand.craftableid = new int[MadSand.CRAFTABLES];
		item = new Texture[MadSand.LASTITEMID + 1];
		objects = new Texture[MadSand.LASTOBJID];
		tile = new Texture[MadSand.LASTTILEID + 1];
		npc = new Texture[MadSand.NPCSPRITES + 1];
		String stgs, stglen;
		String[] cont;
		Vector<Integer> stages, slens;
		int i = 0, cc = 0;
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

			WorldGenProp.name.add(Utils.getAttr(gendoc, "biome", Utils.str(i), "name"));
			group = Utils.getGroup(i, "tile_group");
			objGroup = Utils.getGroup(i, "object_group");

			def.add(Integer.parseInt(Utils.getAttrValues(gendoc, "biome", Utils.str(i), "def_tile", Utils.str(-1))));
			lake = Utils.nodeMapToHashMap(Utils.getNested(gendoc, "biome", Utils.str(i), "lake", Utils.str(-1)));

			WorldGenProp.loadTileBlock(i, def, group, lake);
			WorldGenProp.loadObjectBlock(i, objGroup);
			defT = Utils.getAttrValues(gendoc, "biome", Utils.str(i), "cave_tile", Utils.str(-1));
			defO = Utils.getAttrValues(gendoc, "biome", Utils.str(i), "cave_object", Utils.str(-1));
			ore.add(Utils.getAttrValues(gendoc, "biome", Utils.str(i), "ore", Utils.str(-1)));
			vdungeon = Utils.nodeMapToHashMap(Utils.getNested(gendoc, "biome", Utils.str(i), "dungeon", Utils.str(-1)));
			WorldGenProp.loadUnderworldBlock(i, defT, defO, ore, vdungeon);
			++i;
		}
		Utils.out("Done initializing WorldGen!");
		i = 0;
		// Loading everything abUtils.out inventory items
		// Craft recipes
		while (i < MadSand.LASTITEMID) {
			item[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "inv/" + i + ".png"));
			if (!Utils.getKey(resdoc, "item", "" + i, "recipe").equals("-1")) {
				MadSand.craftableid[cc] = i;
				cc++;
			}

			// Crops
			stgs = Utils.getKey(resdoc, "item", "" + i, "stages");
			if (!stgs.equals("-1")) {
				cont = stgs.split("\\,");
				stages = new Vector<Integer>();
				for (String stage : cont)
					stages.add(Integer.parseInt(stage));
				CropProp.stages.put(i, stages);
				stglen = Utils.getKey(resdoc, "item", "" + i, "stages");
				cont = stglen.split("\\,");
				slens = new Vector<Integer>();
				for (String slen : cont)
					slens.add(Integer.parseInt(slen));
				CropProp.stagelen.put(i, slens);
			}

			// Item properties
			ItemProp.name.put(i, Utils.getKey(resdoc, "item", "" + i, "name"));
			ItemProp.type.put(i, ItemType.get(Integer.parseInt(Utils.getKey(resdoc, "item", "" + i, "type"))));
			ItemProp.altObject.put(i, Integer.parseInt(Utils.getKey(resdoc, "item", "" + i, "altobject")));
			
			ItemProp.dmg.put(i, Integer.parseInt(Utils.getKey(resdoc, "item", "" + i, "dmg", "0")));
			
			ItemProp.hp.put(i, Integer.parseInt(Utils.getKey(resdoc, "item", "" + i, "hp")));
			ItemProp.cost.put(i, Integer.parseInt(Utils.getKey(resdoc, "item", "" + i, "cost")));
			ItemProp.craftable.put(i, Integer.parseInt(Utils.getKey(resdoc, "item", "" + i, "craftable")) != 0);
			ItemProp.recipe.put(i, Utils.getKey(resdoc, "item", "" + i, "recipe"));
			ItemProp.heal.put(i, Utils.getKey(resdoc, "item", "" + i, "heal"));
			ItemProp.useAction.put(i, Utils.getKey(resdoc, "item", "" + i, "onuse"));
			i++;
		}
		i = 0;

		// Loading map objects
		String skill;
		while (i < MadSand.LASTOBJID) {
			objects[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "obj/" + i + ".png"));
			ObjectProp.name.put(i, Utils.getKey(resdoc, "object", Utils.str(i), "name"));
			ObjectProp.hp.put(i, Integer.parseInt(Utils.getKey(resdoc, "object", Utils.str(i), "tough")));
			ObjectProp.harvestHp.put(i, Integer.parseInt(Utils.getKey(resdoc, "object", Utils.str(i), "harvesthp")));
			skill = Utils.getKey(resdoc, "object", Utils.str(i), "skill");
			if (!skill.equals("-1"))
				ObjectProp.skill.put(i, Skill.valueOf(skill));
			ObjectProp.altitems.put(i, Utils.getAitem(i, "object"));
			
			ObjectProp.minLvl.put(i, Integer.parseInt(Utils.getKey(resdoc, "object", Utils.str(i), "lvl")));
			
			ObjectProp.vRendMasks.put(i, Integer.parseInt(Utils.getKey(resdoc, "object", Utils.str(i), "vmask")));
			ObjectProp.hRendMasks.put(i, Integer.parseInt(Utils.getKey(resdoc, "object", Utils.str(i), "hmask")));
			ObjectProp.interactAction.put(i, Utils.getKey(resdoc, "object", Utils.str(i), "oninteract"));
			i++;
		}
		i = 0;

		// Loading tiles
		while (i < MadSand.LASTTILEID) {
			tile[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "terrain/" + i + ".png"));
			TileProp.name.put(i, Utils.getKey(resdoc, "tile", "" + i, "name"));
			TileProp.damage.put(i, Utils.val(Utils.getKey(resdoc, "tile", "" + i, "damage")));
			TileProp.altitems.put(i, Utils.getAitem(i, "tile"));
			i++;
		}
		i = 0;

		// Loading NPCs
		while (i < MadSand.NPCSPRITES) {
			npc[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "npc/" + i + ".png"));
			Utils.getKey(resdoc, "npc", "" + i, "hp");
			Utils.getKey(resdoc, "npc", "" + i, "maxhp");
			Utils.getKey(resdoc, "npc", "" + i, "rewardexp");
			Utils.getKey(resdoc, "npc", "" + i, "drop");
			Utils.getKey(resdoc, "npc", "" + i, "name");
			Utils.getKey(resdoc, "npc", "" + i, "atk");
			Utils.getKey(resdoc, "npc", "" + i, "accuracy");
			Utils.getKey(resdoc, "npc", "" + i, "friendly");
			Utils.getKey(resdoc, "npc", "" + i, "fraction");
			Utils.getKey(resdoc, "npc", "" + i, "spawnonce");
			Utils.getKey(resdoc, "npc", "" + i, "qids");
			i++;
		}

		placeholder = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/placeholder.png"));
		noEquip = new TextureRegionDrawable(new TextureRegion(placeholder));
		FileHandle pfhandle = Gdx.files.local(MadSand.SAVEDIR + "player/d1.png");
		dtex = new Texture(pfhandle);
		pfhandle = Gdx.files.local(MadSand.SAVEDIR + "player/u1.png");
		utex = new Texture(pfhandle);
		pfhandle = Gdx.files.local(MadSand.SAVEDIR + "player/r1.png");
		rtex = new Texture(pfhandle);
		pfhandle = Gdx.files.local(MadSand.SAVEDIR + "player/l1.png");
		ltex = new Texture(pfhandle);
		playerSprite = new Sprite(dtex);
	}

	static Texture mapcursor;

	static void loadSkillReqs() {
		if (SkillContainer.reqList.size() > 0)
			return;
		skilldoc = Utils.XMLString(GameSaver.getExternalNl(MadSand.SKILLFILE));
		int i = 0;
		int skills = Utils.countKeys(skilldoc, "skill");
		Skill skill;
		String skillStr;
		int req;
		double mul;
		while (i < skills) {
			skillStr = Utils.getAttr(skilldoc, "skill", Utils.str(i), "name");
			if (skillStr.equals("-1")) {
				++i;
				continue;
			}
			skill = Skill.valueOf(skillStr);
			req = Utils.val(Utils.getKey(skilldoc, "skill", Utils.str(i), "required"));
			mul = Double.parseDouble(Utils.getKey(skilldoc, "skill", Utils.str(i), "multiplier"));
			Utils.out(skill + " " + req + " " + mul);
			SkillContainer.reqList.put(skill, Utils.makeTuple(req, mul));
			++i;
		}
	}
}
