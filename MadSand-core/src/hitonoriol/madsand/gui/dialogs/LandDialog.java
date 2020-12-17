package hitonoriol.madsand.gui.dialogs;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.entities.inventory.ItemUI;
import hitonoriol.madsand.enums.Faction;
import hitonoriol.madsand.enums.WorkerType;
import hitonoriol.madsand.properties.WorldGenProp;
import hitonoriol.madsand.world.Location;
import hitonoriol.madsand.world.Settlement;
import hitonoriol.madsand.world.World;
import hitonoriol.madsand.world.Settlement.WorkerContainer;

public class LandDialog extends GameDialog {

	private Location location;
	private Settlement settlement;

	Table dialogContents = new Table(Gui.skin);

	private LandDialog(Stage stage) {
		super(stage);
	}

	public LandDialog(Location location) {
		super(Gui.overlay);
		this.location = location;
		this.settlement = location.settlement;
		super.setTitle(location.name);
		super.skipLine();

		initDialogContents();

		super.add(dialogContents).height(200).row();
		super.addCloseButton().align(Align.center).padTop(20).row();
		super.pack();
	}

	private void initDialogContents() {
		dialogContents.align(Align.topLeft);
		dialogContents.defaults().align(Align.topLeft).padBottom(5).padLeft(13);

		dialogContents.add("* Biome: " + WorldGenProp.getBiome(location.biome).biomeName).row();
		dialogContents.add(location.faction == Faction.None ? "* Ownerless land" : ("* Owned by " + location.faction))
				.row();
		dialogContents.add("").row();

		if (this.settlement == null)
			addSettlementCreationMenu(dialogContents);
		else
			addSettlementInfo(dialogContents);
	}

	private void addSettlementCreationMenu(Table container) {
		Player player = World.player;
		ArrayList<Item> reqItems = player.getSettlementCreationReq();

		container.add("There's no civilization nearby").align(Align.center).row();
		container.add("").row();
		container.add("Establish a new settlement:").row();
		container.add(ItemUI.createItemList(reqItems)).align(Align.center).row();

		boolean reqs = true;
		for (Item item : reqItems)
			reqs &= player.inventory.hasItem(item.id, item.quantity);

		TextButton createBtn = new TextButton("Establish", Gui.skin);
		if (reqs)
			container.add(createBtn).row();
		else
			container.add("You don't have the materials required to do this").row();

		createBtn.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				player.inventory.delItem(reqItems);
				location.createSettlement().setPlayerOwned();
				++player.settlementsEstablished;
				dialogContents.clear();
				initDialogContents();
			}
		});
	}

	private void addSettlementInfo(Table container) {
		container.add("Settlement " + location.name).row();

		container.add("Workers:").row();
		container.add(getWorkerList()).row();
		container.add("").row();

		container.add("Warehouse:").row();
		container.add(getWarehouseContents()).row();
	}

	private String getWorkerList() {
		StringBuilder sb = new StringBuilder();

		WorkerContainer workers;
		for (WorkerType type : WorkerType.values()) {
			if ((workers = settlement.getWorkers(type)).quantity == 0)
				continue;

			sb.append(type.name() + ": " + workers.quantity + " ");
			sb.append("(" + Utils.round(workers.getGatheringRate() / MadSand.world.realtimeTickRate)
					+ " actions per sec)");
			sb.append(Resources.LINEBREAK);
		}

		return sb.toString();
	}

	private String getWarehouseContents() {
		StringBuilder sb = new StringBuilder();
		for (Item item : settlement.warehouse.items)
			sb.append(
					"* " + item.quantity + " " + item.name + " (" + Utils.round(item.getWeight()) + ")"
							+ Resources.LINEBREAK);
		return settlement.warehouse.items.isEmpty() ? "Empty" : sb.toString();
	}

}
