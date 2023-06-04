package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;

import hitonoriol.madsand.util.Utils;

public class AutoFocusScrollPane extends ScrollPane {

	public AutoFocusScrollPane(Actor actor) {
		super(actor);
		setScrollingDisabled(true, false);
		addListener(new ScrollFocusListener(this));
		setWidth(actor.getWidth());
	}
	
	@Override
	protected void sizeChanged() {
		var actor = getActor();
		if (actor != null) {
			Utils.out("ScrollPane: Updating child width: %f -> %f", actor.getWidth(), getWidth());
			actor.setWidth(getWidth());
		}
		super.sizeChanged();
	}
}