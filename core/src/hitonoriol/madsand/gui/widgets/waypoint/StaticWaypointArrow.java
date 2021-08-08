package hitonoriol.madsand.gui.widgets.waypoint;

public class StaticWaypointArrow extends WaypointArrow {
	private final static int REMOVE_DST = 2; 
	public StaticWaypointArrow(int x, int y) {
		super(x, y);
		randomizeColor();
	}

	@Override
	protected void update(int pX, int pY) {
		if (toDestination(pX, pY) < REMOVE_DST) {
			remove();
			return;
		}

		super.update(pX, pY);
	}
}
