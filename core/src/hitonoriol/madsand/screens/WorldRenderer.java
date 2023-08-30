package hitonoriol.madsand.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.crashinvaders.vfx.effects.ChromaticAberrationEffect;
import com.crashinvaders.vfx.effects.FilmGrainEffect;
import com.crashinvaders.vfx.effects.RadialDistortionEffect;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gamecontent.Textures;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.animation.WorldAnimation;
import hitonoriol.madsand.input.Mouse;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.pathfinding.Path;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.resources.TextureMap;
import hitonoriol.madsand.util.CameraShaker;
import hitonoriol.madsand.vfx.BlackAndWhiteEffect;
import hitonoriol.madsand.vfx.ShaderManager;

public class WorldRenderer {
	public static final float TARGET_FRAME_DELTA = 1f / 60f;
	public static final float MIN_ZOOM = 0.5f, MAX_ZOOM = 4.0f, DEFAULT_ZOOM = 1.5F;
	private static final int OBJECT_LOOT = 7;
	private static final int CAM_OFFSET_X = 0, CAM_OFFSET_Y = 37;

	private float cameraX, cameraY;
	private boolean followPlayer;
	private float zoom = DEFAULT_ZOOM;

	private SpriteBatch batch = new SpriteBatch();
	private OrthographicCamera camera = new OrthographicCamera();
	private CameraShaker shaker = new CameraShaker(camera);
	private static final float cameraRadius = 300;
	private static final float offsetFactor = 0.3f;
	private boolean enableFloatingCamera = false;
	private float frameDelta = 0;
	
	private ShaderManager shaderManager = new ShaderManager(batch);
	private RadialDistortionEffect radialDistortion = new RadialDistortionEffect();
	
	private LightMap light = new LightMap();
	private List<WorldAnimation> animations = new ArrayList<>();
	private ConcurrentHashMap<Path, PathDescriptor> paths = new ConcurrentHashMap<>();
	
	private TextureRegion mapCursor = Textures.getTexture("misc/map_cursor");

	public WorldRenderer() {
		updateViewport();
		
		radialDistortion.setDistortion(0.1f);
		radialDistortion.setZoom(0.85f);
		shaderManager.addEffect(radialDistortion, true);
		shaderManager.addEffect(new BlackAndWhiteEffect());
		var chromaEffect = new ChromaticAberrationEffect(12);
		chromaEffect.setMaxDistortion(0.09f);
		shaderManager.addEffect(chromaEffect, true);
		shaderManager.addEffect(new FilmGrainEffect());
	}

	private void renderObject(MapObject object, float x, float y) {
		x *= Resources.TILESIZE;
		y *= Resources.TILESIZE;

		if (object.centered)
			x -= object.getRenderOffset();

		var texture = object.getTexture();
		float w = texture.getRegionWidth(), h = texture.getRegionHeight();
		if ((object.id() != MapObject.NULL_OBJECT_ID) && (object.id() != MapObject.COLLISION_MASK_ID))
			batch.draw(
				texture, x, y, w / 2f, h / 2f, w, h, 1f, 1f,
				object.getDirection().getRotation()
			);
	}

	public void queueAnimation(WorldAnimation animation) {
		animations.add(animation);
	}

	public void queuePath(Path path, float duration, Color color) {
		paths.put(path, new PathDescriptor(duration, color));
	}

	public void queuePath(Path path, Color color) {
		queuePath(path, Float.MAX_VALUE, color);
	}

	public void queuePath(Path path) {
		paths.put(path, PathDescriptor.DEFAULT);
	}

	public void removePath(Path path) {
		if (path != null)
			paths.remove(path);
	}

	public boolean isPathQueued(Path path) {
		return paths.containsKey(path);
	}

	private void drawPaths() {
		paths.forEach((path, duration) -> {
			var pInfo = paths.get(path);
			drawPath(path, pInfo.color);

			if (pInfo.noTimeLimit())
				return;

			float time = pInfo.render(frameDelta);

			if (time <= 0)
				paths.remove(path);
		});
	}

