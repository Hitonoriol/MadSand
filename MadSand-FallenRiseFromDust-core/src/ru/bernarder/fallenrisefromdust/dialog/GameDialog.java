package ru.bernarder.fallenrisefromdust.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ru.bernarder.fallenrisefromdust.Gui;
import ru.bernarder.fallenrisefromdust.Tuple;

public class GameDialog extends Dialog {
	public static final float WIDTH = Gdx.graphics.getWidth() / 4;
	private Label textLbl;
	private Stage stage;

	@SafeVarargs
	public GameDialog(String title, String text, Stage stage, Tuple<String, GameDialog>... replies) {
		super(title, Gui.skin);
		textLbl = new Label(text, Gui.skin);
		add(textLbl).width(WIDTH).row();
		this.stage = stage;
		for (Tuple<String, GameDialog> reply : replies) {
			chainReply(reply.l, reply.r);
		}
	}

	public GameDialog(String title, String text, Stage stage, String okText) {
		this(title, text, stage);
		addOkButton(okText);
	}

	public void chainReply(String btnText, GameDialog nextDialog) {
		TextButton nextBtn = new TextButton(btnText, Gui.skin);
		add(nextBtn).width(WIDTH).row();

		nextBtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				nextDialog.show(stage);
				remove();
			}
		});
	}

	public void addOkButton(String text) {
		TextButton okBtn = new TextButton(text, Gui.skin);
		add(okBtn).width(WIDTH).row();
		okBtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				remove();
			}
		});
	}

	public void addButton(TextButton button) {
		add(button).width(WIDTH).row();
	}

	public void show() {
		show(stage);
	}

}
