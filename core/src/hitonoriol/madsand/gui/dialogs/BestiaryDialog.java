package hitonoriol.madsand.gui.dialogs;

import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gamecontent.NpcDescriptor;
import hitonoriol.madsand.gamecontent.Npcs;
import hitonoriol.madsand.gamecontent.Textures;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;

public class BestiaryDialog extends GameDialog {

	float PAD_BOTTOM = 20;
	float ENTRY_PAD = 5;
	float ENTRY_WIDTH = 120;
	float ENTRY_HEIGHT = 160;
	int ENTRIES_PER_ROW = 4;

	float WIDTH = (ENTRY_WIDTH * ENTRIES_PER_ROW) + (ENTRY_PAD * ENTRIES_PER_ROW);
	float HEIGHT = (ENTRY_HEIGHT * 2) + PAD_BOTTOM;

	Table scrollTable = new Table();
	AutoFocusScrollPane scroll;
	Label emptyLabel = new Label("You haven't killed any monsters yet", Gui.skin);

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
		NpcDescriptor npc = Npcs.all().get(id);
		Table entry = new Table();
		Label topLabel = new Label(npc.name, Gui.skin);
		Label bottomLabel = new Label("Kills: " + kills, Gui.skin);

		topLabel.setWrap(true);
		bottomLabel.setWrap(true);
		topLabel.setAlignment(Align.center);
		bottomLabel.setAlignment(Align.center);

		entry.add(topLabel).align(Align.center).width(ENTRY_WIDTH).row();
		entry.add(new Image(Textures.getNpc(id))).expandY().row();

		if (kills >= STAT_KILLS) {
			entry.add(new Label("HP: " + npc.hp, Gui.skin)).align(Align.center).row();
			entry.add(new Label("Def: " + npc.defense, Gui.skin)).align(Align.center).row();
			entry.add(new Label("Str: " + npc.strength, Gui.skin)).align(Align.center).row();
			entry.add(new Label("Acc: " + npc.accuracy, Gui.skin)).align(Align.center).row();
			entry.add(new Label("Dex: " + npc.dexterity, Gui.skin)).align(Align.center).padBottom(10).row();
		} else {
			Label killMoreLbl = new Label("Kill " + (STAT_KILLS - kills) + " more to unlock its stats", Gui.skin);
			killMoreLbl.setWrap(true);
			killMoreLbl.setAlignment(Align.center);
			entry.add(killMoreLbl)
					.width(ENTRY_WIDTH)
					.align(Align.center)
					.pad(10).row();
		}

		entry.add(bottomLabel).row();

		entry.setBackground(GuiSkin.darkBackground());
		return entry;
	}

}