	private void drawAnimations() {
		if (animations.isEmpty())
			return;

		var it = animations.iterator();
		WorldAnimation animation;
		while (it.hasNext()) {
			animation = it.next();
			batch.draw(animation.getCurrentKeyFrame(), animation.x(), animation.y());

			if (animation.isAnimationFinished())
				it.remove();
		}
	}

	private static final float LOOT_SIZE = Resources.TILESIZE * 0.85f;
	private static final float LOOT_OFFSET = (Resources.TILESIZE - LOOT_SIZE) * 0.5f;

	private void drawLoot(float x, float y) {
		var loot = MadSand.world().getCurLoc().getLoot((int) x, (int) y);

		x = x * Resources.TILESIZE + LOOT_OFFSET;
		y = y * Resources.TILESIZE + LOOT_OFFSET;

		if (loot.getItemCount() == 1)
			batch.draw(
				loot.get(0).getTexture(),
				x, y, LOOT_SIZE, LOOT_SIZE
			);
		else
			batch.draw(
				Textures.getObject(OBJECT_LOOT),
				x, y, LOOT_SIZE, LOOT_SIZE
			);

	}

	private void drawEntity(Entity entity) {
		var drawPos = entity.getScreenPosition();
		if (entity.isMoving())
			entity.animateMovement();

		batch.draw(entity.getSprite(), drawPos.x, drawPos.y);

		if (followPlayer && entity instanceof Player) {
			if (enableFloatingCamera && !Gui.isDialogActive()) {
				var offset = new Vector3(
					offsetFactor * (Mouse.worldX() - drawPos.x),
					offsetFactor * (Mouse.worldY() - drawPos.y), 0
				);
				camera.position.set(offset.x + drawPos.x, offset.y + drawPos.y, 0);
				if (Line.calcDistance(camera.position.x, camera.position.y, drawPos.x, drawPos.y) > cameraRadius) {
					offset.nor();
					camera.position.set(offset.x * cameraRadius + drawPos.x, offset.y * cameraRadius + drawPos.y, 0);
				}
				setCamPosition(camera.position.x, camera.position.y);
			} else
				setCamPosition(drawPos.x, drawPos.y);
		}
	}

	private static Color defColor = new Color(1, 1, 1, 1);

	public void drawPath(Path path, Color color) {
		batch.setColor(color);
		path.forEach(cell -> batch.draw(mapCursor, cell.x * Resources.TILESIZE, cell.y * Resources.TILESIZE));
		batch.setColor(defColor);
	}

	public void drawPath(Path path) {
		drawPath(path, defColor);
	}

	private void drawMapCursor() {
		if (!Mouse.hasClickAction())
			batch.draw(mapCursor, Mouse.wx * Resources.TILESIZE, Mouse.wy * Resources.TILESIZE);
		else
			drawPath(Mouse.getPathToCursor());
	}

	private void darkenTile(int x, int y, int dstFromLight) {
		batch.draw(light.get(dstFromLight), x * Resources.TILESIZE, y * Resources.TILESIZE);
	}

	private void drawGame() {
		var map = MadSand.world().getCurLoc();
		var player = MadSand.player();

		if (player.isInBackground()) // Draw player under tiles & objects if he is currently in the background
			drawEntity(player);

		player.forEachInFov((x, y) -> { // Render background tiles
			var tile = map.getTile(x, y);

			if (!tile.visible() && !tile.visited) //Don't render tiles which were never seen
				return;

			if (!map.validCoords(x, y) && MadSand.world().isUnderGround()) // Don't render default tile while underground
				return;

			batch.draw(MadSand.world().getTileOrDefault(x, y), x * Resources.TILESIZE, y * Resources.TILESIZE);
		});

		player.forEachInFov((x, y) -> { // Render objects, loot, NPCs and player
			var tile = map.getTile(x, y);
			var object = map.getObject(x, y);
			var npc = map.getNpc(x, y);
			boolean tileVisited = tile.visited && !tile.visible();

			if (!map.validCoords(x, y) && MadSand.world().isUnderGround())
				return;

			/* Non-visible NPCs won't be rendered, so only process their movement queue */
			if (!tile.visible() && !npc.isEmpty())
				npc.animateMovement();

			if (!tile.visible() && !tile.visited) //Don't render anything on tiles which were never seen
				return;

			if (tileVisited && object.isWall) // If object is a wall, it'll be rendered even when not visible
				renderObject(object, x, y);

			if (tileVisited) { // Render visited & not currently visible tiles partially darkened
				darkenTile(x, y, tile.getLightLevel());
				return;
			}

			renderObject(object, x, y);

			if (player.standingOnLoot(x, y))
				drawLoot(x, y);

			if (!npc.isEmpty())
				drawEntity(npc);

			if (player.at(x, y) && !player.isInBackground())
				drawEntity(player);
		});

		drawAnimations();

		if (!Gui.isGameUnfocused())
			drawMapCursor();
		drawPaths();
	}

