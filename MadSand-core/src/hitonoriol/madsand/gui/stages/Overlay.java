package hitonoriol.madsand.gui.stages;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.Stats;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.Skill;
import hitonoriol.madsand.gui.dialogs.CharacterCreationDialog;
import hitonoriol.madsand.gui.dialogs.CharacterInfoWindow;
import hitonoriol.madsand.world.World;

/*
 * Main in-game overlay (GameStage.GAME) 
 */

public class Overlay extends Stage {

	public CharacterInfoWindow statWindow;
	public CharacterCreationDialog charCreateDialog;

	public TextButton exitToMenuBtn;
	public TextButton craftBtn;

	static Label[] overlayStatLabels;
	static Image[] equip;

	public static final int EQ_SLOTS = 5;
	static int ITEM_DISPLAY_HEAD = 0;
	static int ITEM_DISPLAY_CHEST = 1;
	static int ITEM_DISPLAY_LEGS = 2;
	static int ITEM_DISPLAY_SHIELD = 3;
	static int ITEM_DISPLAY_HOLDING = 4;
	static int ITEM_DISPLAY_SLOTS = 5;
	static final int OVSTAT_COUNT = 6;

	public Overlay() {
		super();
		init();
	}

	private void init() {
		overlayStatLabels = new Label[OVSTAT_COUNT];

		equip = new Image[EQ_SLOTS];
		for (int i = 0; i < EQ_SLOTS; ++i) {
			equip[i] = new Image();
			equip[i].setDrawable(Resources.noEquip);
		}
	}

	public void showStatsWindow() {
		if (statWindow != null) {
			statWindow.remove();
			statWindow = null;
			return;
		}

		statWindow.show();
	}

	public void createCharDialog() {
		if (charCreateDialog == null)
			charCreateDialog = new CharacterCreationDialog();
		charCreateDialog.show();
	}

	public void setHandDisplay(int id) {
		Utils.out("Setting hand display to " + id);
		Drawable img = Resources.noEquip;
		if (id != 0)
			img = new TextureRegionDrawable(Resources.item[id]);
		equip[ITEM_DISPLAY_HOLDING].setDrawable((Drawable) img);
	}

	public void refreshEquipDisplay() {
		Stats stats = World.player.stats;
		Drawable img = Resources.noEquip;

		if (stats.headEquip != Item.nullItem)
			img = new TextureRegionDrawable(Resources.item[stats.headEquip.id]);
		equip[ITEM_DISPLAY_HEAD].setDrawable((Drawable) img);

		img = Resources.noEquip;
		if (stats.chestEquip != Item.nullItem)
			img = new TextureRegionDrawable(Resources.item[stats.chestEquip.id]);
		equip[ITEM_DISPLAY_CHEST].setDrawable((Drawable) img);

		img = Resources.noEquip;
		if (stats.legsEquip != Item.nullItem)
			img = new TextureRegionDrawable(Resources.item[stats.legsEquip.id]);
		equip[ITEM_DISPLAY_LEGS].setDrawable((Drawable) img);

	}

	public void refreshOverlay() {
		overlayStatLabels[0].setText("HP: " + World.player.stats.hp + "/" + World.player.stats.mhp);
		overlayStatLabels[1].setText("LVL: " + World.player.stats.skills.getLvl(Skill.Level));
		overlayStatLabels[2].setText("XP: " + World.player.stats.skills.getExpString(Skill.Level));
		overlayStatLabels[3].setText("Food: " + World.player.stats.food + " / " + World.player.stats.maxFood);
		overlayStatLabels[4].setText("Hand: " + World.player.stats.hand.name);
	}
}
