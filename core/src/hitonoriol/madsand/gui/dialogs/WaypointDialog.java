package hitonoriol.madsand.gui.dialogs;

import static hitonoriol.madsand.Gui.*;
import java.util.List;
import java.util.stream.Collectors;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.map.object.Waypoint;

public class WaypointDialog extends GameDialog {
	private Table container = new Table(Gui.skin);
	public WaypointDialog() {
		centerTitle();
		setTitle("Waypoints in this sector");
		makeBordered();
		container.align(Align.topLeft);
		add(new AutoFocusScrollPane(container)).size(ENTRY_WIDTH * 3.1f, 350).row();
		List<Waypoint> waypoints = MadSand.world().getCurLoc()
				.getObjects().stream()
				.map(object -> object.as(Waypoint.class).orElse(null))
				.filter(waypoint -> waypoint != null)
				.collect(Collectors.toList());
		if (waypoints.isEmpty())
			container.add("There are no GPS waypoints in this sector!");
		else {
			container.add(createWaypointEntry()).padBottom(ENTRY_PAD * 2).row();
			waypoints.forEach(waypoint -> container.add(createWaypointEntry(waypoint)).align(Align.left).row());
		}
		skipLine();
		addCloseButton();
	}

	private final static float ENTRY_WIDTH = 225, ENTRY_PAD = 10;

	private Table createWaypointEntry(Waypoint waypoint) {
		Table entry = new Table(Gui.skin);
		entry.defaults().width(ENTRY_WIDTH).height(FONT_S).padRight(ENTRY_PAD);
		/* Create table header */
		if (waypoint == null) {
			setFontSize(entry.add("Name").getActor(), FONT_M);
			setFontSize(entry.add("Position").getActor(), FONT_M);
			setFontSize(entry.add("Toggle navigation").getActor(), FONT_M);
			return entry;
		}
		entry.add(waypoint.name);
		entry.add(waypoint.getPosition().toString());
		TextButton toggleBtn = updateButton(new TextButton("", Gui.skin), waypoint);
		entry.add(toggleBtn).size(ENTRY_WIDTH, BTN_HEIGHT);
		Gui.setAction(toggleBtn, () -> {
			waypoint.toggleArrow();
			updateButton(toggleBtn, waypoint);
			toFront();
		});
		return entry;
	}

	private Table createWaypointEntry() {
		return createWaypointEntry(null);
	}

	private TextButton updateButton(TextButton button, Waypoint waypoint) {
		button.setText(waypoint.hasArrow() ? "Deactivate" : "Activate");
		return button;
	}
}
