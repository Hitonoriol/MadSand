package hitonoriol.madsand.resources;

import java.util.function.Consumer;
import java.lang.Character;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gamecontent.Textures;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.util.Log;
import hitonoriol.madsand.util.TimeUtils;
import hitonoriol.madsand.util.Utils;

public class Resources {
	public static final String SCRIPT_DIR = "scripts/";
	public static final String ENCOUNTER_DIR = "encounter/";
	public static final int TILESIZE = 33;

	private static final GameAssetManager assetManager = new GameAssetManager();
	private static final Serializer loader = new Serializer();

	public static final String emptyField = "-1";
	public static final int emptyId = -1;
	public static final String Space = " ", Colon = ":";
	public static final String Tab = "    ";
	public static final String LINEBREAK = Character.toString('\n');
	public static final String COLOR_END = "[]";

	public static GameAssetManager loadAll() {
		try {
			Utils.out("Loading game content...");
			Gdx.graphics.setCursor(Gdx.graphics.newCursor(loadPixmap("textures/cursor.png"), 0, 0));
			GuiSkin.get();
			var backgrounds = loader.loadList("textures/loading/backgrounds.json", String.class);
			var loadingScreen = assetManager.createLoadingScreen();
			loadingScreen.getStage().setBackground(loadTexture(Utils.randElement(backgrounds)));
			MadSand.switchScreen(loadingScreen);
			Content.asList().forEach(Resources::load);
			Utils.printMemoryInfo();
			return assetManager;
		} catch (Exception e) {
			Utils.die(e);
			return null;
		}
	}

	private static void load(Content asset) {
		assetManager.load(asset.descriptor());
	}

	static final String FONT_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;:,{}\"'<>";
	static final String FONT_PATH = "fonts/8bitoperator.ttf";

	public static BitmapFont createFont(int size) {
		var font = new BitmapFont();
		var generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_PATH));
		var param = new FreeTypeFontGenerator.FreeTypeFontParameter();
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

	private static String getAtlasPath(String name) {
		return "textures/" + name + ".atlas";
	}

	public static TextureAtlas loadAtlas(String name) {
		return assetManager.loadAndWait(getAtlasPath(name), TextureAtlas.class);
	}

	public static void loadAtlas(String name, Consumer<TextureAtlas> afterLoading) {
		assetManager.loadAndThen(getAtlasPath(name), TextureAtlas.class, afterLoading::accept);
	}

	public static String readInternal(String file) {
		return Gdx.files.internal(file).readString();
	}

	public static Serializer loader() {
		return loader;
	}

	public static GameAssetManager manager() {
		return assetManager;
	}

	public static NinePatchDrawable loadNinePatch(String file) {
		return new NinePatchDrawable(new NinePatch(Textures.getTexture(file)));
	}

	public static Texture loadTexture(String file) {
		return new Texture(Gdx.files.internal("textures/" + file));
	}

	public static Pixmap loadPixmap(String file) {
		return new Pixmap(Gdx.files.internal(file));
	}

	public static void takeScreenshot(boolean withGui) {
		Gui.unfocusGame();
		MadSand.player().setFov(
			(int) (1.25f * (Gdx.graphics.getWidth() / (Resources.TILESIZE * MadSand.getRenderer().getCamZoom())))
		);
		if (!withGui)
			Gui.overlay.setVisible(false);
		TimeUtils.scheduleTask(() -> {
			byte[] pixels = ScreenUtils.getFrameBufferPixels(
				0, 0, Gdx.graphics.getBackBufferWidth(),
				Gdx.graphics.getBackBufferHeight(), true
			);

			for (int i = 4; i < pixels.length; i += 4)
				pixels[i - 1] = (byte) 255;

			var pixmap = new Pixmap(
				Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(),
				Pixmap.Format.RGBA8888
			);

			BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
			PixmapIO.writePNG(Gdx.files.local(Utils.now(Log.getAccurateDateFormat()) + ".png"), pixmap);
			pixmap.dispose();
			if (!withGui)
				Gui.overlay.setVisible(true);
			MadSand.player().setFov();
			Gui.resumeGameFocus();
		}, Gui.DELAY);
	}
}
