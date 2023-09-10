package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;

import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.Widgets;

public class RowColorTable extends Table {
	private Color colorA;
	private Color colorB;
	private Color mouseOverColor;

	private int rowIdx = 0;
	private Table rowContainer = null;

	public RowColorTable(Color colorA, Color colorB) {
		super(Gui.skin);
		this.colorA = colorA;
		this.colorB = colorB;
		mouseOverColor = new Color(
			(colorA.r + colorB.r) * 0.5f,
			(colorA.g + colorB.g) * 0.5f,
			(colorA.b + colorB.b) * 0.5f, 1.0f
		);
	}

	public RowColorTable() {
		this(new Color(0.6f, 0.6f, 0.6f, 0.5f), new Color(0.4f, 0.4f, 0.4f, 0.5f));
	}

	public void setRowBackground(Color color) {
		if (rowContainer == null)
			createContainer();

		rowContainer.setBackground(GuiSkin.getColorDrawable(color));
	}

	private void createContainer() {
		rowContainer = Widgets.table();
		setRowBackground(rowIdx % 2 == 0 ? colorA : colorB);
		rowContainer.addListener(new ClickListener() {
			private Table container = rowContainer;
			private Drawable background = rowContainer.getBackground();
			
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				container.setBackground(GuiSkin.getColorDrawable(mouseOverColor));
				super.enter(event, x, y, pointer, fromActor);
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				container.setBackground(background);
				super.exit(event, x, y, pointer, toActor);
			}
		});
		super.add(rowContainer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Cell<? extends Actor> add(@Null Actor actor) {
		if (rowContainer == null)
			createContainer();

		return rowContainer.add(actor);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Cell row() {
		++rowIdx;
		rowContainer = null;
		return super.row();
	}
}
