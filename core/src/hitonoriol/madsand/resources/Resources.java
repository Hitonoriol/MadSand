package hitonoriol.madsand.resources;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Queue;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.quest.Quest;
import hitonoriol.madsand.gui.Gui;
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
	private static final SimpleDateFormat accurateDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH'h'mm'm'-ss's'SSS");
	private static final String QUEST_FILE = "quests.json";
	private static final String WORLDGEN_FILE = "worldgen.json";
	private static final String ENCOUNTER_FILE = "encounters.json";
	private static final String TUTORIAL_FILE = "tutorial.json";
	private static final String TILE_FILE = "tiles.json";
	private static final String OBJECT_FILE = "objects.json";
	private static final String ITEMFACTORY_FILE = "itemfactories.json";
	private static final String NPC_FILE = "npcs.json";
	private static final String ITEM_FILE = "items.json";
	public static final String GLOBALS_FILE = "globals.json";
	public static final String BUILDRECIPE_FILE = "buildrecipes.json";
	public static final String SKILL_FILE = "skills.json";
	public static final String SCRIPT_DIR = "scripts/";
	public static final String ENCOUNTER_DIR = "encounter/";
	public static final String OUT_FILE = String.format("MadSand-%s.log", accurateDateFormat.format(Calendar.getInstance().getTime()));
	
	public static final int TILESIZE = 33;

	private static final TextureAtlas textures = loadAtlas("textures");
	private static TextureMap<String> textureMap = new TextureMap<>(textures);
	private static TextureMap<Integer> tiles = new TextureMap<>(textures, "terrain");
	private static TextureMap<Integer> objects = new TextureMap<>(textures, "obj");
	private static TextureMap<Integer> items = new TextureMap<>(textures, "inv");
	private static TextureMap<Integer> npcs = new TextureMap<>(textures, "npc");

	public static final String emptyField = "-1";
	public static final int emptyId = -1;
	public static final String Space = " ", Colon = ":";
	public static final String Tab = "    ";
	public static final String LINEBREAK = Character.toString('\n');
	public static final String COLOR_END = "[]";

	private final static Queue<Runnable> initQueue = new ArrayDeque<>();
	private final static Serializer loader = new Serializer();

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
		Globals.loadGlobals();
		loadMapTiles();
		loadMapObjects();
		loadItems();
		loadItemProducers();
		loadWorldGen();
		loadQuests();
		loadNpcs();
		loadTutorial();
		Globals.values().loadMisc();
		System.gc();
		Utils.out("Done loading resources.");
	}

	public static void deferInit(Runnable initTask) {
		initQueue.add(initTask);
	}

	private static void finalizeInit() {
		initQueue.forEach(action -> action.run());
	}

	private static void loadTutorial() {
		Tutorial.strings = loader.loadMap(TUTORIAL_FILE, String.class, String.class);
	}

	private static void loadQuests() {
		QuestList.quests = loader.loadEnumerableMap(QUEST_FILE, Quest.class);
		Utils.out(QuestList.quests.size() + " quests");
	}

	private static void loadNpcs() {
		NpcProp.npcs = loader.loadEnumerableMap(NPC_FILE, NpcContainer.class);
		Utils.out(NpcProp.npcs.size() + " NPCs");
	}

	private static void loadMapTiles() {
		TileProp.tiles = loader.loadEnumerableMap(TILE_FILE, Tile.class);
		Utils.out(TileProp.tiles.size() + " tiles");
	}

	private static void loadMapObjects() {
		ObjectProp.objects = loader.loadEnumerableMap(OBJECT_FILE, MapObject.class);
		Utils.out(ObjectProp.objects.size() + " map objects");
	}

	private static void loadItems() {
		ItemProp.items = loader.loadEnumerableMap(ITEM_FILE, Item.class);
		finalizeInit();
		ItemProp.items.forEach((id, item) -> {
			item.initRecipe();
			item.initCategory();
		});
		Utils.out("%d items (%d craftable)", ItemProp.items.size(), ItemProp.craftReq.size());
	}

	private static void loadWorldGen() {
		WorldGenProp.biomes = loader.loadMap(WORLDGEN_FILE, Integer.class, WorldGenPreset.class);
		WorldGenProp.encounters = loader.loadList(ENCOUNTER_FILE, String.class);
		Utils.out(WorldGenProp.biomes.size() + " biomes");
	}

	private static void loadItemProducers() {
		ObjectProp.itemProducers = loader.loadMap(ITEMFACTORY_FILE, Integer.class, ItemProducer.class);
		ObjectProp.buildRecipes = loader.loadMap(BUILDRECIPE_FILE, Integer.class, String.class);
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

	public static Serializer loader() {
		return loader;
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
			PixmapIO.writePNG(Gdx.files.local(Utils.now(accurateDateFormat) + ".png"), pixmap);
			pixmap.dispose();
			Gui.resumeGameFocus();
		}, Gui.DELAY);
	}
}
