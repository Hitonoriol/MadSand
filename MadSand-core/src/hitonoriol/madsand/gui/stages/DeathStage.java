package hitonoriol.madsand.gui.stages;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.world.World;

public class DeathStage extends Stage {
	Skin skin;
	Label dieLabel;
	TextButton respawnButton;
	
	public DeathStage() {
		super(Gui.uiViewport);
		skin = Gui.skin;
		
		respawnButton = new TextButton("Respawn", skin);
		Table tab = new Table();
		dieLabel = new Label("", skin);
		dieLabel.setAlignment(Align.center);
		tab.add(dieLabel).width(500.0F);
		tab.row();
		tab.add(new Label("", skin)).width(500.0F);
		tab.row();
		tab.add(respawnButton).width(500.0F).row();
		super.addActor(Gui.darkness);
		tab.setFillParent(true);
		super.addActor(tab);
		
		initRespawnListener();
	}
	
	private void initRespawnListener() {
		respawnButton.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				Gui.darkness.setVisible(false);
				World.player.respawn();
				MadSand.world.updateLight();
				MadSand.reset();
			}
		});
	}
	
	public void setDeadText(String str) {
		dieLabel.setText(str);
	}
}
