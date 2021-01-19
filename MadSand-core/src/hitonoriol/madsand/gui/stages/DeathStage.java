package hitonoriol.madsand.gui.stages;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.world.World;

public class DeathStage extends Stage {
	Label titleLabel, deathMsgLabel;
	TextButton respawnButton;

	public DeathStage() {
		super(Gui.uiViewport);
		Table container = new Table();

		deathMsgLabel = new Label("", Gui.skin);
		deathMsgLabel.setAlignment(Align.center);
		respawnButton = new TextButton("Respawn", Gui.skin);
		titleLabel = new Label("You Died", Gui.skin);
		Gui.setFontSize(titleLabel, Gui.FONT_XL);

		container.add(titleLabel).width(Gui.defLblWidth).row();
		container.add(deathMsgLabel).padBottom(75).row();
		container.add(respawnButton).width(500.0F).row();
		container.setFillParent(true);
		super.addActor(Gui.darkness);
		super.addActor(container);

		Gui.setAction(respawnButton, () -> {
			World.player.respawn();
			MadSand.world.updateLight();
			MadSand.reset();
		});
	}

	public void setDeathMessage(String str) {
		deathMsgLabel.setText(str);
	}
}
