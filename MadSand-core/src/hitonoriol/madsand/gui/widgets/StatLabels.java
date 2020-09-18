package hitonoriol.madsand.gui.widgets;

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
		strStatLbl.setText("Strength: " + stats.strength);
		accStatLbl.setText("Accuracy: " + stats.accuracy);
		conStatLbl.setText("Constitution: " + stats.constitution);
		intStatLbl.setText("Intelligence: " + stats.intelligence);
		luckStatLbl.setText("Luck: " + stats.luck);
		dexStatLbl.setText("Dexterity: " + stats.dexterity);
		statSumLbl.setText("\nStat sum: " + statSum);
		freeStatPointsLbl.setText("Free stat points: " + (Stats.STAT_MAX_SUM - statSum));

		hpStatLbl.setText("HP: " + stats.hp + "/" + stats.mhp);
		staminaStatLbl.setText("Stamina: " + stats.stamina + "/" + stats.maxstamina);
	}

	public static String conString = "Constitution";
	public static String strString = "Strength";
	public static String accString = "Accuracy";
	public static String intString = "Intelligence";
	public static String luckString = "Luck";
	public static String dexString = "Dexterity";

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

		refreshStatLabels();

	}
}
