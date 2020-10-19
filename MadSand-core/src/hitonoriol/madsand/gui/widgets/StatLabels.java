package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.containers.IntContainer;
import hitonoriol.madsand.entities.Stats;
import hitonoriol.madsand.world.World;

public class StatLabels {
	public Label conStatLbl;
	public Label strStatLbl;
	public Label accStatLbl;
	public Label intStatLbl;
	public Label luckStatLbl;
	public Label dexStatLbl;
	public Label defStatLbl;
	public Label statSumLbl;
	public Label freeStatPointsLbl;
	public Label hpStatLbl, staminaStatLbl;

	public Stats stats = World.player.stats;

	public StatLabels(Skin skin) {
		conStatLbl = new Label("", skin);
		strStatLbl = new Label("", skin);
		accStatLbl = new Label("", skin);
		intStatLbl = new Label("", skin);
		luckStatLbl = new Label("", skin);
		dexStatLbl = new Label("", skin);
		defStatLbl = new Label("", skin);
		statSumLbl = new Label("", skin);
		hpStatLbl = new Label("", skin);
		staminaStatLbl = new Label("", skin);
		freeStatPointsLbl = new Label("", skin);
	}

	public StatLabels() {
		this(Gui.skin);
	}

	public void refreshStatLabels() {
		int statSum = stats.getSum();
		setStatText(strStatLbl, strString, stats.strength);
		setStatText(accStatLbl, accString, stats.accuracy);
		setStatText(conStatLbl, conString, stats.constitution);
		setStatText(intStatLbl, intString, stats.intelligence);
		setStatText(luckStatLbl, luckString, stats.luck);
		setStatText(dexStatLbl, dexString, stats.dexterity);
		setStatText(defStatLbl, defString, stats.defense);
		statSumLbl.setText("\nStat sum: " + statSum);
		freeStatPointsLbl.setText("Free stat points: " + (stats.maxStatSum - statSum));

		hpStatLbl.setText("HP: " + stats.hp + "/" + stats.mhp);
		staminaStatLbl.setText("Stamina: " + stats.stamina + "/" + stats.maxstamina);
		Gui.refreshOverlay();
	}

	public static String conString = "Constitution";
	public static String strString = "Strength";
	public static String accString = "Accuracy";
	public static String intString = "Intelligence";
	public static String luckString = "Luck";
	public static String dexString = "Dexterity";
	public static String defString = "Defense";

	public void refreshStatLabel(IntContainer value) {
		if (value.name.equals(conString))
			stats.constitution = value.value;
		if (value.name.equals(strString))
			stats.strength = value.value;
		if (value.name.equals(accString))
			stats.accuracy = value.value;
		if (value.name.equals(intString))
			stats.intelligence = value.value;
		if (value.name.equals(luckString))
			stats.luck = value.value;
		if (value.name.equals(dexString))
			stats.dexterity = value.value;

		stats.calcStats();
		refreshStatLabels();

	}

	private void setStatText(Label label, String stat, int value) {
		label.setText(stat + ": " + applyColor(value));
	}

	private String applyColor(int stat) {
		return "[LIME]" + stat + "[]";
	}
}
