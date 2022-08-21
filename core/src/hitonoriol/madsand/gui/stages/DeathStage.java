package hitonoriol.madsand.gui.stages;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;

public class DeathStage extends Stage {
	Label titleLabel, deathMsgLabel;
	TextButton respawnButton;

	public DeathStage() {
		super(Gui.viewport());
		Table container = new Table();
		final float SIZE_COEF = 1.85f;

		deathMsgLabel = new Label("", Gui.skin);
		deathMsgLabel.setAlignment(Align.center);
		respawnButton = new TextButton("Respawn", Gui.skin);
		titleLabel = new Label("You Died", Gui.skin);
		Gui.setFontSize(titleLabel, Gui.FONT_XL);
		Gui.setFontSize(deathMsgLabel, 19);
		titleLabel.setAlignment(Align.center);

		container.add(titleLabel).align(Align.center).width(Gui.DEF_LABEL_WIDTH).padBottom(30f).row();
		container.add(deathMsgLabel).padBottom(75).row();
		container.add(respawnButton).size(Gui.BTN_WIDTH * SIZE_COEF, Gui.BTN_HEIGHT * SIZE_COEF).row();
		container.setFillParent(true);
		container.setBackground(Gui.darkBackground);
		super.addActor(container);

		Gui.setAction(respawnButton, () -> {
			MadSand.player().respawn();
			MadSand.world().updateLight();
			MadSand.reset();
		});
	}

	public void setDeathMessage(String str) {
		deathMsgLabel.setText(str);
	}
}
