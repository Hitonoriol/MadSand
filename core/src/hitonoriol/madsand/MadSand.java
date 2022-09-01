package hitonoriol.madsand;

import java.util.Optional;
import java.util.function.Consumer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.containers.Storage;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gui.Gui;
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
import hitonoriol.madsand.screens.TravelScreen;
import hitonoriol.madsand.screens.WorldRenderer;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.util.cast.Cast;
import hitonoriol.madsand.world.World;

public class MadSand extends Game {
	private static MadSand game;

	private World world;
	private WorldRenderer worldRenderer;
	private Storage<AbstractScreen<?>> currentScreen = new Storage<>();

	private boolean worldUntouched = true;
	private long startTime = System.currentTimeMillis();

	public void create() {
		Utils.out("Starting initialization!");
		game = this;
		Gdx.graphics.setContinuousRendering(false);
		Timer.instance().start();
		Resources.loadAll();
		Gui.init();
		initScreens();
		Keyboard.initListener();
		Mouse.initListener();
		initNewGame();
		Keyboard.initDefaultKeyBinds();
		switchScreen(Screens.MainMenu);
		Utils.out("End of initialization (%.3f sec spent)", Utils.toSeconds(System.currentTimeMillis() - startTime));
		Utils.printMemoryInfo();
	}

	private void initScreens() {
		worldRenderer = new WorldRenderer();
		Screens.MainMenu.set(new MainMenu(worldRenderer));
		Screens.Game.set(new GameScreen(worldRenderer));
		Screens.Crafting.set(new CraftScreen(worldRenderer));
		Screens.Death.set(new DeathScreen(worldRenderer));
		Screens.Travel.set(new TravelScreen());
	}

	public static void initNewGame() {
		game.createWorld();
		MadSand.player().updCoords();
		Lua.init();
		Gui.overlay.getGameLog().clear();
		if (!Globals.headless() && isWorldUntouched())
			game.world.generate();
	}

	private void createWorld() {
		if (world != null)
			world.close();

		world = new World();
	}

	public static void switchScreen(Screen screen) {
		Gui.overlay.getContextMenu().close();
		game.setScreen(screen);
		System.gc();
	}

	public static void switchScreen(AbstractScreen<?> screen) {
		game.currentScreen.set(screen);
		switchScreen((Screen) screen);
	}

	public static void switchScreen(Screens screen) {
		switchScreen(screen.screen());
	}

	public static void reset() {
		Gui.resetGameFocus();
		switchScreen(Screens.Game);
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
		return game.worldRenderer.getCamera();
	}

	public static Stage getStage() {
		return game.currentScreen.get().getStage();
	}

	public static WorldRenderer getRenderer() {
		return game.worldRenderer;
	}
	
	public static float toScreen(float value) {
		return value * game.worldRenderer.getCamZoom();
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
		if (isWorldUntouched())
			game.worldUntouched = false;

		Gui.overlay.setPlayer(player());
		game.world.enter();
		System.gc();
	}

	public static boolean isWorldUntouched() {
		return game.worldUntouched;
	}

	public static MadSand game() {
		return game;
	}

	public enum Screens {
		MainMenu, Game, Crafting, Death, Travel;

		private AbstractScreen<?> screen;

		private void set(AbstractScreen<?> screen) {
			this.screen = screen;
		}

		public AbstractScreen<?> screen() {
			return screen;
		}

		public <T extends AbstractScreen<?>> Optional<T> screen(Class<T> screenClass) {
			return Cast.to(screen(), screenClass);
		}

		public Stage stage() {
			return screen.getStage();
		}

		public <T extends Stage> Optional<T> stage(Class<T> stageClass) {
			return Cast.to(stage(), stageClass);
		}
	}
}