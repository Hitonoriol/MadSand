package hitonoriol.madsand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import hitonoriol.madsand.containers.AnimationContainer;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.containers.Pair.PairKeyDeserializer;
import hitonoriol.madsand.entities.TradeListContainer;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.quest.Quest;
import hitonoriol.madsand.entities.skill.Skill;
import hitonoriol.madsand.entities.skill.SkillContainer;
import hitonoriol.madsand.entities.skill.SkillValue;
import hitonoriol.madsand.map.ItemProducer;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.NpcContainer;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.properties.QuestList;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.properties.Tutorial;
import hitonoriol.madsand.properties.WorldGenProp;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.worldgen.WorldGenPreset;

public class Resources {

	static String QUEST_FILE = "quests.json";
	static String WORLDGEN_FILE = "worldgen.json";
	static String ENCOUNTER_FILE = "encounters.json";
	static String TUTORIAL_FILE = "tutorial.json";
	static String TRADELIST_FILE = "tradelists.json";
	static String TILE_FILE = "tiles.json";
	static String OBJECT_FILE = "objects.json";
	static String ITEMFACTORY_FILE = "itemfactories.json";
	static String NPC_FILE = "npcs.json";
	static String ITEM_FILE = "items.json";
	public static String GLOBALS_FILE = "globals.json";
	public static String BUILDRECIPE_FILE = "buildrecipes.json";
	static String SKILL_FILE = "skills.json";

	public static final String SCRIPT_DIR = "scripts/";
	public static final String ENCOUNTER_DIR = "encounter/";

	public static final String ERR_FILE = "MadSandCritical.log";
	public static final String OUT_FILE = "MadSandOutput.log";

	static final int ANIM_FRAME_SIZE = 32;
	public static final float ACTION_ANIM_DURATION = 0.15f;

	public static Texture[] item;
	public static Texture[] objects;
	public static Texture[] tile;
	public static Texture visitedMask;
	public static Texture[] npc;

	static Texture animsheet;
	public static NinePatchDrawable transparency = loadNinePatch("misc/transparency.png");

	static float playerAnimDuration = 0.2f;

	static TextureRegion[] animup = new TextureRegion[2];
	static TextureRegion[] animdown = new TextureRegion[2];
	static TextureRegion[] animleft = new TextureRegion[2];
	static TextureRegion[] animright = new TextureRegion[2];

	public static Animation<TextureRegion> uanim, danim, lanim, ranim;

	public static Texture mapCursor;
	public static Texture questArrow;
	static Texture placeholder;
	public static TextureRegionDrawable noEquip;

	public static Sprite playerRightSpr;
	public static Sprite playerLeftSpr;
	public static Sprite playerUpSpr;
	public static Sprite playerDownSpr;

	public static TextureRegion[] attackAnimStrip, objectHitAnimStrip;
	public static TextureRegion[] healAnimStrip, detectAnimStrip;

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
	public static final String Tab = "      ";
	public static final String LINEBREAK = System.lineSeparator();
	public static String COLOR_END = "[]";

	public static ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	public static TypeFactory typeFactory = mapper.getTypeFactory();

	public static void loadAll() {
		try {
			init();
		} catch (Exception e) {
			Utils.die("Exception on init: " + ExceptionUtils.getStackTrace(e));
		}
	}
	
	private static void init() throws Exception {
		Utils.out("Loading resources...");

		mapCursor = loadTexture("misc/cur.png");
		animsheet = loadTexture("player/anim.png");
		visitedMask = loadTexture("light/light_visited.png");
		placeholder = loadTexture("misc/placeholder.png");
		questArrow = loadTexture("misc/arrow.png");
		noEquip = new TextureRegionDrawable(new TextureRegion(placeholder));

		loadPlayerAnimation();

		Cursor mouseCursor = Gdx.graphics.newCursor(loadPixmap("cursor.png"), 0, 0);
		Gdx.graphics.setCursor(mouseCursor);

		initObjectMapper();

		Globals.loadGlobals();
		loadItems();
		loadMapTiles();
		loadMapObjects();
		loadProductionStations();
		loadWorldGen();
		loadQuests();
		loadNpcs();
		loadTradeLists();
		loadTutorial();
		loadActionAnimations();
		loadSkillReqs();

		Utils.out("Done loading resources.");
	}

	private static void loadActionAnimations() {
		attackAnimStrip = loadAnimationStrip("anim/hit.png");
		objectHitAnimStrip = loadAnimationStrip("anim/obj_hit.png");
		healAnimStrip = loadAnimationStrip("anim/heal.png");
		detectAnimStrip = loadAnimationStrip("anim/detect.png");
	}

	private static void initObjectMapper() {
		mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.enableDefaultTyping();

		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addKeyDeserializer(Pair.class, new PairKeyDeserializer());

		mapper.registerModule(simpleModule);
	}

