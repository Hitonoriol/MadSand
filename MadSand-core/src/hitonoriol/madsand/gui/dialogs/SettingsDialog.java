package hitonoriol.madsand.gui.dialogs;

import java.io.File;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;

public class SettingsDialog extends Dialog {
	Skin skin = Gui.skin;

	public SettingsDialog() {
		super("", Gui.skin);
		createDialog();
	}

	void createDialog() {
		int radius = 12;
		float width = Gui.defLblWidth;

		super.text("\nSettings");
		super.row();
		super.align(Align.center);
		final Label renderv = new Label("", skin);
		final Slider renderslide = new Slider(5, 100, 1, false, skin);
		if (new File("MadSand_Saves/lastrend.dat").exists())
			radius = (Integer.parseInt(GameSaver.getExternal("lastrend.dat")));
		renderslide.setValue(radius);
		renderv.setText("Render radius (" + (int) renderslide.getValue() + ")");
		TextButton cbtn = new TextButton("Set", skin);
		TextButton cancel = new TextButton("Cancel", skin);

		super.add(renderv).row();
		super.add(renderslide).width(width).row();

		super.add(cbtn).width(width).row();
		super.add(cancel).width(width).row();

		renderslide.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				renderv.setText("Render radius (" + (int) renderslide.getValue() + ")");
			}
		});
		cbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.setRenderRadius(Math.round(renderslide.getValue()));
				GameSaver.saveToExternal("lastrend.dat", Math.round(renderslide.getValue()) + "");
				// TODO
				remove();
			}

		});
		cancel.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				remove();
			}

		});
	}
	
	public void show() {
		super.show(Gui.mainMenu);
	}
}
