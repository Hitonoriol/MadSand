package hitonoriol.madsand.map.object;

import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.dialogs.InputDialog;
import hitonoriol.madsand.gui.widgets.overlay.GameContextMenu;
import hitonoriol.madsand.gui.widgets.waypoint.WaypointArrow;

public class Waypoint extends MapObject {
	private Pair position;

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
		WaypointArrow arrow = getArrow();
		if (arrow != null) {
			arrow.remove();
			Gui.refreshOverlay();
		} else
			Gui.overlay.addWaypointArrow(new WaypointArrow(position.x, position.y)
					.setDestinationName(getName())
					.randomizeColor());
	}

	public WaypointArrow getArrow() {
		Pair position = getPosition();
		return Gui.overlay.getWaypointArrow(position.x, position.y);
	}

	public boolean hasArrow() {
		return getArrow() != null;
	}

	@Override
	protected void populateContextMenu(GameContextMenu menu) {
		menu.addButton("Rename", () -> new InputDialog("Rename Waypoint", newName -> name = newName)
				.setInitialText(getName())
				.show());
	}

	@Override
	public Pair getPosition() {
		if (position == null)
			return position = super.getPosition();
		return position;
	}

	@Override
	public void interact(Player player) {
		interact(player, () -> toggleArrow());
	}
}
