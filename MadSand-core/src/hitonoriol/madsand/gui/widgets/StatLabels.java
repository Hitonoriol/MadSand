package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import hitonoriol.madsand.Gui;
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
	public Label hpStatLbl, staminaStatLbl;
	
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
	}
	
	public StatLabels() {
		this(Gui.skin);
	}
	
	public void refreshStatLabels() {
		Stats s = World.player.stats;
		strStatLbl.setText("Strength: " + s.strength);
		accStatLbl.setText("Accuracy: " + s.accuracy);
		conStatLbl.setText("Constitution: " + s.constitution);
		intStatLbl.setText("Intelligence: " + s.intelligence);
		luckStatLbl.setText("Luck: " + s.luck);
		dexStatLbl.setText("Dexterity: " + s.dexterity);
		statSumLbl.setText("\nStat sum: " + s.getSum());

		hpStatLbl.setText("HP: " + s.hp + "/" + s.mhp);
		staminaStatLbl.setText("Stamina: " + s.stamina + "/" + s.maxstamina);
	}
}
