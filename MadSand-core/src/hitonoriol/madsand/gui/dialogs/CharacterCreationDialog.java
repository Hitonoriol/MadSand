package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.dialog.GameTextSubstitutor;
import hitonoriol.madsand.entities.Stats;
import hitonoriol.madsand.gui.widgets.StatLabels;
import hitonoriol.madsand.world.World;

public class CharacterCreationDialog {
	StatLabels statLabels;
	Stats stats;
	PlayerStatDialog dialog;
	static String titleString = "Character Creation";

	public CharacterCreationDialog() {
		statLabels = new StatLabels();
		stats = statLabels.stats;
		createCharDialog();
	}

	void rollStats() {
		stats.roll();
		statLabels.refreshStatLabels();
	}

	void createCharDialog() {
		float width = Gui.defLblWidth;
		rollStats();
		dialog = new PlayerStatDialog(Gui.overlay, statLabels, titleString);
		
		TextButton rbtn = new TextButton("Reroll", Gui.skin);
		TextButton cbtn = new TextButton("Create", Gui.skin);
		dialog.add(rbtn).width(width).row();
		dialog.row();
		dialog.add(cbtn).width(width).row();

		cbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				
				if (stats.maxStatSum - stats.getSum() > 0) {
					Gui.drawOkDialog("You still have unassigned stat points left!", Gui.overlay);
					return;
				}
				
				if (!dialog.nameField.getText().trim().equals("")) {
					World.player.setName(dialog.nameField.getText());
					World.player.reinit();
					dialog.remove();
					Gui.gameUnfocused = false;
					Gui.refreshOverlay();
					GameTextSubstitutor.add(GameTextSubstitutor.PLAYER_NAME, dialog.nameField.getText());
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
		dialog.show(Gui.overlay);
	}
}
