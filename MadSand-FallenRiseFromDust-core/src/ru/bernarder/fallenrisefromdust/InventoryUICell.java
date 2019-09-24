package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class InventoryUICell {
	private final int size = 80;
	private ImageButton btn;
	private Label label;

	Group cell;

	public InventoryUICell(Item item) {
		btn = new ImageButton(new SpriteDrawable(new Sprite(Utils.item[item.id])));
		label = new Label(item.quantity + "", Gui.skin);
		cell = new Group();
		cell.addActor(btn);
		cell.addActor(label);
		cell.setSize(size, size);

		Table invcontext;
		TextButton[] invcontbtn;
		invcontbtn = new TextButton[1];
		invcontbtn[0] = new TextButton("Drop", Gui.skin);
		invcontext = new Table(Gui.skin);
		invcontext.add(invcontbtn[0]).width(100.0F).height(50.0F).row();
		invcontext.setVisible(false);
		Gui.overlay.addActor(invcontext);

		invcontbtn[0].addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				invcontext.setVisible(false);
				MadSand.contextopened = false;
			}

		});
		btn.addListener(new ClickListener(Buttons.LEFT) {
			public void clicked(InputEvent event, float x, float y) {
				MadSand.player.stats.hand = item.id;
				Utils.toggleInventory();
			}
		});
		btn.addListener(new ClickListener(Buttons.RIGHT) {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (!invcontext.isVisible() && !MadSand.contextopened) {
					invcontext.setVisible(true);
					MadSand.mx = Gdx.input.getX();
					MadSand.my = Gdx.graphics.getHeight() - Gdx.input.getY();
					invcontext.setPosition(MadSand.mx, MadSand.my);
					MadSand.contextopened = true;
				} else {
					invcontext.setVisible(false);
					MadSand.contextopened = false;
				}
			}
		});
	}

	void setText(String str) {
		label.setText(str);
	}
}
