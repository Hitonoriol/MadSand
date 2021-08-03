package hitonoriol.madsand.map.object;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gui.widgets.waypoint.WaypointArrow;

public class Waypoint extends MapObject {
	public Waypoint(Waypoint protoObject) {
		super(protoObject);
	}

	public Waypoint() {}

	@Override
	public Waypoint copy() {
		return new Waypoint(this);
	}

	public void toggleArrow() {
		Pair position = getPosition();
		WaypointArrow arrow = Gui.overlay.getWaypointArrow(position.x, position.y);
		if (arrow != null)
			arrow.remove();
		else
			Gui.overlay.addWaypointArrow(new WaypointArrow(position.x, position.y).randomizeColor());
	}

	@Override
	public void interact(Player player) {
		interact(player, () -> toggleArrow());
	}
}
