package hitonoriol.madsand;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import org.w3c.dom.Document;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.fasterxml.jackson.databind.type.MapType;

import hitonoriol.madsand.containers.Tuple;
import hitonoriol.madsand.entities.SkillContainer;
import hitonoriol.madsand.entities.TradeListContainer;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.Skill;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.map.Tile;
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

	public static int craftableItemCount;
	public static int itemCount;
	public static int mapObjectCount;
	public static int npcCount;
	public static int tileCount;
	public static int biomeCount;
	public static int questCount;

	public static final String emptyField = "-1";
	public static final int emptyId = -1;
	public static String Space = " ", Colon = ":";
	public static final String LINEBREAK = System.lineSeparator();

	public static void init() throws Exception {
		Utils.out("Loading resources...");

		mapcursor = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/cur.png"));
		animsheet = new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/anim.png"));
		visitedMask = new Texture(Gdx.files.local(MadSand.SAVEDIR + "light/light_visited.png"));
		placeholder = new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/placeholder.png"));
		noEquip = new TextureRegionDrawable(new TextureRegion(placeholder));

		loadPlayerAnimation();

		Cursor mouseCursor = Gdx.graphics.newCursor(new Pixmap(Gdx.files.local(MadSand.SAVEDIR + "cursor.png")), 0, 0);
		Gdx.graphics.setCursor(mouseCursor);

		Globals.loadGlobals();
		loadWorldGen();
		loadItems();
		loadMapObjects();
		loadMapTiles();
		loadQuests();
		loadNpcs();
		loadTradeLists();
		loadTutorial();

		Utils.out("Done loading resources.");
	}

	private static void loadTradeLists() throws Exception {
		NpcProp.tradeLists = MadSand.mapper.readValue(new File(MadSand.TRADELISTFILE), TradeListContainer.class);
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
		npcCount = NpcProp.npcs.size();
		Utils.out(npcCount + " NPCs");

		npc = new Texture[npcCount];

		int i = 0;
		for (Entry<Integer, NpcContainer> npcEntry : NpcProp.npcs.entrySet()) {
			i = npcEntry.getKey();
			npc[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "npc/" + i + ".png"));
		}

	}

	private static void loadMapTiles() throws Exception {
		MapType tileMap = MadSand.typeFactory.constructMapType(HashMap.class, Integer.class, Tile.class);
		TileProp.tiles = MadSand.mapper.readValue(new File(MadSand.TILEFILE), tileMap);

		tileCount = TileProp.tiles.size();
		tile = new Texture[tileCount + 1];
		Utils.out(tileCount + " tiles");

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

		mapObjectCount = ObjectProp.objects.size();
		objects = new Texture[mapObjectCount + 1];
		Utils.out(mapObjectCount + " map objects");

		// Load object textures
		int i;
		for (Entry<Integer, MapObject> objectEntry : ObjectProp.objects.entrySet()) {
			i = objectEntry.getKey();
			objects[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "obj/" + i + ".png"));
		}
	}

	private static void loadItems() throws Exception {
		MapType itemMap = MadSand.typeFactory.constructMapType(HashMap.class, Integer.class, Item.class);
		ItemProp.items = MadSand.mapper.readValue(new File(MadSand.ITEMSFILE), itemMap);
		itemCount = ItemProp.items.size();
		item = new Texture[itemCount];

		int i = 0;
		Item valItem;
		for (Entry<Integer, Item> entry : ItemProp.items.entrySet()) {
			i = entry.getKey();
			valItem = entry.getValue();

			item[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "inv/" + i + ".png"));
			valItem.id = i;

			if (valItem.recipe != null) {
				ItemProp.craftReq.put(i, Item.parseCraftRequirements(valItem.recipe));
				++craftableItemCount;
			}
		}

		Utils.out(craftableItemCount + " craftable items");

	}

	private static void loadWorldGen() throws Exception {
		MapType worldGenMap = MadSand.typeFactory.constructMapType(HashMap.class, Integer.class, WorldGenPreset.class);
		WorldGenProp.biomes = MadSand.mapper.readValue(new File(MadSand.GENFILE), worldGenMap);
		biomeCount = WorldGenProp.biomes.size();

		Utils.out(biomeCount + " biomes");
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

	private static void loadPlayerAnimation() {
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

		playerDownSpr = new Sprite(new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/d1.png")));
		playerUpSpr = new Sprite(new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/u1.png")));
		playerRightSpr = new Sprite(new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/r1.png")));
		playerLeftSpr = new Sprite(new Texture(Gdx.files.local(MadSand.SAVEDIR + "player/l1.png")));
	}
	
	static final String FONT_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;:,{}\"'<>";
	static final String FONT_PATH = "fonts/8bitoperator.ttf";
	
	public static BitmapFont createFont(int size) {
		BitmapFont font = new BitmapFont();
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.local(MadSand.SAVEDIR + FONT_PATH));
		FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
		param.characters = FONT_CHARS;
		param.size = size;
		param.color = Color.WHITE;
		param.borderWidth = 0.9f;
		param.borderColor = Color.BLACK;
		font = generator.generateFont(param);
		generator.dispose();
		font.getData().markupEnabled = true;
		return font;
	}

}