	public void render(float delta) {
		frameDelta = delta;
		shaderManager.begin();
		drawGame();
		shaderManager.end();
		updateCamPosition();
	}

	public OrthographicCamera getCamera() {
		return camera;
	}

	public float getCamX() {
		return cameraX;
	}

	public float getCamY() {
		return cameraY;
	}

	public float getCamZoom() {
		return zoom;
	}

	public void setCamFollowPlayer(boolean follow) {
		followPlayer = follow;
	}

	public void moveCamera(float byX, float byY) {
		cameraX += byX;
		cameraY += byY;
	}

	public void changeZoom(float by) {
		zoom += by;
		if (zoom < MIN_ZOOM)
			zoom = MIN_ZOOM;
		if (zoom > MAX_ZOOM)
			zoom = MAX_ZOOM;
		updateViewport();
	}

	public void setZoom(float val) {
		zoom = val;
		updateViewport();
	}

	public void setCamPosition(float x, float y) {
		cameraX = x;
		cameraY = y;
	}

	public void updateCamPosition(float x, float y) {
		camera.position.set(x + CAM_OFFSET_X, y + CAM_OFFSET_Y, 0.0F);
		shaker.tick(frameDelta);
		batch.setProjectionMatrix(camera.combined);
		camera.update();
	}

	public void setWorldCamPosition(int x, int y) {
		updateCamPosition(x * Resources.TILESIZE, y * Resources.TILESIZE);
	}

	public void updateCamPosition() {
		updateCamPosition(cameraX, cameraY);
	}

	public void updateViewport() {
		camera.viewportWidth = Gdx.graphics.getWidth() / zoom;
		camera.viewportHeight = Gdx.graphics.getHeight() / zoom;
		camera.update();
	}

	public void shakeCamera(float intensity, float duration) {
		shaker.shake(intensity, duration);
	}

	public void enableFloatingCamera(boolean enable) {
		enableFloatingCamera = enable;
	}

	public boolean isFloatingCameraEnbled() {
		return enableFloatingCamera;
	}
	
	public ShaderManager getShaderManager() {
		return shaderManager;
	}

	private static class LightMap extends TextureMap<Integer> {
		private final static float LIGHT_START = 0.16f, ALPHA_STEP = 0.055f;
		private final static int LIGHT_LEVELS = (int) ((1f - LIGHT_START) / ALPHA_STEP);
		/* How often <n> repeats */
		private final static IntUnaryOperator lightLevelFunction = n -> (int) Math.log(1.2 * n);
		private final static int MAX_DISTANCE = IntStream.range(1, LIGHT_LEVELS + 1)
			.map(lightLevelFunction)
			.sum();
		private final static java.util.Map<Integer, Integer> lightLvlMap = new HashMap<>();

		private final static String REGION = "light";

		private LightMap() {
			super(Textures.get(), REGION);
			createLightLevels();

			for (int dst = 1, lightLvl = 1, occurences = 0; dst <= MAX_DISTANCE; ++dst) {
				lightLvlMap.put(dst, lightLvl);
				if (lightLvl < LIGHT_LEVELS && ++occurences >= lightLevelFunction.applyAsInt(lightLvl)) {
					++lightLvl;
					occurences = 0;
				}
			}
		}

