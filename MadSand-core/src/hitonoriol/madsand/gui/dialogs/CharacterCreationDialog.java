package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.widgets.StatLabels;
import hitonoriol.madsand.world.World;

public class CharacterCreationDialog {
	StatLabels statLabels;
	GameDialog dialog;

	public CharacterCreationDialog() {
		statLabels = new StatLabels();
		dialog = new GameDialog(Gui.overlay);
		createCharDialog();
	}

	void rollStats() {
		World.player.stats.roll();
		statLabels.refreshStatLabels();
	}

	void createCharDialog() {
		float width = Gui.defLblWidth;
		rollStats();

		String msg = "Character creation";

		Label title = dialog.getTitleLabel();

		title.setText(msg);
		title.setAlignment(Align.center);
		dialog.setMovable(true);

		final TextField nameField = new TextField("Player", Gui.skin);
		statLabels.refreshStatLabels();
		dialog.add(new Label("\nCharacter name:", Gui.skin)).width(width).row();
		dialog.row();
		dialog.add(nameField).width(width).row();
		dialog.row();
		dialog.add(statLabels.conStatLbl).width(width).row();
		dialog.row();
		dialog.add(statLabels.strStatLbl).width(width).row();
		dialog.row();
		dialog.add(statLabels.accStatLbl).width(width).row();
		dialog.row();
		dialog.add(statLabels.intStatLbl).width(width).row();
		dialog.row();
		dialog.add(statLabels.luckStatLbl).width(width).row();
		dialog.row();
		dialog.add(statLabels.dexStatLbl).width(width).row();
		dialog.row();
		dialog.add(statLabels.statSumLbl).width(width).row();
		TextButton rbtn = new TextButton("Reroll", Gui.skin);
		TextButton cbtn = new TextButton("Create", Gui.skin);
		dialog.add(rbtn).width(width).row();
		dialog.row();
		dialog.add(cbtn).width(width).row();

		cbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				if (!nameField.getText().trim().equals("")) {
					World.player.setName(nameField.getText());
					World.player.reinit();
					dialog.remove();
					Gui.gameUnfocused = false;
					// refreshOverlay();
					LuaUtils.executeScript(LuaUtils.onCreationScript);
				}
			}

		});
		rbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				rollStats();
			}

		});

	}

	public void show() {
		Gui.gameUnfocused = true;
		dialog.show(Gui.overlay);
	}
}