	private static void loadTradeLists() throws Exception {
		NpcProp.tradeLists = Resources.mapper.readValue(readInternal(Resources.TRADELIST_FILE),
				TradeListContainer.class);
	}

	private static void loadTutorial() throws Exception {
		MapType tutorialMap = Resources.typeFactory.constructMapType(HashMap.class, String.class, String.class);
		Tutorial.strings = Resources.mapper.readValue(readInternal(Resources.TUTORIAL_FILE), tutorialMap);
	}

	private static void loadQuests() throws Exception {
		MapType questMap = Resources.typeFactory.constructMapType(HashMap.class, Integer.class, Quest.class);
		QuestList.quests = Resources.mapper.readValue(readInternal(Resources.QUEST_FILE), questMap);

		for (Entry<Integer, Quest> entry : QuestList.quests.entrySet())
			entry.getValue().id = entry.getKey();

		Utils.out(QuestList.quests.size() + " quests");

	}

	private static void loadNpcs() throws Exception {
		MapType npcMap = Resources.typeFactory.constructMapType(HashMap.class, Integer.class, NpcContainer.class);
		NpcProp.npcs = Resources.mapper.readValue(readInternal(Resources.NPC_FILE), npcMap);
		npcCount = NpcProp.npcs.size();
		Utils.out(npcCount + " NPCs");

		npc = new Texture[npcCount];

		int i = 0;
		for (Entry<Integer, NpcContainer> npcEntry : NpcProp.npcs.entrySet()) {
			i = npcEntry.getKey();
			npc[i] = loadTexture("npc/" + i + ".png");
			npcEntry.getValue().id = i;
		}

	}

	private static void loadMapTiles() throws Exception {
		MapType tileMap = Resources.typeFactory.constructMapType(HashMap.class, Integer.class, Tile.class);
		TileProp.tiles = Resources.mapper.readValue(readInternal(Resources.TILE_FILE), tileMap);

		tileCount = TileProp.tiles.size();
		tile = new Texture[tileCount];
		Utils.out(tileCount + " tiles");

		// Load tile textures
		int i = 0;
		for (Entry<Integer, Tile> tileEntry : TileProp.tiles.entrySet()) {
			i = tileEntry.getKey();

			tile[i] = loadTexture("terrain/" + i + ".png");
			tileEntry.getValue().id = i;
		}
	}

	private static void loadMapObjects() throws Exception {
		MapType objectMap = Resources.typeFactory.constructMapType(HashMap.class, Integer.class, MapObject.class);
		ObjectProp.objects = Resources.mapper.readValue(readInternal(Resources.OBJECT_FILE), objectMap);

		mapObjectCount = ObjectProp.objects.size();
		objects = new Texture[mapObjectCount + 1];
		Utils.out(mapObjectCount + " map objects");

		// Load object textures
		int i;
		for (Entry<Integer, MapObject> objectEntry : ObjectProp.objects.entrySet()) {
			i = objectEntry.getKey();

			objects[i] = loadTexture("obj/" + i + ".png");
			objectEntry.getValue().id = i;
		}
	}

	private static void loadItems() throws Exception {
		MapType itemMap = Resources.typeFactory.constructMapType(HashMap.class, Integer.class, Item.class);
		ItemProp.items = Resources.mapper.readValue(readInternal(Resources.ITEM_FILE), itemMap);
		itemCount = ItemProp.items.size();
		item = new Texture[itemCount];

		int i = 0;
		Item valItem;
		String craftStationRecipe[];
		for (Entry<Integer, ? extends Item> entry : ItemProp.items.entrySet()) {
			i = entry.getKey();
			valItem = entry.getValue();

			item[i] = loadTexture("inv/" + i + ".png");
			valItem.id = i;

			// Load item's craft recipe if it has one
			if (valItem.recipe != null) {
				if (valItem.recipe.contains(Item.CRAFTSTATION_DELIM)) {
					craftStationRecipe = valItem.recipe.split("\\" + Item.CRAFTSTATION_DELIM);
					valItem.recipe = craftStationRecipe[1];
					ItemProp.addCraftStationRecipe(Utils.val(craftStationRecipe[0]), valItem.id);
					++craftableItemCount;
				} else {
					ItemProp.craftReq.put(i, Item.parseCraftRequirements(valItem.recipe));
					++craftableItemCount;
				}
			}
		}

		Utils.out(craftableItemCount + " craftable items");
	}

	private static void loadWorldGen() throws Exception {
		MapType worldGenMap = Resources.typeFactory.constructMapType(HashMap.class, Integer.class,
				WorldGenPreset.class);
		WorldGenProp.biomes = Resources.mapper.readValue(readInternal(Resources.WORLDGEN_FILE), worldGenMap);
		biomeCount = WorldGenProp.biomes.size();

		WorldGenProp.encounters = Resources.mapper.readValue(readInternal(Resources.ENCOUNTER_FILE),
				typeFactory.constructParametricType(ArrayList.class, String.class));

		Utils.out(biomeCount + " biomes");
	}

