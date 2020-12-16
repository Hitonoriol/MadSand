package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.Faction;
import hitonoriol.madsand.enums.WorkerType;
import hitonoriol.madsand.properties.WorldGenProp;
import hitonoriol.madsand.world.Location;
import hitonoriol.madsand.world.Settlement;
import hitonoriol.madsand.world.Settlement.WorkerContainer;

public class LandDialog extends GameDialog {

	private Location location;
	private Settlement settlement;

	private LandDialog(Stage stage) {
		super(stage);
	}

	public LandDialog(Location location) {
		super(Gui.overlay);
		this.location = location;
		this.settlement = location.settlement;
		super.setTitle(location.name);
		super.skipLine();

		Table containerTbl = new Table(Gui.skin);
		containerTbl.align(Align.topLeft);
		containerTbl.defaults().align(Align.topLeft).padBottom(5).padLeft(13);

		containerTbl.add("* Biome: " + WorldGenProp.getBiome(location.biome).biomeName).row();
		containerTbl.add(location.faction == Faction.None ? "* Ownerless land" : ("* Owned by " + location.faction))
				.row();
		containerTbl.add("").row();

		if (this.settlement == null)
			containerTbl.add("There's no civilization nearby").align(Align.center).row();
		else
			addSettlementInfo(containerTbl);

		super.add(containerTbl).height(200).row();
		super.addCloseButton().align(Align.center).row();
		super.pack();
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
