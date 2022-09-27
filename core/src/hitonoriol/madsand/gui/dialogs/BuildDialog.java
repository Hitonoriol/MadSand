package hitonoriol.madsand.gui.dialogs;

import java.util.Map.Entry;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.ItemUI;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gamecontent.Objects;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.resources.Resources;

public class BuildDialog extends GameDialog {

	float WIDTH = 300;
	float HEIGHT = 400;

	Table buildTable = new Table();
	Label emptyLabel = new Label("You don't know how to build anything", Gui.skin);
	Label unlockProgressLabel = new Label("", Gui.skin);
	AutoFocusScrollPane buildScroll;
	float PAD = 50;

	public BuildDialog() {
		super(Gui.overlay);
		super.setTitle("Build menu");
		super.add().padTop(15).row();
		super.add(unlockProgressLabel).width(WIDTH).row();
		buildTable.setBackground(GuiSkin.darkBackground());
		BuildDialogEntry buildEntry;
		Player player = MadSand.player();
		for (Entry<Integer, String> object : Objects.all().buildRecipes().entrySet()) {
			if (player.getBuildRecipes().contains(object.getKey())) {
				buildEntry = new BuildDialogEntry(this, object.getKey(), object.getValue());
				buildTable.add(buildEntry).size(buildEntry.WIDTH, buildEntry.HEIGHT).padBottom(PAD).row();
			}
		}

		unlockProgressLabel.setText("Build recipes unlocked: " + player.buildRecipeProgress());
		unlockProgressLabel.setWrap(true);
		unlockProgressLabel.setAlignment(Align.center);
		emptyLabel.setWrap(true);
		emptyLabel.setAlignment(Align.center);

		if (player.getBuildRecipes().isEmpty())
			buildTable.add(emptyLabel).width(WIDTH);

		buildScroll = new AutoFocusScrollPane(buildTable);
		super.add(buildScroll).size(WIDTH, HEIGHT).row();
		super.add(createCloseButton()).size(100, 50);
	}

}

class BuildDialogEntry extends Group {
	final float WIDTH = 300;
	final float HEIGHT = 170;
	final float PAD = 10;
	BuildDialog dialog;
	Table container = new Table(Gui.skin);
	Player player = MadSand.player();
	String recipe;
	int id;

	public BuildDialogEntry(BuildDialog dialog, int id, String recipe) {
		super();
		this.dialog = dialog;
		this.id = id;
		this.recipe = recipe;

		Image objImage = new Image(Resources.getObject(id));
		Label resourceLabel = new Label(
				"Resources required to build:" + Resources.LINEBREAK + Item.createReadableItemList(recipe),
				Gui.skin);
		resourceLabel.setWrap(true);
		resourceLabel.setAlignment(Align.center);

		container.add(Objects.all().getName(id)).padBottom(PAD).row();
		container.add(objImage).align(Align.center).height(objImage.getHeight()).padBottom(PAD).row();
		container.add(Objects.all().get(id).getBuildInfo()).padBottom(PAD).row();
		container.add(resourceLabel).align(Align.center).width(WIDTH).row();
		container.setFillParent(true);

		super.setSize(WIDTH, HEIGHT);
		super.addActor(container);

		initListeners();
	}

	private void initListeners() {
		super.addListener(new InputListener() {
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				container.setBackground(ItemUI.emptyItem);
			}

			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				container.setBackground(GuiSkin.transparency());
			}
		});

		super.addListener(new ClickListener(Buttons.LEFT) {
			public void clicked(InputEvent event, float x, float y) {
				MapObject object = player.objectLookingAt();
				if (!object.equals(Map.nullObject))
					Gui.drawOkDialog("The tile in front of you is not empty." + Resources.LINEBREAK
							+ "You can only build on empty tiles.");
				else if (!player.inventory.itemsExist(recipe))
					Gui.drawOkDialog("You don't have enough resources to build this!");
				else {
					player.inventory.delItem(recipe);
					MadSand.world().getCurLoc().addObject(player.x, player.y, player.stats.look, id);
					dialog.remove();
				}
			}
		});
	}
}