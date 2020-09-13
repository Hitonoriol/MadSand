package hitonoriol.madsand.gui.dialogs;

import java.io.File;
import java.io.FilenameFilter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.enums.GameState;

public class LoadWorldDialog extends Dialog {

	Skin skin = Gui.skin;

	public LoadWorldDialog() {
		super("", Gui.skin);
		createDialog();
	}

	void createDialog() {
		float width = Gui.defLblWidth;
		File file = new File(MadSand.MAPDIR);

		String[] dirs = file.list(new FilenameFilter() {
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		super.text("\nLoad game:\n");
		super.row();
		int i = 0;
		int slots = 0;
		try {
			slots = dirs.length;
		} catch (Exception e) {

		}

		if (slots > MadSand.MAXSAVESLOTS)
			slots = MadSand.MAXSAVESLOTS;

		TextButton[] ldbtn = new TextButton[slots];
		while (i < slots) {
			ldbtn[i] = new TextButton(dirs[i], skin);
			super.add(ldbtn[i]).width(Gdx.graphics.getWidth() / 2).row();
			final String sa = dirs[i];
			ldbtn[i].addListener(new ChangeListener() {
				public void changed(ChangeListener.ChangeEvent event, Actor actor) {
					MadSand.WORLDNAME = sa;

					if (GameSaver.loadWorld(sa))
						MadSand.state = GameState.GAME;

				}
			});
			i++;
		}
		TextButton cbtn = new TextButton("Cancel", skin);

		if (slots == 0)
			super.add(new TextButton("No worlds to load", skin)).width(width).row();

		super.add(cbtn).width(width).row();
		super.add(new Label("\n", skin)).width(width).row();
		cbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				remove();

			}

		});
	}

	public void show() {
		super.show(Gui.mainMenu);
	}
}
