package hitonoriol.madsand.gui.dialogs;

import static hitonoriol.madsand.gui.Widgets.label;

import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gamecontent.Npcs;
import hitonoriol.madsand.gamecontent.Textures;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;

public class BestiaryDialog extends GameDialog {

	float PAD_BOTTOM = 20;
	float ENTRY_PAD = 5;
	float ENTRY_WIDTH = 120;
	float ENTRY_HEIGHT = 160;
	int ENTRIES_PER_ROW = 4;

	float WIDTH = (ENTRY_WIDTH * ENTRIES_PER_ROW) + (ENTRY_PAD * ENTRIES_PER_ROW);
	float HEIGHT = (ENTRY_HEIGHT * 2) + PAD_BOTTOM;

	Table scrollTable = Widgets.table();
	AutoFocusScrollPane scroll;
	Label emptyLabel = Widgets.label("You haven't killed any monsters yet");

	public BestiaryDialog(Player player) {
		super(Gui.overlay);
		super.getTitleLabel().setAlignment(Align.center);
		super.setTitle("Bestiary");
		super.add().padBottom(15).row();
		emptyLabel.setAlignment(Align.center);
		scroll = new AutoFocusScrollPane(scrollTable);
		scrollTable.align(Align.topLeft);
		Map<Integer, Integer> killCount = player.getKillCount();

		if (killCount.isEmpty())
			scrollTable.add(emptyLabel).align(Align.center).expand();

		int i = 1;
		for (Entry<Integer, Integer> entry : killCount.entrySet()) {
			scrollTable.add(createNpcEntry(entry.getKey(), entry.getValue()))
				.width(ENTRY_WIDTH).height(ENTRY_HEIGHT)
				.padRight(ENTRY_PAD).padBottom(PAD_BOTTOM);

			if (i % ENTRIES_PER_ROW == 0) {
				scrollTable.row();
				i = 1;
			}

			++i;
		}

		super.add(scroll).size(WIDTH, HEIGHT).pad(ENTRY_PAD).row();
		super.addCloseButton(175, 40);
	}

	int STAT_KILLS = 10;

	Table createNpcEntry(int id, int kills) {
		var npc = Npcs.all().get(id);
		var entry = Widgets.table();
		entry.debugAll();
		var topLabel = Widgets.label(npc.name);
		var bottomLabel = Widgets.label("Kills: " + kills);

		topLabel.setWrap(true);
		bottomLabel.setWrap(true);
		topLabel.setAlignment(Align.center);
		bottomLabel.setAlignment(Align.center);

		entry.add(topLabel).align(Align.center).width(ENTRY_WIDTH).row();
		entry.add(new Image(Textures.getNpc(id))).growY().row();

		if (kills >= STAT_KILLS) {
			entry.add(label("HP: " + npc.hp)).align(Align.center).row();
			entry.add(label("Def: " + npc.defense)).align(Align.center).row();
			entry.add(label("Str: " + npc.strength)).align(Align.center).row();
			entry.add(label("Acc: " + npc.accuracy)).align(Align.center).row();
			entry.add(label("Dex: " + npc.dexterity)).align(Align.center).padBottom(10).row();
		} else {
			var killMoreLbl = label("Kill " + (STAT_KILLS - kills) + " more to unlock its stats");
			killMoreLbl.setWrap(true);
			killMoreLbl.setAlignment(Align.center);
			entry.add(killMoreLbl)
				.growX()
				.align(Align.center)
				.pad(10).row();
		}

		entry.add(bottomLabel).growX().row();

		entry.setBackground(GuiSkin.darkBackground());
		return entry;
	}

}
