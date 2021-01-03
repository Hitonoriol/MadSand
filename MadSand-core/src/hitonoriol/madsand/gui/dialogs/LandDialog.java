package hitonoriol.madsand.gui.dialogs;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Faction;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.entities.inventory.ItemUI;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.properties.WorldGenProp;
import hitonoriol.madsand.world.Location;
import hitonoriol.madsand.world.Settlement;
import hitonoriol.madsand.world.WorkerType;
import hitonoriol.madsand.world.World;
import hitonoriol.madsand.world.Settlement.WorkerContainer;

public class LandDialog extends GameDialog {

	static int ITEMS_PER_ROW = 6;
	static float ITEM_SCALE = 0.1f;
	static float PAD = 5;

	private Location location;
	private Settlement settlement;
	private Timer.Task refreshTask;

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
		}, 0, MadSand.world.realtimeTickRate);
	}

	private void refreshDialogContents() {
		dialogContents.clear();
		dialogContents.align(Align.topLeft);
		dialogContents.defaults().align(Align.topLeft).padBottom(PAD).padLeft(13);

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
		container.add("Establish a new settlement:").padBottom(PAD * 2).row();
		container.add(ItemUI.createItemList(reqItems)).height(ItemUI.SIZE).align(Align.center).padBottom(PAD * 2).row();

		boolean reqs = true;
		for (Item item : reqItems)
			reqs &= player.inventory.hasItem(item.id, item.quantity);

		TextButton createBtn = new TextButton("Establish", Gui.skin);
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
				++player.settlementsEstablished;
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
		Table warehouseTbl = ItemUI.createItemList(settlement.warehouse.items, ITEMS_PER_ROW);
		for (Cell<Actor> cell : warehouseTbl.getCells()) {
			cell.getActor().scaleBy(-ITEM_SCALE);
			cell.size(ItemUI.SIZE * (1f - ITEM_SCALE));
		}
		container.add(warehouseTbl).row();
	}

	private String getWorkerList() {
		StringBuilder sb = new StringBuilder();

		WorkerContainer workers;
		for (WorkerType type : WorkerType.values()) {
			if ((workers = settlement.getWorkers(type)).getQuantity() == 0)
				continue;

			sb.append("* " + type.name() + ": " + workers.getQuantity() + " ");
			sb.append("(" + Utils.round(workers.getGatheringRate() / MadSand.world.realtimeTickRate) + " actions/sec) ")
					.append("[[" + Utils.round(workers.itemCharge * 100f) + "%]")
					.append(Resources.LINEBREAK);
		}
		if (settlement.workers.isEmpty())
			sb.append("No workers recruited");

		return sb.toString();
	}

	private String getWarehouseContents() {
		StringBuilder sb = new StringBuilder();
		for (Item item : settlement.warehouse.items)
			sb.append("* " + item.quantity + " " + item.name + " (" + Utils.round(item.getWeight()) + " kg)")
					.append(Resources.LINEBREAK);

		return settlement.warehouse.items.isEmpty() ? "Empty" : sb.toString();
	}

	@Override
	public boolean remove() {
		refreshTask.cancel();
		return super.remove();
	}

}
