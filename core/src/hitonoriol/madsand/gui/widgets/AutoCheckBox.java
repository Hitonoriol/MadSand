package hitonoriol.madsand.gui.widgets;

import java.util.function.Consumer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import hitonoriol.madsand.gui.Gui;

public class AutoCheckBox extends CheckBox {

	public AutoCheckBox(boolean initVal, Consumer<Boolean> refresher) {
		super("", Gui.skin);
		setChecked(initVal);

		super.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				refresher.accept(isChecked());
			}
		});
	}

	public AutoCheckBox(Consumer<Boolean> refresher) {
		this(true, refresher);
	}
}
