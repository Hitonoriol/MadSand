package hitonoriol.madsand.gui.dialogs;

import java.util.Map.Entry;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.map.ProductionStation;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.world.World;

public class BuildDialog extends GameDialog {

	float WIDTH = 300;
	float HEIGHT = 400;

	TextButton exitButton = new TextButton("Close", Gui.skin);

	Table buildTable = new Table();
	AutoFocusScrollPane buildScroll;
	float PAD = 50;

	private BuildDialog(Stage stage) {
		super(stage);
		super.setTitle("Build menu");
	}

	public BuildDialog() {
		this(Gui.overlay);
		super.add().padTop(15).row();
		buildTable.setBackground(Gui.darkBackgroundSizeable);
		BuildDialogEntry buildEntry;
		for (Entry<Integer, String> object : ObjectProp.buildRecipes.entrySet()) {
			buildEntry = new BuildDialogEntry(object.getKey(), object.getValue());
			buildTable.add(buildEntry).size(buildEntry.WIDTH, buildEntry.HEIGHT).padBottom(PAD).row();
		}
		buildScroll = new AutoFocusScrollPane(buildTable);
		super.add(buildScroll).size(WIDTH, HEIGHT).row();
		super.add(exitButton).size(100, 50);

		exitButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				remove();
			}
		});
	}

}

class BuildDialogEntry extends Group {
	final float WIDTH = 300;
	final float HEIGHT = 170;
	final float PAD = 10;
	Table container = new Table(Gui.skin);
	Player player = World.player;
	String recipe;
	int id;

	public BuildDialogEntry(int id, String recipe) {
		super();
		this.id = id;
		this.recipe = recipe;
		ProductionStation station = ObjectProp.productionStations.get(id);
		Image objImage = new Image(Resources.objects[id]);
		Label resourceLabel = new Label("Resources required to build:" + Resources.LINEBREAK + Item.queryToName(recipe),
				Gui.skin);
		resourceLabel.setWrap(true);
		resourceLabel.setAlignment(Align.center);

		container.add(ObjectProp.getName(id)).padBottom(PAD).row();
		container.add(objImage).align(Align.center).height(objImage.getHeight()).padBottom(PAD).row();
		container.add("Produces " + ItemProp.getItemName(station.producedMaterial)).padBottom(PAD).row();
		container.add("Consumes " + ItemProp.getItemName(station.consumedMaterial)).padBottom(PAD).row();
		container.add(resourceLabel).align(Align.center).width(WIDTH).row();
		container.setFillParent(true);

		super.setSize(WIDTH, HEIGHT);
		super.addActor(container);

		initListeners();
	}

	private void initListeners() {
		super.addListener(new InputListener() {
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				container.setBackground(Resources.noEquip);
			}

			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				container.setBackground(Gui.transparency);
			}
		});

		super.addListener(new ClickListener(Buttons.LEFT) {
			public void clicked(InputEvent event, float x, float y) {
				if (!player.inventory.delItem(recipe))
					Gui.drawOkDialog("You don't have enough resources to build this!", Gui.overlay);
				else {
					MadSand.world.getCurLoc().addObject(player.x, player.y, player.stats.look, id);
					remove();
				}
			}
		});
	}
}