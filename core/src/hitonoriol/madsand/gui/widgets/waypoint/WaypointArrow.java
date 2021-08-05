package hitonoriol.madsand.gui.widgets.waypoint;

import static hitonoriol.madsand.MadSand.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.gui.textgenerator.StaticTextGenerator;
import hitonoriol.madsand.gui.widgets.gametooltip.TooltipLabel;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;

public class WaypointArrow extends Group {
	private final static float RADIUS = 100;
	private final static float ANIM_TIME = 0.2f;
	private final static int FONT = Gui.FONT_XS;
	private static final float MIN_DISTANCE = 3;
	private static TextureRegion waypointArrow = Resources.getTexture("gui/arrow");

	private Image arrow = new Image(waypointArrow);
	private String name = "Waypoint";
	private Pair destination = new Pair();
	private Vector2 screenCoords = new Vector2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
	private TooltipLabel descriptionLabel = new TooltipLabel(new StaticTextGenerator((x, y) -> {
		return String.format("%s (%.1f m)", name, toDestination(x, y));
	}));

	public WaypointArrow(int x, int y) {
		arrow.setOrigin(Align.center);
		setDestination(x, y);

		Gui.setFontSize(descriptionLabel, FONT);
		super.setOrigin(Align.center);
		super.addActor(arrow);
		super.addActor(descriptionLabel);
		descriptionLabel.setY(arrow.getY() + arrow.getHeight() + FONT * 1.25f);
	}

	public WaypointArrow() {
		this(0, 0);
	}

	public WaypointArrow setDestinationName(String name) {
		this.name = name;
		return this;
	}

	protected void update(int pX, int pY) {
		double dst = toDestination(pX, pY);
		if (dst <= MIN_DISTANCE) {
			arrow.setVisible(false);
			descriptionLabel.setText(name);
			Vector3 screen = new Vector3(destination.x * MadSand.TILESIZE, destination.y * MadSand.TILESIZE, 0);
			MadSand.getRenderer().getCamera().project(screen);
			descriptionLabel.setX(0);
			addAction(Actions.moveTo(screen.x - getTextOffset(name), screen.y - MadSand.TILESIZE / 2, ANIM_TIME));
			return;
		} else if (!arrow.isVisible())
			arrow.setVisible(true);

		float angle = (float) Math.atan2(destination.y - pY, destination.x - pX);
		float x = screenCoords.x, y = screenCoords.y;

		x += (float) (RADIUS * Math.cos((angle)));
		y += (float) (RADIUS * Math.sin((angle)));

		super.addAction(Actions.moveTo(x, y, ANIM_TIME));
		arrow.addAction(Actions.rotateTo((float) Math.toDegrees(angle), ANIM_TIME));
		descriptionLabel.refresh(pX, pY);
		descriptionLabel.addAction(
				Actions.moveTo(arrow.getX() - getTextOffset(descriptionLabel.getGenerator().getText()),
						descriptionLabel.getY(), 0.1f));
	}

	private float getTextOffset(String text) {
		return Gui.getTextWidth(text, FONT) * 0.33f;
	}

	public double toDestination(int x, int y) {
		return Line.calcDistance(x, y, destination.x, destination.y);
	}

	public Pair getDestination() {
		return destination;
	}

	public WaypointArrow setDestination(int x, int y) {
		destination.set(x, y);
		return this;
	}

	public WaypointArrow setDestination(Pair coords) {
		return setDestination(coords.x, coords.y);
	}

	public WaypointArrow randomizeColor() {
		arrow.setColor(Utils.randomColor(Utils.dataGen));
		return this;
	}
	
	protected Image getArrow() {
		return arrow;
	}

	public void update() {
		update(player().x, player().y);
	}

	@Override
	public String toString() {
		return String.format("%s -> (%s)", name, destination);
	}
}
