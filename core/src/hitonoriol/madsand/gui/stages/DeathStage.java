package hitonoriol.madsand.gui.stages;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.Widgets;

public class DeathStage extends Stage {
	Label titleLabel, deathMsgLabel;
	TextButton respawnButton;

	public DeathStage() {
		super(Gui.viewport());
		var container = Widgets.table();
		final float SIZE_COEF = 1.85f;

		deathMsgLabel = Widgets.label("");
		deathMsgLabel.setAlignment(Align.center);
		respawnButton = Widgets.button("Respawn");
		titleLabel = Widgets.label("You Died");
		Gui.setFontSize(titleLabel, Gui.FONT_XL);
		Gui.setFontSize(deathMsgLabel, 19);
		titleLabel.setAlignment(Align.center);

		container.add(titleLabel).align(Align.center).width(Gui.DEFAULT_WIDTH).padBottom(30f).row();
		container.add(deathMsgLabel).padBottom(75).row();
		container.add(respawnButton).size(Gui.BTN_WIDTH * SIZE_COEF, Gui.BTN_HEIGHT * SIZE_COEF).row();
		container.setFillParent(true);
		container.setBackground(GuiSkin.darkBackground());
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
