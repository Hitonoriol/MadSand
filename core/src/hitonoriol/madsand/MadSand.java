package hitonoriol.madsand;

import java.util.function.Consumer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.containers.Storage;
import hitonoriol.madsand.dialog.GameTextSubstitutor;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.input.Keyboard;
import hitonoriol.madsand.input.Mouse;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.Prefs;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.screens.AbstractScreen;
import hitonoriol.madsand.screens.CraftScreen;
import hitonoriol.madsand.screens.DeathScreen;
import hitonoriol.madsand.screens.GameScreen;
import hitonoriol.madsand.screens.MainMenu;
import hitonoriol.madsand.screens.WorldRenderer;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.World;

public class MadSand extends Game {
	public static final int TILESIZE = 33;

	public static final String SAVE_EXT = ".msf";
	public static final String SAVEDIR = "MadSand_Saves/";
	public static final String MAPDIR = SAVEDIR + "worlds/";
	public static final String LOGFILE = "/log" + SAVE_EXT;
	public static final String NPCSFILE = "NPCs";
	public static final String WORLDFILE = "/World" + SAVE_EXT;
	public static String WORLDNAME = "";

	private static boolean worldUntouched = true;
	private long startTime = System.currentTimeMillis();

	private World world;
	private Storage<AbstractScreen<?>> currentScreen = new Storage<>();
	private static MadSand game;
	private static WorldRenderer worldRenderer;

	public static GameScreen gameScreen;
	public static CraftScreen craftScreen;
	public static DeathScreen deathScreen;
	public static MainMenu mainMenu;

	public void create() {
		Utils.out("Starting initialization!");
		game = this;
		worldRenderer = new WorldRenderer();
		Gdx.graphics.setContinuousRendering(false);

		Timer.instance().start();
		Resources.loadAll();
		Gui.init();
		GameTextSubstitutor.init();
		initScreens();
		Keyboard.initListener();
		Mouse.initListener();

		GameSaver.createDirs();
		initNewGame();
		if (!Globals.headless())
			game.world.generate();
		Keyboard.initDefaultKeyBinds();
		switchScreen(mainMenu);
		Utils.out("End of initialization (%.3f sec spent)", Utils.toSeconds(System.currentTimeMillis() - startTime));
		Utils.printMemoryInfo();
	}

	private static void initScreens() {
		gameScreen = new GameScreen(worldRenderer);
		craftScreen = new CraftScreen(worldRenderer);
		deathScreen = new DeathScreen(worldRenderer);
		mainMenu = new MainMenu(worldRenderer);
	}

	public static void initNewGame() {
		game.createWorld();
		MadSand.player().updCoords();
		Lua.init();
		Gui.overlay.getGameLog().clear();
	}

	private void createWorld() {
		if (world != null)
			world.close();

		world = new World();
	}

	public static void switchScreen(Screen screen) {
		if (Gui.gameUnfocused)
			Gui.overlay.getContextMenu().close();
		game.setScreen(screen);
		System.gc();
	}

	public static void switchScreen(AbstractScreen<?> screen) {
		game.currentScreen.set(screen);
		switchScreen((Screen) screen);
	}

	public static void reset() {
		Gui.forceResumeFocus();
		switchScreen(gameScreen);
	}

	@Override
	public void render() {
		Keyboard.pollGlobalHotkeys();
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.render();
	}

	@Override
	public void resume() {}

	@Override
	public void resize(int width, int height) {
		worldRenderer.updateViewport();
		Gui.overlay.getViewport().update(width, height, true);
		Gui.overlay.updateWidgetPositions();
		player().setFov();
	}

	@Override
	public void pause() {}

	@Override
	public void dispose() {
		Prefs.savePrefs();
		Utils.out("Bye! Session lasted for [%s]", Utils.timeString((long) (Utils.now() - Utils.toSeconds(startTime))));
	}

	public static OrthographicCamera getCamera() {
		return worldRenderer.getCamera();
	}

	public static Stage getStage() {
		return game.currentScreen.get().getStage();
	}

	public static WorldRenderer getRenderer() {
		return worldRenderer;
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public Player getPlayer() {
		return world.getPlayer();
	}

	public static World world() {
		return game.getWorld();
	}

	public static Player player() {
		return game.getPlayer();
	}

	public static void exec(Consumer<World> action) {
		action.accept(world());
	}

	public static void warn(String msg, boolean duplicateAsDialog) {
		Gui.overlay.getGameLog().warn(msg);
		if (duplicateAsDialog)
			Gui.drawOkDialog("Warning", msg);
	}

	public static void warn(String msg) {
		warn(msg, false);
	}

	public static void print(String arg) {
		Gui.overlay.getGameLog().print(arg);
	}

	public static void print(String arg, Object... args) {
		print(String.format(arg, args));
	}

	public static void print(String msg, String color) {
		Gui.overlay.getGameLog().print(msg, color);
	}

	public static void notice(String msg) {
		Gui.overlay.getGameLog().notify(msg);
	}

	public static void notice(String msg, Object... args) {
		notice(String.format(msg, args));
	}

	// New world is generated on game launch, so this thing ensures that new world won't be generated twice when you press "New Game" button
	public static void enterWorld() {
		if (MadSand.isWorldUntouched())
			MadSand.worldUntouched = false;

		Gui.overlay.setPlayer(player());
		game.world.enter();
		System.gc();
	}

	public static boolean isWorldUntouched() {
		return worldUntouched;
	}

	public static void setWorldName(String arg) {
		WORLDNAME = arg;
	}

	public static MadSand instance() {
		return game;
	}
}