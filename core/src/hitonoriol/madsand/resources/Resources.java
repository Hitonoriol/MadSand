package hitonoriol.madsand.resources;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.Gui;
import hitonoriol.madsand.containers.AnimationContainer;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.containers.Pair.PairKeyDeserializer;
import hitonoriol.madsand.entities.TradeListContainer;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.quest.Quest;
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
import hitonoriol.madsand.util.TimeUtils;
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
	public static String SKILL_FILE = "skills.json";
	public static final String SCRIPT_DIR = "scripts/";
	public static final String ENCOUNTER_DIR = "encounter/";
	public static final String ERR_FILE = "MadSandCritical.log";
	public static final String OUT_FILE = "MadSandOutput.log";

	static final int ANIM_FRAME_SIZE = 32;
	public static final float ACTION_ANIM_DURATION = 0.15f;

	private static final TextureAtlas textures = loadAtlas("textures");
	private static TextureMap<String> textureMap = new TextureMap<>(textures);
	private static TextureMap<Integer> tiles = new TextureMap<>(textures, "terrain");
	private static TextureMap<Integer> objects = new TextureMap<>(textures, "obj");
	private static TextureMap<Integer> items = new TextureMap<>(textures, "inv");
	private static TextureMap<Integer> npcs = new TextureMap<>(textures, "npc");

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

	private final static ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	private final static TypeFactory typeFactory = mapper.getTypeFactory();

	public static void loadAll() {
		try {
			init();
		} catch (Exception e) {
			Utils.die("Exception on init: " + ExceptionUtils.getStackTrace(e));
		}
	}

	private static void init() throws Exception {
		Utils.out("Loading resources...");
		Gdx.graphics.setCursor(Gdx.graphics.newCursor(loadPixmap("textures/cursor.png"), 0, 0));
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
		Utils.out("Done loading resources.");
	}

	private static void loadActionAnimations() {
		attackAnimStrip = loadAnimationStrip("anim/hit");
		objectHitAnimStrip = loadAnimationStrip("anim/obj_hit");
		healAnimStrip = loadAnimationStrip("anim/heal");
		detectAnimStrip = loadAnimationStrip("anim/detect");
	}

	private static void initObjectMapper() {
		mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator());

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
		//npcs = new TextureMap<>(loadAtlas("npc"));
		NpcProp.npcs.forEach((id, npc) -> npc.id = id);
	}

	private static void loadMapTiles() throws Exception {
		MapType tileMap = Resources.typeFactory.constructMapType(HashMap.class, Integer.class, Tile.class);
		TileProp.tiles = Resources.mapper.readValue(readInternal(Resources.TILE_FILE), tileMap);

		tileCount = TileProp.tiles.size();
		//tiles = new TextureMap<>(loadAtlas("terrain"));
		Utils.out(tileCount + " tiles");
		TileProp.tiles.forEach((id, tile) -> tile.id = id);
	}

	private static void loadMapObjects() throws Exception {
		MapType objectMap = Resources.typeFactory.constructMapType(HashMap.class, Integer.class, MapObject.class);
		ObjectProp.objects = Resources.mapper.readValue(readInternal(Resources.OBJECT_FILE), objectMap);

		mapObjectCount = ObjectProp.objects.size();
		//objects = new TextureMap<>(loadAtlas("obj"));
		Utils.out(mapObjectCount + " map objects");
		ObjectProp.objects.forEach((id, object) -> object.id = id);
	}

	private static void loadItems() throws Exception {
		MapType itemMap = Resources.typeFactory.constructMapType(HashMap.class, Integer.class, Item.class);
		ItemProp.items = Resources.mapper.readValue(readInternal(Resources.ITEM_FILE), itemMap);
		itemCount = ItemProp.items.size();
		//items = new TextureMap<>(loadAtlas("inv"));

		ItemProp.items.forEach((id, item) -> {
			item.id = id;
			// Load item's craft recipe if it has one
			if (item.recipe != null) {
				if (item.recipe.contains(Item.CRAFTSTATION_DELIM)) {
					String[] craftStationRecipe = item.recipe.split("\\" + Item.CRAFTSTATION_DELIM);
					item.recipe = craftStationRecipe[1];
					ItemProp.addCraftStationRecipe(Utils.val(craftStationRecipe[0]), item.id);
				} else
					ItemProp.craftReq.put(id, Item.parseCraftRequirements(item.recipe));
				++craftableItemCount;
			}
		});
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
		ObjectProp.itemProducers = loadMap(ITEMFACTORY_FILE, Integer.class, ItemProducer.class);
		ObjectProp.buildRecipes = loadMap(BUILDRECIPE_FILE, Integer.class, String.class);

		for (Entry<Integer, String> entry : ObjectProp.buildRecipes.entrySet())
			ItemProp.buildReq.put(entry.getKey(), Item.parseCraftRequirements(entry.getValue()));
	}

	public static TextureRegion getTile(int id) {
		return tiles.get(id);
	}

	public static TextureRegion getObject(int id) {
		return objects.get(id);
	}

	public static TextureRegion getItem(int id) {
		return items.get(id);
	}

	public static TextureRegion getNpc(int id) {
		return npcs.get(id);
	}

	public static TextureRegion getTexture(String name) {
		return textureMap.get(name);
	}

	public static TextureAtlas getAtlas() {
		return textures;
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

	public static TextureAtlas loadAtlas(String name) {
		return new TextureAtlas(Gdx.files.internal("textures/" + name + ".atlas"));
	}

	public static String readInternal(String file) {
		return Gdx.files.internal(file).readString();
	}

	public static void save(String file, Object object) {
		try {
			mapper.writeValue(new File(file), object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static <T> ArrayList<T> loadList(String internalFile, Class<T> type) {
		try {
			return mapper.readValue(readInternal(internalFile),
					typeFactory.constructCollectionType(ArrayList.class, type));
		} catch (Exception e) {
			return null;
		}
	}

	public static MapType getMapType(Class<?> key, Class<?> value) {
		return typeFactory.constructMapType(HashMap.class, key, value);
	}

	public static ObjectWriter getMapWriter(Class<?> key, Class<?> value) {
		return mapper.writerFor(getMapType(key, value));
	}

	public static ObjectReader getMapReader(Class<?> key, Class<?> value) {
		return mapper.readerFor(getMapType(key, value));
	}

	public static ObjectReader getMapReader(Class<?> value) {
		return getMapReader(Pair.class, value);
	}

	public static ObjectWriter getMapWriter(Class<?> value) {
		return getMapWriter(Pair.class, value);
	}

	private static <K, V> HashMap<K, V> loadMap(String file, Class<K> keyType, Class<V> valueType, boolean internal) {
		try {
			return mapper.readValue(internal ? readInternal(file) : GameSaver.readFile(file),
					getMapType(keyType, valueType));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <K, V> HashMap<K, V> loadMap(String internalFile, Class<K> keyType, Class<V> valueType) {
		return loadMap(internalFile, keyType, valueType, true);
	}

	public static <K, V> HashMap<K, V> readMap(String file, Class<K> keyType, Class<V> valueType) {
		return loadMap(file, keyType, valueType, false);
	}

	public static <V> HashMap<Pair, V> readMap(String file, Class<V> valueType) {
		return readMap(file, Pair.class, valueType);
	}

	public static String saveMap(Map<?, ?> map, Class<?> keyType, Class<?> valType) {
		try {
			return getMapWriter(keyType, valType).writeValueAsString(map);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String saveMap(Map<?, ?> map, Class<?> valType) {
		return saveMap(map, Pair.class, valType);
	}

	public static <K, V> void saveMap(String file, Map<K, V> map, Class<K> keyType, Class<V> valType) {
		try {
			getMapWriter(keyType, valType).writeValue(new File(file), map);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ObjectMapper getMapper() {
		return mapper;
	}

	public static NinePatchDrawable loadNinePatch(String file) {
		return new NinePatchDrawable(new NinePatch(getTexture(file)));
	}

	public static Texture loadTexture(String file) {
		return new Texture(Gdx.files.internal("textures/" + file));
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

	public static TextureRegion[] getAnimationStrip(TextureRegion[][] region, int row, int frames) { // convert [][] strip to []
		TextureRegion[] strip = new TextureRegion[frames];

		for (int i = 0; i < frames; ++i)
			strip[i] = region[row][i];

		return strip;
	}

	private static TextureRegion[] loadAnimationStrip(String file, int frameSize) { // load 1xN animation strip from file
		TextureRegion[][] animStrip = getTexture(file).split(frameSize, frameSize);
		return getAnimationStrip(animStrip, 0, animStrip[0].length);
	}

	private static TextureRegion[] loadAnimationStrip(String file) {
		return loadAnimationStrip(file, ANIM_FRAME_SIZE);
	}

	private static Pixmap extractPixmap(TextureRegion textureRegion) {
		TextureData textureData = textureRegion.getTexture().getTextureData();
		if (!textureData.isPrepared())
			textureData.prepare();
		Pixmap pixmap = new Pixmap(
				textureRegion.getRegionWidth(),
				textureRegion.getRegionHeight(),
				textureData.getFormat());
		pixmap.drawPixmap(
				textureData.consumePixmap(),
				0,
				0,
				textureRegion.getRegionX(),
				textureRegion.getRegionY(),
				textureRegion.getRegionWidth(),
				textureRegion.getRegionHeight());
		return pixmap;
	}

	public static Texture createTexture(TextureRegion region) {
		return new Texture(extractPixmap(region));
	}

	private static SimpleDateFormat screenshotDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");

	public static void takeScreenshot() {
		Gui.gameUnfocus();
		TimeUtils.scheduleTask(() -> {
			byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(),
					Gdx.graphics.getBackBufferHeight(), true);

			for (int i = 4; i < pixels.length; i += 4)
				pixels[i - 1] = (byte) 255;

			Pixmap pixmap = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(),
					Pixmap.Format.RGBA8888);

			BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
			PixmapIO.writePNG(Gdx.files.local(Utils.now(screenshotDateFormat) + ".png"), pixmap);
			pixmap.dispose();
			Gui.gameResumeFocus();
		}, Gui.DELAY);
	}
}