	private static void loadProductionStations() throws Exception {
		ObjectProp.productionStations = mapper.readValue(
				readInternal(Resources.ITEMFACTORY_FILE),
				getMapType(Integer.class, ItemProducer.class));

		ObjectProp.buildRecipes = mapper.readValue(
				readInternal(Resources.BUILDRECIPE_FILE),
				getMapType(Integer.class, String.class));
		for (Entry<Integer, String> entry : ObjectProp.buildRecipes.entrySet())
			ItemProp.buildReq.put(entry.getKey(), Item.parseCraftRequirements(entry.getValue()));

	}

	public static void loadSkillReqs() throws Exception {
		SkillContainer.reqList = mapper.readValue(readInternal(Resources.SKILL_FILE),
				getMapType(Skill.class, SkillValue.class));
	}

	static final int PLAYER_ANIM_WIDTH = 35;
	static final int PLAYER_ANIM_HEIGHT = 74;
	static final int PLAYER_ANIM_FRAMES = 2;

	private static void loadPlayerAnimation() {
		TextureRegion[][] tmpAnim = TextureRegion.split(animsheet, PLAYER_ANIM_WIDTH, PLAYER_ANIM_HEIGHT);

		animdown = getAnimationStrip(tmpAnim, 0, PLAYER_ANIM_FRAMES);
		animleft = getAnimationStrip(tmpAnim, 1, PLAYER_ANIM_FRAMES);
		animright = getAnimationStrip(tmpAnim, 2, PLAYER_ANIM_FRAMES);
		animup = getAnimationStrip(tmpAnim, 3, PLAYER_ANIM_FRAMES);

		uanim = new Animation<TextureRegion>(playerAnimDuration, animup);
		danim = new Animation<TextureRegion>(playerAnimDuration, animdown);
		lanim = new Animation<TextureRegion>(playerAnimDuration, animleft);
		ranim = new Animation<TextureRegion>(playerAnimDuration, animright);

		playerDownSpr = new Sprite(animdown[0]);
		playerUpSpr = new Sprite(animup[0]);
		playerRightSpr = new Sprite(animright[0]);
		playerLeftSpr = new Sprite(animleft[0]);
	}

	static final String FONT_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;:,{}\"'<>";
	static final String FONT_PATH = "fonts/8bitoperator.ttf";

	public static BitmapFont createFont(int size) {
		BitmapFont font = new BitmapFont();
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_PATH));
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

	public static String readInternal(String file) {
		return Gdx.files.internal(file).readString();
	}

	public static MapType getMapType(Class<?> key, Class<?> value) {
		return Resources.typeFactory.constructMapType(HashMap.class, key, value);
	}

	public static NinePatchDrawable loadNinePatch(String file) {
		return new NinePatchDrawable(new NinePatch(loadTexture(file)));
	}

	public static Texture loadTexture(String file) {
		return new Texture(Gdx.files.internal(file));
	}

	public static Pixmap loadPixmap(String file) {
		return new Pixmap(Gdx.files.internal(file));
	}

	public static AnimationContainer createAnimation(TextureRegion[] strip, float duration) { // Create animation from loaded strip
		return new AnimationContainer(duration, strip);
	}

	public static AnimationContainer createAnimation(TextureRegion[] strip) {
		return createAnimation(strip, ACTION_ANIM_DURATION);
	}

	private static TextureRegion[] getAnimationStrip(TextureRegion[][] region, int row, int frames) { // convert [][] strip to []
		TextureRegion[] strip = new TextureRegion[frames];

		for (int i = 0; i < frames; ++i)
			strip[i] = region[row][i];

		return strip;
	}

	private static TextureRegion[] loadAnimationStrip(String file, int frameSize) { // load 1xN animation strip from file
		TextureRegion[][] animStrip = TextureRegion.split(loadTexture(file), frameSize, frameSize);
		return getAnimationStrip(animStrip, 0, animStrip[0].length);
	}

	private static TextureRegion[] loadAnimationStrip(String file) {
		return loadAnimationStrip(file, ANIM_FRAME_SIZE);
	}

	public static void takeScreenshot() {
		byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(),
				Gdx.graphics.getBackBufferHeight(), true);

		for (int i = 4; i < pixels.length; i += 4)
			pixels[i - 1] = (byte) 255;

		Pixmap pixmap = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(),
				Pixmap.Format.RGBA8888);

		BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
		PixmapIO.writePNG(Gdx.files.local(System.currentTimeMillis() + ".png"), pixmap);
		pixmap.dispose();
	}
}
