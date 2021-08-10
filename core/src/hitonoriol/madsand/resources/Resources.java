package hitonoriol.madsand.resources;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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

import hitonoriol.madsand.Enumerable;
import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.Gui;
import hitonoriol.madsand.containers.AnimationContainer;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.containers.Pair.PairKeyDeserializer;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.category.ItemCategories;
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
			Utils.printMemoryInfo();
		} catch (Exception e) {
			Utils.die("Exception on init: " + ExceptionUtils.getStackTrace(e));
		}
	}

	private static void init() throws Exception {
		Utils.out("Loading resources...");
		Gdx.graphics.setCursor(Gdx.graphics.newCursor(loadPixmap("textures/cursor.png"), 0, 0));
		initObjectMapper();
		Globals.loadGlobals();
		loadMapTiles();
		loadMapObjects();
		loadItems();
		loadItemProducers();
		loadWorldGen();
		loadQuests();
		loadNpcs();
		loadTradeLists();
		loadTutorial();
		loadActionAnimations();
		Globals.values().loadMisc();
		System.gc();
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

	private static void loadTradeLists() {
		NpcProp.tradeLists = load(TRADELIST_FILE, ItemCategories.class);
	}

	private static void loadTutorial() {
		Tutorial.strings = loadMap(TUTORIAL_FILE, String.class, String.class);
	}

	private static void loadQuests() {
		QuestList.quests = loadMap(QUEST_FILE, Integer.class, Quest.class);
		Utils.out(QuestList.quests.size() + " quests");
	}

	private static void loadNpcs() {
		NpcProp.npcs = loadEnumerableMap(NPC_FILE, NpcContainer.class);
		Utils.out(NpcProp.npcs.size() + " NPCs");
	}

	private static void loadMapTiles() {
		TileProp.tiles = loadEnumerableMap(TILE_FILE, Tile.class);
		Utils.out(TileProp.tiles.size() + " tiles");
	}

	private static void loadMapObjects() {
		ObjectProp.objects = loadEnumerableMap(OBJECT_FILE, MapObject.class);
		Utils.out(ObjectProp.objects.size() + " map objects");
	}

	private static void loadItems() {
		ItemProp.items = loadEnumerableMap(ITEM_FILE, Item.class, item -> item.initRecipe());
		Utils.out("%d items (%d craftable)", ItemProp.items.size(), ItemProp.craftReq.size());
	}

	private static void loadWorldGen() {
		WorldGenProp.biomes = loadMap(WORLDGEN_FILE, Integer.class, WorldGenPreset.class);
		WorldGenProp.encounters = loadList(ENCOUNTER_FILE, String.class);
		Utils.out(WorldGenProp.biomes.size() + " biomes");
	}

	private static <T extends Enumerable> Map<Integer, T> loadEnumerableMap(
			String internalFile,
			Class<T> type,
			Consumer<T> initAction) {
		Map<Integer, T> map = loadMap(internalFile, Integer.class, type);
		map.forEach((id, value) -> {
			value.setId(id);
			if (initAction != null)
				initAction.accept(value);
		});
		return map;
	}

	private static <T extends Enumerable> Map<Integer, T> loadEnumerableMap(String internalFile, Class<T> type) {
		return loadEnumerableMap(internalFile, type, null);
	}

	private static void loadItemProducers() {
		ObjectProp.itemProducers = loadMap(ITEMFACTORY_FILE, Integer.class, ItemProducer.class);
		ObjectProp.buildRecipes = loadMap(BUILDRECIPE_FILE, Integer.class, String.class);
		ObjectProp.buildRecipes.forEach((id, recipe) -> ItemProp.buildReq.put(id, Item.parseCraftRequirements(recipe)));
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

	public static <T> T load(String internalFile, Class<T> type) {
		try {
			return mapper.readerFor(type).readValue(readInternal(internalFile));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
		Gui.unfocusGame();
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
			Gui.resumeGameFocus();
		}, Gui.DELAY);
	}
}
