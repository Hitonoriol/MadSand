package hitonoriol.madsand;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

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
import hitonoriol.madsand.entities.SkillContainer;
import hitonoriol.madsand.entities.SkillValue;
import hitonoriol.madsand.entities.TradeListContainer;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.entities.quest.Quest;
import hitonoriol.madsand.enums.Skill;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.map.ProductionStation;
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

	static final int ANIM_FRAME_SIZE = 32;
	static final float ACTION_ANIM_DURATION = 0.15f;

	public static Texture[] item;
	public static Texture[] objects;
	static Texture[] tile;
	static Texture visitedMask;
	public static Texture[] npc;

	static TextureRegion[][] tmpAnim;

	static Texture animsheet;
	public static NinePatchDrawable transparency = loadNinePatch("misc/transparency.png");

	static float playerAnimDuration = 0.2f;

	static TextureRegion[] animup = new TextureRegion[2];
	static TextureRegion[] animdown = new TextureRegion[2];
	static TextureRegion[] animleft = new TextureRegion[2];
	static TextureRegion[] animright = new TextureRegion[2];

	static Animation<TextureRegion> uanim;
	static Animation<TextureRegion> danim;
	static Animation<TextureRegion> lanim;
	static Animation<TextureRegion> ranim;

	public static Texture questArrow;
	static Texture placeholder;
	public static TextureRegionDrawable noEquip;

	public static Sprite playerRightSpr;
	public static Sprite playerLeftSpr;
	public static Sprite playerUpSpr;
	public static Sprite playerDownSpr;

	public static TextureRegion[] attackAnimStrip;

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

	public static ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	public static TypeFactory typeFactory = mapper.getTypeFactory();

	public static void init() throws Exception {
		Utils.out("Loading resources...");

		mapcursor = loadTexture("misc/cur.png");
		animsheet = loadTexture("player/anim.png");
		visitedMask = loadTexture("light/light_visited.png");
		placeholder = loadTexture("misc/placeholder.png");
		questArrow = loadTexture("misc/arrow.png");
		noEquip = new TextureRegionDrawable(new TextureRegion(placeholder));

		loadPlayerAnimation();

		Cursor mouseCursor = Gdx.graphics.newCursor(new Pixmap(Gdx.files.local(MadSand.SAVEDIR + "cursor.png")), 0, 0);
		Gdx.graphics.setCursor(mouseCursor);

		initObjectMapper();

		Globals.loadGlobals();
		loadWorldGen();
		loadItems();
		loadMapObjects();
		loadProductionStations();
		loadMapTiles();
		loadQuests();
		loadNpcs();
		loadTradeLists();
		loadTutorial();
		loadActionAnimations();
		loadSkillReqs();

		Utils.out("Done loading resources.");
	}

	private static void loadActionAnimations() {
		attackAnimStrip = loadAnimationStrip("anim/hit.png", ANIM_FRAME_SIZE);
	}

	private static void initObjectMapper() {
		mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addKeyDeserializer(Pair.class, Pair.getInstance().new PairKeyDeserializer());

		mapper.registerModule(simpleModule);
	}

	private static void loadTradeLists() throws Exception {
		NpcProp.tradeLists = Resources.mapper.readValue(new File(MadSand.TRADELISTFILE), TradeListContainer.class);
	}

	private static void loadTutorial() throws Exception {
		MapType tutorialMap = Resources.typeFactory.constructMapType(HashMap.class, String.class, String.class);
		Tutorial.strings = Resources.mapper.readValue(new File(MadSand.TUTORIALFILE), tutorialMap);
	}

	private static void loadQuests() throws Exception {
		MapType questMap = Resources.typeFactory.constructMapType(HashMap.class, Integer.class, Quest.class);
		QuestList.quests = Resources.mapper.readValue(new File(MadSand.QUESTFILE), questMap);

		for (Entry<Integer, Quest> entry : QuestList.quests.entrySet())
			entry.getValue().id = entry.getKey();

		Utils.out(QuestList.quests.size() + " quests");

	}

	private static void loadNpcs() throws Exception {
		MapType npcMap = Resources.typeFactory.constructMapType(HashMap.class, Integer.class, NpcContainer.class);
		NpcProp.npcs = Resources.mapper.readValue(new File(MadSand.NPCFILE), npcMap);
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
		MapType tileMap = Resources.typeFactory.constructMapType(HashMap.class, Integer.class, Tile.class);
		TileProp.tiles = Resources.mapper.readValue(new File(MadSand.TILEFILE), tileMap);

		tileCount = TileProp.tiles.size();
		tile = new Texture[tileCount];
		Utils.out(tileCount + " tiles");

		// Load tile textures
		int i = 0;
		for (Entry<Integer, Tile> tileEntry : TileProp.tiles.entrySet()) {
			i = tileEntry.getKey();

			tile[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "terrain/" + i + ".png"));
			tileEntry.getValue().id = i;
		}
	}

	private static void loadMapObjects() throws Exception {
		MapType objectMap = Resources.typeFactory.constructMapType(HashMap.class, Integer.class, MapObject.class);
		ObjectProp.objects = Resources.mapper.readValue(new File(MadSand.OBJECTFILE), objectMap);

		mapObjectCount = ObjectProp.objects.size();
		objects = new Texture[mapObjectCount + 1];
		Utils.out(mapObjectCount + " map objects");

		// Load object textures
		int i;
		for (Entry<Integer, MapObject> objectEntry : ObjectProp.objects.entrySet()) {
			i = objectEntry.getKey();

			objects[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "obj/" + i + ".png"));
			objectEntry.getValue().id = i;
		}
	}

	private static void loadItems() throws Exception {
		MapType itemMap = Resources.typeFactory.constructMapType(HashMap.class, Integer.class, Item.class);
		ItemProp.items = Resources.mapper.readValue(new File(MadSand.ITEMSFILE), itemMap);
		itemCount = ItemProp.items.size();
		item = new Texture[itemCount];

		int i = 0;
		Item valItem;
		String craftStationRecipe[];
		for (Entry<Integer, Item> entry : ItemProp.items.entrySet()) {
			i = entry.getKey();
			valItem = entry.getValue();

			item[i] = new Texture(Gdx.files.local(MadSand.SAVEDIR + "inv/" + i + ".png"));
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
		WorldGenProp.biomes = Resources.mapper.readValue(new File(MadSand.GENFILE), worldGenMap);
		biomeCount = WorldGenProp.biomes.size();

		Utils.out(biomeCount + " biomes");
	}

	private static void loadProductionStations() throws Exception {
		ObjectProp.productionStations = mapper.readValue(
				new File(MadSand.PRODSTATIONFILE),
				getMapType(Integer.class, ProductionStation.class));

		ObjectProp.buildRecipes = mapper.readValue(
				new File(MadSand.BUILDRECIPE),
				getMapType(Integer.class, String.class));
		for (Entry<Integer, String> entry : ObjectProp.buildRecipes.entrySet())
			ItemProp.buildReq.put(entry.getKey(), Item.parseCraftRequirements(entry.getValue()));

	}

	static Texture mapcursor;

	public static void loadSkillReqs() throws Exception{
		SkillContainer.reqList = mapper.readValue(new File(MadSand.SKILLFILE), getMapType(Skill.class, SkillValue.class));
	}

	static final int PLAYER_ANIM_WIDTH = 35;
	static final int PLAYER_ANIM_HEIGHT = 74;
	static final int PLAYER_ANIM_FRAMES = 2;

	private static void loadPlayerAnimation() {
		tmpAnim = TextureRegion.split(animsheet, PLAYER_ANIM_WIDTH, PLAYER_ANIM_HEIGHT);

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

	public static MapType getMapType(Class<?> key, Class<?> value) {
		return Resources.typeFactory.constructMapType(HashMap.class, key, value);
	}

	public static NinePatchDrawable loadNinePatch(String file) {
		return new NinePatchDrawable(new NinePatch(loadTexture(file)));
	}

	public static Texture loadTexture(String file) {
		return new Texture(Gdx.files.local(MadSand.SAVEDIR + file));
	}

	public static AnimationContainer createAnimation(TextureRegion[] strip, float duration) { // Create animation from loaded strip
		return new AnimationContainer(duration, strip);
	}

	public static AnimationContainer createAnimation(TextureRegion[] strip) {
		return createAnimation(strip, ACTION_ANIM_DURATION);
	}

	public static TextureRegion[] loadAnimationStrip(String file, int frameSize) { // load 1xN animation strip from file
		TextureRegion[][] animStrip = TextureRegion.split(loadTexture(file), frameSize, frameSize);
		Utils.out("Loading animstrip w len: " + animStrip[0].length);
		return getAnimationStrip(animStrip, 0, animStrip[0].length);
	}

	private static TextureRegion[] getAnimationStrip(TextureRegion[][] region, int row, int frames) { // convert [][] strip to []
		TextureRegion[] strip = new TextureRegion[frames];

		for (int i = 0; i < frames; ++i)
			strip[i] = region[row][i];

		return strip;
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
