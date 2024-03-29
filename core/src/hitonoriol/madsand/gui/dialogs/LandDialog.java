package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Faction;
import hitonoriol.madsand.entities.inventory.ItemUI;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gamecontent.WorldGenPresets;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.Location;
import hitonoriol.madsand.world.Settlement;
import hitonoriol.madsand.world.Settlement.WorkerContainer;
import hitonoriol.madsand.world.WorkerType;

public class LandDialog extends GameDialog {

	static int DEC_PLACES = 4;
	static int ITEMS_PER_ROW = 6;
	static float ITEM_SCALE = 0.1f;
	static float PAD = 5;

	private Location location;
	private Settlement settlement;
	private Timer.Task refreshTask;

	Table dialogContents = Widgets.table();

	public LandDialog(Location location) {
		super(Gui.overlay);
		this.location = location;
		settlement = location.settlement;
		super.setTitle(location.name);
		super.skipLine();
		super.add(new AutoFocusScrollPane(dialogContents)).height(240).pad(PAD * 2).row();
		super.skipLine();
		super.addCloseButton().align(Align.center).padTop(15).row();
		refreshDialogContents();
		super.pack();

		Timer.instance().scheduleTask(refreshTask = new Timer.Task() {
			@Override
			public void run() {
				if (location.isSettlement())
					refreshDialogContents();
			}
		}, 0, MadSand.world().getRealtimeActionSeconds());
	}

	private void refreshDialogContents() {
		dialogContents.clear();
		dialogContents.align(Align.topLeft);
		dialogContents.defaults().align(Align.topLeft).padBottom(PAD).padLeft(13);

		dialogContents.add("* Biome: " + WorldGenPresets.all().get(location.biome).biomeName).row();
		dialogContents.add(location.faction == Faction.None ? "* Ownerless land" : ("* Owned by " + location.faction))
			.row();
		dialogContents.add("").row();

		if (settlement == null)
			addSettlementCreationMenu(dialogContents);
		else
			addSettlementInfo(dialogContents);
	}

	private void addSettlementCreationMenu(Table container) {
		var player = MadSand.player();
		var reqItems = player.getSettlementCreationReq();

		container.add("There's no civilization nearby").align(Align.center).row();
		container.add("").row();
		container.add("Establish a new settlement:").padBottom(PAD * 2).row();
		container.add(ItemUI.createItemList(reqItems)).height(ItemUI.SIZE).align(Align.center).padBottom(PAD * 2).row();

		boolean reqs = true;
		for (Item item : reqItems)
			reqs &= player.inventory.hasItem(item.id(), item.quantity);

		var createBtn = Widgets.button("Establish");
		if (reqs)
			container.add(createBtn).size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT).align(Align.center);
		else
			container.add("Not enough materials").align(Align.center);
		container.row();

		createBtn.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				player.inventory.delItem(reqItems);
				location.createSettlement().setPlayerOwned();
				player.establishSettlement();
				settlement = location.settlement;
				Gui.overlay.refresh();
				refreshDialogContents();
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void addSettlementInfo(Table container) {
		container.add(location.name + " settlement ").row();
		container.add("Leader: " + settlement.getLeaderName()).row();
		container.add("").row();

		container.add("Workers:").row();
		container.add(getWorkerList()).row();
		container.add("").row();

		container.add("Warehouse " + settlement.warehouse.getWeightString() + ":").row();
		container.add(getWarehouseContents()).row();
		var warehouseTbl = ItemUI.createItemList(settlement.warehouse.getItems(), ITEMS_PER_ROW);
		for (Cell<Actor> cell : warehouseTbl.getCells()) {
			cell.getActor().scaleBy(-ITEM_SCALE);
			cell.size(ItemUI.SIZE * (1f - ITEM_SCALE));
		}
		container.add(warehouseTbl).row();
	}

	private String getWorkerList() {
		var sb = new StringBuilder();

		WorkerContainer workers;
		for (WorkerType type : WorkerType.values()) {
			if ((workers = settlement.getWorkers(type)).getQuantity() == 0)
				continue;

			sb.append("* " + type.name() + ": " + workers.getQuantity() + " ");
			sb.append(
				"(" + Utils.round(workers.getGatheringRate() / MadSand.world().getRealtimeActionSeconds(), DEC_PLACES)
					+ " actions/sec) "
			)
				.append("[[" + Utils.round(workers.itemCharge * 100f) + "%]")
				.append(Resources.LINEBREAK);
		}
		if (settlement.workers.isEmpty())
			sb.append("No workers recruited");

		return sb.toString();
	}

	private String getWarehouseContents() {
		var sb = new StringBuilder();
		for (Item item : settlement.warehouse.getItems())
			sb.append("* " + item.quantity + " " + item.name + " (" + Utils.round(item.getTotalWeight()) + " kg)")
				.append(Resources.LINEBREAK);

		return settlement.warehouse.getItems().isEmpty() ? "Empty" : sb.toString();
	}

	@Override
	public boolean remove() {
		refreshTask.cancel();
		return super.remove();
	}

}
