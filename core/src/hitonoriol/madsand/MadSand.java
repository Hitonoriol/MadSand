package hitonoriol.madsand;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.containers.Storage;
import hitonoriol.madsand.dialog.GameTextSubstitutor;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.screens.AbstractScreen;
import hitonoriol.madsand.screens.CraftScreen;
import hitonoriol.madsand.screens.DeathScreen;
import hitonoriol.madsand.screens.GameScreen;
import hitonoriol.madsand.screens.GameWorldRenderer;
import hitonoriol.madsand.screens.MainMenu;
import hitonoriol.madsand.world.World;

public class MadSand extends Game {
	public static final int TILESIZE = 33;

	public static final String SAVE_EXT = ".msf";

	public static final String SAVEDIR = "MadSand_Saves/";
	public static final String MAPDIR = SAVEDIR + "worlds/";
	public static final String PLAYERFILE = "/Player" + SAVE_EXT;
	public static final String LOGFILE = "/log" + SAVE_EXT;
	public static final String NPCSFILE = "NPCs";
	public static final String WORLDFILE = "/World" + SAVE_EXT;
	public static String WORLDNAME = "";

	public static boolean isWorldUntouched = true; // flag for once-per-launch actions

	public static int MAXSAVESLOTS = 10;

	public static SpriteBatch batch;
	public static World world;

	Storage<AbstractScreen<?>> currentScreen = new Storage<>();
	private static MadSand game;
	private static GameWorldRenderer gameWorld;

	public static GameScreen gameScreen;
	public static CraftScreen craftScreen;
	public static DeathScreen deathScreen;
	public static MainMenu mainMenu;

	public void create() {
		Utils.out("Starting initialization!");
		game = this;
		batch = new SpriteBatch();
		gameWorld = new GameWorldRenderer();
		Gdx.graphics.setContinuousRendering(false);

		Timer.instance().start();
		Utils.init();
		Gui.init();
		GameTextSubstitutor.init();
		Keyboard.initKeyListener();

		GameSaver.createDirs();
		initNewGame();
		world.generate();
		initScreens();

		Utils.out("End of initialization!");
	}

	private static void initScreens() {
		gameScreen = new GameScreen(gameWorld);
		craftScreen = new CraftScreen(gameWorld);
		deathScreen = new DeathScreen(gameWorld);
		mainMenu = new MainMenu(gameWorld);
		switchScreen(mainMenu);
	}

	public static void initNewGame() {
		world = new World();
		World.player.updCoords();
		Lua.init();
		Gui.overlay.gameLog.clear();
	}

	public static void switchScreen(Screen screen) {
		if (Gui.gameUnfocused)
			Gui.overlay.gameContextMenu.setVisible(false);
		game.setScreen(screen);
	}

	public static void switchScreen(AbstractScreen<?> screen) {
		game.currentScreen.set(screen);
		switchScreen((Screen) screen);
	}

	public static void reset() {
		Gui.gameResumeFocus();
		switchScreen(gameScreen);
	}

	public void render() {
		Keyboard.pollGlobalHotkeys();
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.render();
	}

	public static void setRenderRadius(int radius) {
		gameWorld.setRenderRadius(radius);
	}

	public static OrthographicCamera getCamera() {
		return gameWorld.getCamera();
	}

	public static Stage getStage() {
		return game.currentScreen.get().getStage();
	}

	public static GameWorldRenderer getRenderer() {
		return gameWorld;
	}

	public static void warn(String msg) {
		Gui.overlay.gameLog.warn(msg);
	}

	public static void print(String arg) {
		Gui.overlay.gameLog.print(arg);
	}

	public static void print(String arg, Object... args) {
		print(String.format(arg, args));
	}

	public static void print(String msg, String color) {
		Gui.overlay.gameLog.print(msg, color);
	}

	public static void notice(String msg) {
		Gui.overlay.gameLog.notice(msg);
	}

	public static void notice(String msg, Object... args) {
		notice(String.format(msg, args));
	}

	// New world is generated on game launch, so this thing ensures that new world won't be generated twice when you press "New Game" button
	public static void worldEntered() {
		if (MadSand.isWorldUntouched)
			MadSand.isWorldUntouched = false;

		world.enter();
	}

	public static void setWorldName(String arg) {
		WORLDNAME = arg;
	}

	public void resume() {}

	public void resize(int width, int height) {
		gameWorld.updateViewport();
		Gui.overlay.getViewport().update(width, height, true);
		Gui.overlay.updateWidgetPositions();
	}

	public void pause() {}

	public void dispose() {
		Utils.out("End");
	}
}