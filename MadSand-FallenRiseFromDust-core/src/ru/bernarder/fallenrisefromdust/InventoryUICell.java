package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
	}
	
	void setText(String str) {
		label.setText(str);
	}
}