		private void createLightLevels() {
			var color = new Color(Color.BLACK);
			for (int lvl = 1; lvl <= LIGHT_LEVELS; ++lvl) {
				var light = new Pixmap(Resources.TILESIZE, Resources.TILESIZE, Format.RGBA8888);
				color.a = Math.min(LIGHT_START + ALPHA_STEP * (lvl - 1), 1f);
				light.setColor(color);
				light.fill();
				atlas.addRegion(REGION + "/" + lvl, new TextureRegion(new Texture(light)));
			}
		}

		@Override
		public AtlasRegion get(Integer distance) {
			return super.get(lightLvlMap.get(Math.max(1, Math.min(distance, MAX_DISTANCE))));
		}
	}

	@SuppressWarnings("unused")
	private static class PathDescriptor {
		float duration = Float.MAX_VALUE;
		Color color = defColor;

		static PathDescriptor DEFAULT = new PathDescriptor();

		public PathDescriptor(float duration, Color color) {
			this.duration = duration;
			this.color = color;
		}

		public PathDescriptor(Color color) {
			this.color = color;
		}

		public PathDescriptor() {}

		boolean noTimeLimit() {
			return duration == Float.MAX_VALUE;
		}

		float render(float delta) {
			duration -= delta;
			return duration;
		}
	}
	
	public Vector2 projectScreenToWorld(Vector3 screenCoordsInTmp, Vector2 worldCoordsOut) {
		if (!radialDistortion.isDisabled()) {
			RadialDistortion.apply(screenCoordsInTmp, radialDistortion.getDistortion(), radialDistortion.getZoom());
		}
		
		camera.unproject(screenCoordsInTmp);
		return worldCoordsOut.set(screenCoordsInTmp.x, screenCoordsInTmp.y);
	}
	
	public Vector2 projectWorldToScreen(Vector3 worldCoordsInTmp, Vector2 screenCoordsOut) {
		camera.project(worldCoordsInTmp);
		screenCoordsOut.set(worldCoordsInTmp.x, worldCoordsInTmp.y);
		
		if (!radialDistortion.isDisabled()) {
			RadialDistortion.apply(worldCoordsInTmp, radialDistortion.getDistortion(), radialDistortion.getZoom());
		}
		
		return screenCoordsOut.set(worldCoordsInTmp.x, worldCoordsInTmp.y);
	}
	
	// Apply the same logic from the RadialDistortionEffect shader to screen coordinates
	private static class RadialDistortion {
	    static void screenToUV(Vector3 screenCoords) {
	        float uvX = screenCoords.x / Gui.screenWidth();
	        float uvY = 1.0f - screenCoords.y / Gui.screenHeight();
	        screenCoords.set(uvX, uvY, 0);
	    }
	    
	    static void uvToScreen(Vector3 uv) {
	    	float screenX = uv.x * Gui.screenWidth();
	        float screenY = (1.0f - uv.y) * Gui.screenHeight();
	        uv.set(screenX, screenY, 0);
	    }

	    static final Vector3 half = new Vector3(0.5f, 0.5f, 0f);
	    static Vector3 apply(Vector3 screenCoordsInTmp, float distortion, float zoom) {
	    	screenToUV(screenCoordsInTmp);
	        var cc = screenCoordsInTmp.cpy().sub(half); 		 // vec2 cc = coord - 0.5;
	        float dist = cc.dot(cc) * distortion; 				 // float dist = dot(cc, cc) * distortion;
	        screenCoordsInTmp.add(cc.scl((1.0f + dist) * dist)); // return (coord + cc * (1.0 + dist) * dist);
	        screenCoordsInTmp.sub(half).scl(zoom).add(half); 	 // uv = 0.5 + (uv-0.5)*(zoom);
	        uvToScreen(screenCoordsInTmp);
	        return screenCoordsInTmp;
	    }
	}
}
